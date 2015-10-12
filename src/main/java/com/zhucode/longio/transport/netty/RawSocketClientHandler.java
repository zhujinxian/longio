/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Future;

import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.protocol.ProtocolParser;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class RawSocketClientHandler extends AbstractNettyHandler {

	public RawSocketClientHandler(Connector connector, ProtocolParser<?> pp) {
		super(connector, null, pp);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.attr(handlerKey).set(this);
		this.sessionId = this.getNettyConnector().registHandlerContext(ctx);
		System.out.println("--------------connect----------------");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message)
			throws Exception {
		ByteBuf buf = (ByteBuf)message;
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		
		try {
			MessageBlock<?> mb = pp.decode(bytes);
			this.connector.getClientDispatcher().setReturnValue(mb);
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
	}

	@Override
	Future<?> sendMessage(ChannelHandlerContext ctx, MessageBlock<?> mb) {
		byte[] bytes = new byte[0];
		try {
			bytes = pp.encode(mb);
			
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		return ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(bytes));
	}
}
