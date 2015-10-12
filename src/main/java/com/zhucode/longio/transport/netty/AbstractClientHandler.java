/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

import com.zhucode.longio.protocol.ProtocolParser;
import com.zhucode.longio.transport.Client;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public abstract class AbstractClientHandler extends AbstractNettyHandler {

	protected Client client;
	
	public AbstractClientHandler(Client client, Connector connector, ProtocolParser<?> pp) {
		super(connector, null, pp);
		this.client = client;
	}

	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		final EventLoop loop = ctx.channel().eventLoop();
		loop.schedule(new Runnable() {
			@Override
			public void run() {
				client.connect();
			}
		}, 1L, TimeUnit.SECONDS);
		
		super.channelInactive(ctx);
	}
	
}
