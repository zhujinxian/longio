/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.cluster;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.transport.Beginpoint;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年2月16日
 * 
 */
public class SingleClientCluster implements ClientCluster {
	
	private String ip;
	private int port;
	private TransportType tt;
	private ProtocolType pt;
	private Connector connector;
	
	private Beginpoint point;
	
	public SingleClientCluster(String ip, int port, TransportType tt,
			ProtocolType pt, Connector connector) {
		super();
		this.ip = ip;
		this.port = port;
		this.tt = tt;
		this.pt = pt;
		this.connector = connector;
		boot();
	}

	private void boot() {
		this.point = new Beginpoint(connector, ip + ":" + port, ip, port, tt, pt);
	}


	@Override
	public Beginpoint getNextPoint() {
		return point;
	}

	
	@Override
	public void sendFail(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "fail");
		System.out.println(msg.toJSONString());
	}

	@Override
	public void sendTimeout(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "timeout");
		System.out.println(msg.toJSONString());
	}

	@Override
	public void sendSuccess(Beginpoint point, MessageBlock<?> mb) {
		JSONObject msg = getMessageJson(point, mb);
		msg.put("flag", "success");
		System.out.println(JSON.toJSONString(msg, SerializerFeature.WriteMapNullValue));
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
}
