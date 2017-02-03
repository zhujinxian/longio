/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.zhucode.longio.LoadBalance;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.Request;
import com.zhucode.longio.boot.ClientHandler;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.server.ResponseWrapper;
import com.zhucode.longio.core.transport.TransportType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @author zhu jinxian
 * @date  2016年9月15日 下午3:47:48 
 * 
 */
public class NettyClient extends ClientHandler {
	
	protected AttributeKey<Long> channelKey = AttributeKey.valueOf("channelId");
	protected AttributeKey<ChannelHandlerContext> ctxKey = AttributeKey.valueOf("ctx");

	private AtomicLong channeId = new AtomicLong(1000000);
	private ConcurrentHashMap<String, NettyConnectionPool> connectionPool = new ConcurrentHashMap<>();

	private NettyConnectionFactory nettyConnectionFactory;

	private AppLookup appLookup;
	
	private Map<String, LoadBalance> lbs;

	public NettyClient(AppLookup appLookup, Map<String, LoadBalance> lbMap, NettyConnectionFactory nettyConnectionFactory) {
		this.nettyConnectionFactory = nettyConnectionFactory;
		this.appLookup = appLookup;
		this.lbs = lbMap;
	}

	@Override
	public boolean writeRequest(String app, Request request) {
		NettyConnectionPool pool = connectionPool.get(app);
		return pool.writeRequest(request);
	}
		
	@Override
	public void connect(String app) {
		NettyConnectionPool pool = new NettyConnectionPool(this, app);
		pool.initPool();
		connectionPool.put(app, pool);
	}

	public long registHandlerContext(ChannelHandlerContext ctx) {
		long cid = channeId.addAndGet(1);
		ctx.channel().attr(channelKey).set(cid);
		ctx.channel().attr(ctxKey).set(ctx);
		return cid;
	}
	
	public void unregistHandlerContext(ChannelHandlerContext ctx) {
		ctx.channel().remoteAddress();
	}

	public AppLookup getAppLookup() {
		return appLookup;
	}
	
	public LoadBalance getLoadBalance(String app) {
		if (this.lbs == null) {
			return null;
		}
		return this.lbs.get(app);
	}


	public NettyConnectionFactory getNettyConnectionFactory() {
		return nettyConnectionFactory;
	}

	public void processMessage(Protocol protocol, ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		ResponseWrapper response = new ResponseWrapper();
		protocol.decodeResponse(response, bytes);
		response.setProtocol(protocol);
		this.handleResponse(response);
	}
	
}
