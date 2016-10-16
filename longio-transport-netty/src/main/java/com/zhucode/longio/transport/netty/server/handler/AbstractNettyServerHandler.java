/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.server.handler;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.transport.netty.handler.AbstractNettyHandler;
import com.zhucode.longio.transport.netty.server.NettyServer;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhu jinxian
 * @date  2016年9月15日 下午9:09:28 
 * 
 */
public abstract class AbstractNettyServerHandler extends AbstractNettyHandler {
	
	protected NettyServer server;


	public AbstractNettyServerHandler(NettyServer server, Protocol protocol) {
		super(protocol);
		this.server = server;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.attr(handlerKey).set(this);
		this.channelId = this.server.registHandlerContext(ctx);

	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.server.unregistHandlerContext(ctx);
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		this.server.unregistHandlerContext(ctx);
		ctx.close();
	}


}
