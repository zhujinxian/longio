/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhu jinxian
 * @date  2016年1月29日
 * 
 */
public class BeginpointFactory {
	
	private static ConcurrentHashMap<String, Beginpoint> points = new ConcurrentHashMap<String, Beginpoint>();
	
	public BeginpointFactory() {
		
	}
	
	public Beginpoint getPoint(Connector connector, String app, String host,  TransportType tt, ProtocolType pt) {
		
		Beginpoint point = points.get(host);
		
		if (point == null) {
			point = creatPoint(connector, app, host, tt, pt);
		}
		return point;
	
	}
	private static synchronized Beginpoint creatPoint(Connector connector, String app, String host,
			TransportType tt, ProtocolType pt) {
		String[] strs = host.split(":");
		String ip = strs[0];
		int port = Integer.parseInt(strs[1]);
		Beginpoint point = new Beginpoint(connector, app, ip, port, tt, pt);
		points.put(host, point);
		return point;
	}

}
