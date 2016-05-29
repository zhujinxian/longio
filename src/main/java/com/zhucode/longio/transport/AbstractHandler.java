/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport;

import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.Dispatcher;
import com.zhucode.longio.protocol.Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public abstract class AbstractHandler {

	protected Connector connector;
	protected Dispatcher dispatcher;
	protected CallbackDispatcher callbackDispatcher;
	
	protected Protocol pp;
	
	public AbstractHandler(Connector connector, Dispatcher dispatcher, CallbackDispatcher callbackDispatcher, Protocol pp) {
		super();
		this.connector = connector;
		this.dispatcher = dispatcher;
		this.pp = pp;
	}


	abstract protected void process(ChannelHandlerContext ctx, ByteBuf buf) throws ProtocolException;
}
