/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Request;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年10月15日 下午4:27:45 
 * 
 */
public class NettyConnectionPool {
	
	private String app;
	
	private AppLookup appLookup;

	private List<NettyConnection> pool = new ArrayList<NettyConnection>();

	private TransportType transportType;

	private Protocol protocol;

	private NettyConnectionFactory nettyConnectionFactory;

	private NettyClient client;
	
	private AtomicLong count = new AtomicLong();

	public NettyConnectionPool(NettyClient client, String app, TransportType transportType, Protocol protocol) {
		super();
		this.app = app;
		this.transportType = transportType;
		this.protocol = protocol;
		this.client = client;
		appLookup = client.getAppLookup();
		nettyConnectionFactory = client.getNettyConnectionFactory();
	}
	
	public void initPool() {
		String[] hosts = appLookup.parseHosts(app);
		for (String host : hosts) {
			String[] strs = host.split("#");
			int weight = 1;
			if (strs.length == 2) {
				weight = Integer.parseInt(strs[1]);
			}
			host = strs[0];
			String h = host.split(":")[0];
			int p = Integer.parseInt(host.split(":")[1]);

			NettyConnection point = null;
			if (TransportType.HTTP == transportType) {
				point = nettyConnectionFactory.runOneHttpClient(h, p, client, protocol);
			} else if (TransportType.SOCKET == transportType) {
				point = nettyConnectionFactory.runOneRawSocketClient(h, p, client, protocol);
			}
			point.connect();
			while (weight-- > 0) {
				pool.add(point);
			}
		}

	}

	public NettyConnection getConnection(Request request) {
		int idx = (int)(count.getAndIncrement()) % pool.size();
		return pool.get(idx);
	}

	public void writeRequest(Request request) {
		NettyConnection connection = getConnection(request);
		connection.writeRequest(request);
	}
	
	
}
