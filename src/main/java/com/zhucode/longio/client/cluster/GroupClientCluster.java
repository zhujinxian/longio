/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.transport.Beginpoint;
import com.zhucode.longio.transport.BeginpointFactory;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年1月28日
 * 
 */
public class GroupClientCluster implements ClientCluster {
	private AppLookup lookup;
	private String app;
	private TransportType tt;
	private ProtocolType pt;
	private Connector connector;
	private List<Beginpoint> points = new ArrayList<Beginpoint>();
	private BeginpointFactory bf;
	private AtomicInteger pos = new AtomicInteger(0);
	private Random rd = new Random();
	private LoadBalance lb;
	
	public GroupClientCluster(Connector connector, AppLookup lookup, String app, TransportType tt, ProtocolType pt, LoadBalance lb) {
		this.connector = connector;
		this.lookup = lookup;
		this.app = app;
		this.tt = tt;
		this.pt = pt;
		this.bf = new BeginpointFactory();
		this.lb = lb;
		boot();
	}
	
	public void boot() {
		String[] hosts = this.lookup.parseHosts(app);
		for (String host : hosts) {
			String[] strs = host.split("#");
			int weight = 1;
			if (strs.length == 2) {
				weight = Integer.parseInt(strs[1]);
			}
			host = strs[0];
			Beginpoint point = bf.getPoint(connector, app, host, tt, pt);
			while (weight-- > 0) {
				points.add(point);
			}
		}
	}
	
	private Beginpoint rollOneBeginpoint() {
		List<Beginpoint> local = this.points;
		int ps = pos.getAndAdd(1)%local.size();
		return local.get(ps);
	}
	
	private  Beginpoint randomOneBeginpoint() {
		List<Beginpoint> local = this.points;
		int ps = rd.nextInt(local.size());
		return local.get(ps);
	}
	
	@Override
	public Beginpoint getNextPoint() {
		if (lb == LoadBalance.Roll) {
			return rollOneBeginpoint();
		}
		if (lb == LoadBalance.Random) {
			return randomOneBeginpoint();
		}
		return null;
	}

	@Override
	public void sendFail(Beginpoint point) {
		
	}

	@Override
	public void sendTimeout(Beginpoint point) {
		
	}

	@Override
	public void sendSuccess(Beginpoint point) {
		System.out.println("invoke success: " + point);
	}
	
}
