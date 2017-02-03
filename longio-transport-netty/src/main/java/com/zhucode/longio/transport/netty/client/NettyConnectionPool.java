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
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;
import com.zhucode.longio.App;
import com.zhucode.longio.LoadBalance;
import com.zhucode.longio.Request;
import com.zhucode.longio.core.client.lb.RandomLoadBalance;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年10月15日 下午4:27:45 
 * 
 */
public class NettyConnectionPool {
	
	private String appName;
	
	private AppLookup appLookup;
	
	private LoadBalance lb;

	private volatile List<App> pool = new ArrayList<App>();
	
	private Map<App, NettyConnection> connectionMap = Maps.newConcurrentMap();

	private NettyConnectionFactory nettyConnectionFactory;

	private NettyClient client;
	
	private AtomicLong count = new AtomicLong();

	public NettyConnectionPool(NettyClient client, String app) {
		super();
		this.appName = app;
		this.client = client;
		appLookup = client.getAppLookup();
		lb = client.getLoadBalance(app);
		if (lb == null) {
			lb = new RandomLoadBalance();
		}
		nettyConnectionFactory = client.getNettyConnectionFactory();
	}
	
	public void initPool() {
		this.adjustPool();
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				adjustPool();
			}
			
		}, 1, 1, TimeUnit.MINUTES);
	}
	
	private NettyConnection createConnection(App app) {
		NettyConnection connection = null;
		if (TransportType.HTTP == app.getTransportType()) {
			connection = nettyConnectionFactory.runOneHttpClient(client, app.getHost(), app.getPort(), app.getProtocol());
		} else if (TransportType.SOCKET == app.getTransportType()) {
			connection = nettyConnectionFactory.runOneRawSocketClient(client, app.getHost(), app.getPort(), app.getProtocol());
		}
		return connection;

	}

	public NettyConnection getConnection(Request request) {
		
		App app = this.lb.select(request, pool);
		
		return this.connectionMap.get(app);

	}

	public boolean writeRequest(Request request) {
		NettyConnection connection = getConnection(request);
		if (connection == null) {
			return false;
		}
		return connection.writeRequest(request);
	}
	
	public void adjustPool() {
		List<App> apps = appLookup.discovery(appName);
		for (App app : apps) {
			NettyConnection connection = this.connectionMap.get(app);
			if (connection == null) {
				connection = createConnection(app);
				connection.connect();
			}
			connectionMap.put(app, connection);
		}
		this.pool = apps;
		List<App> delayClosedApps = new ArrayList<App>();
		for (App app : connectionMap.keySet()) {
			if (!this.pool.contains(app)) {
				delayClosedApps.add(app);
			}
		}
		delayClose(delayClosedApps);

	}
	
	
		
	private void delayClose(final List<App> delayClosedApps) {
		Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
			@Override
			public void run() {
				for (App app : delayClosedApps) {
					if (pool.contains(app)) {
						continue;
					}
					NettyConnection connection = connectionMap.remove(app);
					if (connection != null) {
						connection.close();
					}
				}
			}
			
		}, 5, TimeUnit.MINUTES);
	}

	
}
