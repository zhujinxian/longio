/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Map;

import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.Dispatcher;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.protocol.ProtocolParser;
import com.zhucode.longio.transport.AbstractHandler;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.netty.event.PingEvent;
import com.zhucode.longio.transport.netty.event.PongEvent;



/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public abstract class AbstractNettyHandler extends AbstractHandler implements ChannelHandler, ChannelInboundHandler {
	
	protected AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
	
	
	protected long sessionId;

	public AbstractNettyHandler(Connector connector, Dispatcher dispatcher, CallbackDispatcher callbackDispatcher,ProtocolParser<?> pp) {
		super(connector, dispatcher, callbackDispatcher, pp);
	}
	
	/**
	 *  code from netty
	 *  
	 * @return
	 */
	public boolean isSharable() {
		Class<?> clazz = getClass();
		Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get()
				.handlerSharableCache();
		Boolean sharable = cache.get(clazz);
		if (sharable == null) {
			sharable = clazz.isAnnotationPresent(Sharable.class);
			cache.put(clazz, sharable);
		}
		return sharable;
	}
	
	/**
	 *  code from netty
	 */
	
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    
    /**
     * code from netty
     * 
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.attr(handlerKey).set(this);
		this.sessionId = this.getNettyConnector().registHandlerContext(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.getNettyConnector().unregistHandlerContext(ctx);
		ctx.close();
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelReadComplete();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(pp.getHeartBeat());
            }
        }
		
		if (evt instanceof PingEvent) {
			ctx.writeAndFlush(pp.getHeartBeat());
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		ctx.fireChannelWritabilityChanged();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("-------ebcounter exception-------");
		cause.printStackTrace();
		this.getNettyConnector().unregistHandlerContext(ctx);
		ctx.close();
	}
	
	protected NettyConnector getNettyConnector() {
		return (NettyConnector)this.connector;
	}
	
	@Override
	protected void process(ChannelHandlerContext ctx, ByteBuf buf) throws ProtocolException {

		byte[] bytes = new byte[buf.readableBytes()];
		
		buf.readBytes(bytes);
		
		MessageBlock<?> mb = pp.decode(bytes);
		
		if (mb.getCmd() == 0) {
			ctx.fireUserEventTriggered(new PongEvent());
			return;
		}
		
		mb.setConnector(this.connector);
		mb.setSessionId(ctx.channel().attr(NettyConnector.sessionKey).get());
		mb.setLocalAddress(ctx.channel().localAddress());
		mb.setRemoteAddress(ctx.channel().remoteAddress());
		
		this.dispatcher.dispatch(mb);
	}

	
	public long getSessionId() {
		return sessionId;
	}

	abstract ChannelFuture sendMessage(ChannelHandlerContext ctx, MessageBlock<?> message);
}
