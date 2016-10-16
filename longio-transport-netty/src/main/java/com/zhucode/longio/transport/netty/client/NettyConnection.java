/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Request;
import com.zhucode.longio.transport.netty.handler.AbstractNettyHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

/**
 * @author zhu jinxian
 * @date  2016年9月15日 下午4:36:36 
 * 
 */
public class NettyConnection {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String host;
	
	private int port;
		
	private ChannelInitializer<SocketChannel> initializer;

	private Channel channel;

	private boolean connected;
	
	private NioEventLoopGroup eventLoop;
	
	private Protocol protocol;
	
	private static AttributeKey<Long> channelKey = AttributeKey.valueOf("channelId");
	protected AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
	protected AttributeKey<ChannelHandlerContext> ctxKey = AttributeKey.valueOf("ctx");

	
	public NettyConnection(String host, int port, Protocol protocol, NioEventLoopGroup eventLoop, ChannelInitializer<SocketChannel> initializer) {
		super();
		this.host = host;
		this.port = port;
		this.initializer = initializer;
		this.eventLoop = eventLoop;
		this.protocol = protocol;
	}


	public void connect() {
		Bootstrap b = new Bootstrap();
		b.group(eventLoop);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(initializer);
		ChannelFuture f = null;
		try {
			logger.debug("connecting to [{}:{}]", host, port);
			f = b.connect(host, port).addListener(new ConnectionListener(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.channel = f.channel();
	}

	public long getChannelId() {
		return this.channel.attr(channelKey).get();
	}
	
	public boolean isConnected() {
		return this.connected;
	}

	public void setConnected(boolean b) {
		this.connected = b;
		logger.debug("connect to server [{}:{}] {}", host, port, b);
	}

	public void writeRequest(Request request) {
		ChannelHandlerContext ctx = this.channel.attr(ctxKey).get();
		AbstractNettyHandler handler = ctx.attr(handlerKey).get();
		byte[] bytes = protocol.encodeRequest(request);
		handler.write(ctx, bytes);
	}
	
}
