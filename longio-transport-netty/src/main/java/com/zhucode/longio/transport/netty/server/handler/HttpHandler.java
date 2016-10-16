/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.server.handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Protocol.ProtocolException;
import com.zhucode.longio.transport.netty.server.NettyServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class HttpHandler extends AbstractNettyServerHandler {
	
	private static final String WEBSOCKET_PATH = "/";
	
	private static AttributeKey<Boolean> keepAlive = AttributeKey.valueOf("keepAlive");
	
	public HttpHandler(NettyServer server, Protocol protocol) {
		super(server, protocol);
	}
	

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if (msg instanceof FullHttpRequest) {
				handleHttpRequest(ctx, (FullHttpRequest)msg);
			} else if (msg instanceof WebSocketFrame) {
	            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
	        }
		} finally {
			ReferenceCountUtil.release(msg);
		}
	
	}
	
	
	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) throws ProtocolException {
		// Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
        	AttributeKey<WebSocketServerHandshaker> key = AttributeKey.valueOf("WebSocketServerHandshaker");
        	WebSocketServerHandshaker handshaker= ctx.attr(key).get();
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        ByteBuf buf = ((TextWebSocketFrame) frame).content();
      
        this.server.service(this.channelId, protocol, buf);
	}


	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws ProtocolException {
		
		HttpHeaders headers = req.headers();
		
		AttributeKey<Boolean> isWs = AttributeKey.valueOf("isWs");
		
		String upgrade = headers.get("Upgrade");
		String connection = headers.get("Connection");
		
		if (upgrade != null && upgrade.equalsIgnoreCase("websocket") 
				&& connection != null && connection.equalsIgnoreCase("Upgrade")) {
			// Handshake
	        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
	                getWebSocketLocation(req), null, true);
	        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
	        if (handshaker == null) {
	            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
	        } else {
	            handshaker.handshake(ctx.channel(), req);
	            AttributeKey<WebSocketServerHandshaker> key = AttributeKey.valueOf("WebSocketServerHandshaker");
	            ctx.attr(key).set(handshaker);
	        }
	        ctx.attr(isWs).set(true);
		} else {
			ctx.attr(isWs).set(false);
			if (HttpHeaders.is100ContinueExpected(req)) {
				ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
			}
			
			boolean ka = HttpHeaders.isKeepAlive(req);
			
			ctx.attr(keepAlive).set(ka);
			
			ByteBuf buf = req.content();
			
	        this.server.service(this.channelId, protocol, buf);
		}
	}
	
	
	@Override
	public ChannelFuture write(ChannelHandlerContext ctx, byte[] bytes) {
		AttributeKey<Boolean> isWs = AttributeKey.valueOf("isWs");
		
		if (ctx.attr(isWs).get()) {
			return sendForWebSocket(ctx, bytes);
		} else {
			return sendForHttp(ctx, bytes);
		}
	}
	
	
	private ChannelFuture sendForHttp(ChannelHandlerContext ctx, byte[] bytes) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				OK, Unpooled.wrappedBuffer(bytes));
		response.headers().set(CONTENT_TYPE, "text/json; charset=utf-8");
		response.headers().set(CONTENT_LENGTH,
				response.content().readableBytes());
		
		boolean ka = ctx.attr(keepAlive).get();
		
		if (!ka) {
			return ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			response.headers().set(CONNECTION, Values.KEEP_ALIVE);
			return ctx.writeAndFlush(response);
		}
	}
	
	private ChannelFuture sendForWebSocket(ChannelHandlerContext ctx, byte[] bytes) {
		TextWebSocketFrame frame = new TextWebSocketFrame();
		frame.content().writeBytes(bytes);
		return ctx.channel().writeAndFlush(frame);
	}

	private static String getWebSocketLocation(FullHttpRequest req) {
		String location = req.headers().get(HOST) + WEBSOCKET_PATH;
		return "ws://" + location;
	}

}
