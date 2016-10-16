/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.client.handler;

import java.net.URI;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.transport.netty.client.NettyClient;
import com.zhucode.longio.transport.netty.server.NettyServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author zhu jinxian
 * @date  2016年9月15日 下午3:49:49 
 * 
 */
public class HttpClientHandler extends AbstractNettyClientHandler {
	
	private WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private URI uri;


	
	public HttpClientHandler(NettyClient client, Protocol protocol, URI uri) {
		super(client, protocol);
		this.uri = uri;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (handshaker == null) {
				handleHttp(ctx, msg);
			} else {
				handleWebsocket(ctx, msg);
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}

	}

	@Override
	public ChannelFuture write(ChannelHandlerContext ctx, byte[] bytes) {
		return sendForHttp(ctx, bytes);
	}
	
	private void handleWebsocket(ChannelHandlerContext ctx, Object msg) {
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			System.out.println("WebSocket Client connected!");
			handshakeFuture.setSuccess();
			return;
		}
		
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
	}

	private void handleHttp(ChannelHandlerContext ctx, Object msg) {
		FullHttpResponse res = (FullHttpResponse)msg;
		ByteBuf buf = res.content();
		this.client.processMessage(protocol, buf);
	}
	
	private ChannelFuture sendForHttp(ChannelHandlerContext ctx, byte[] bytes) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                uri.toASCIIString(), Unpooled.wrappedBuffer(bytes));

        // 构建http请求
        request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
	
        return ctx.writeAndFlush(request);
	}
	
	private ChannelFuture sendForWebSocket(ChannelHandlerContext ctx, byte[] bytes) {
		TextWebSocketFrame frame = new TextWebSocketFrame();
		frame.content().writeBytes(bytes);
		return ctx.channel().writeAndFlush(frame);
	}


}
