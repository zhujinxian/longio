/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.transport.Client;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class NettyClient implements Client{
	
	private NioEventLoopGroup workerGroup;
	private String host;
	private int port;
	private ChannelInitializer<SocketChannel> initializer;
	private NettyConnector connector;
	private Channel channel;
	private boolean connected;
	
	public NettyClient(NettyConnector connector, String host, int port,
			AbstarctClientChannelInitializer initializer) {
		super();
		this.connector = connector;
		this.workerGroup = connector.getWorkerGroup();
		this.host = host;
		this.port = port;
		this.initializer = initializer;
		initializer.client = this;
	}
	
	@Override
	public void connect() {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(initializer);
		ChannelFuture f = null;
		try {
			f = b.connect(host, port).addListener(new ConnectionListener(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.channel = f.channel();
	}
	
	
	
	@Override
	public void send(MessageBlock<?> mb) {
		long sid = channel.attr(NettyConnector.sessionKey).get();
		mb.setSessionId(sid);
		ChannelFuture f = this.connector.send(mb);
		f.addListener(new ChannelFutureListener() {
	         public void operationComplete(ChannelFuture future) throws Exception {
	        	 if (future.isCancelled() || future.cause() != null) {
	        		 throw new Exception("service maybe unavailable");
	        	 }
	         }
	     });
		
	}

	
	
	@Override
	public boolean isConnected() {
		return this.connected;
	}


	@Override
	public void setConnected(boolean b) {
		this.connected = b;
		
		System.out.println(this + "" +  System.currentTimeMillis() + " netty client set connected " + this.isConnected());
	}
}
