/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.message.MessageBlock;
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
	
	private Logger log = LoggerFactory.getLogger("longio.stat");
	
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
	
	private String hostString = "";
	
	public GroupClientCluster(Connector connector, AppLookup lookup, String app, 
			TransportType tt, ProtocolType pt, LoadBalance lb) {
		this.connector = connector;
		this.lookup = lookup;
		this.app = app;
		this.tt = tt;
		this.pt = pt;
		this.bf = new BeginpointFactory();
		this.lb = lb;
		boot();
		startCheck();
	}
	
	
	private void startCheck() {
		new Thread(()->{
			while (true) {
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String[] hosts = this.lookup.parseHosts(app);
				String newHostString = String.join("", hosts);
				if (!hostString.equals(newHostString)) {
					hostString = newHostString;
					connectHosts(hosts);
				}
			}
		}).start();
	}

	private void boot() {
		String[] hosts = this.lookup.parseHosts(app);
		hostString = String.join("", hosts);
		connectHosts(hosts);
	}
	
	private void connectHosts(String[] hosts) {
		List<Beginpoint> points = new ArrayList<Beginpoint>();
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
		this.points = points;
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
	public void sendFail(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "fail");
		log.info(msg.toJSONString());
	}

	@Override
	public void sendTimeout(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "timeout");
		log.info(msg.toJSONString());
	}

	@Override
	public void sendSuccess(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "success");
		log.info(JSON.toJSONString(msg, SerializerFeature.WriteMapNullValue));
	}
	
	private JSONObject getMessageJson(Beginpoint point, MessageBlock<?> mb) {
		JSONObject js = new JSONObject();
		js.put("app", point.getApp());
		js.put("host", point.getHost());
		js.put("port", point.getPort());
		js.put("serial", mb.getSerial());
		js.put("cmd", mb.getCmd());
		if (mb.getBody() == null) {
			js.put("body_type", null);
		} else {
			js.put("body_type",mb.getBody().getClass().getCanonicalName());
		}
		
		js.put("body", mb.getBody());
		
		return js;
	}
	
	public static void main(String[] args) {
		byte[] bs = new byte[10];
		bs[1]=0;
		JSONObject js = new JSONObject();
		js.put("app", bs);
		js.put("body_type", bs.getClass().getCanonicalName());
		
		System.out.println(js.toJSONString());
		
		
	}
}
