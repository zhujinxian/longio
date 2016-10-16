/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.handler;

import java.util.Map;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.transport.netty.event.PingEvent;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.internal.InternalThreadLocalMap;



/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public abstract class AbstractNettyHandler implements ChannelHandler, ChannelInboundHandler {
	
	protected AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
		
	protected long channelId;
	
	protected Protocol protocol;
	
	public AbstractNettyHandler(Protocol protocol) {
		this.protocol = protocol;
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
                ctx.writeAndFlush(protocol.getHeartBeat());
            }
        }
		
		if (evt instanceof PingEvent) {
			ctx.writeAndFlush(protocol.getHeartBeat());
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		ctx.fireChannelWritabilityChanged();
	}
	
	

	public abstract ChannelFuture write(ChannelHandlerContext ctx, byte[] bytes);
}
