/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport;

import com.zhucode.longio.client.ClientDispatcher;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class Beginpoint  {
	
	private String host;
	private int port;
	private Connector connector;
	private ClientDispatcher dispatcher;
	private ProtocolType pt;
	private TransportType tt;
	private long sessionId;
	
	public Beginpoint(Connector connector, String host, int port, TransportType tt, ProtocolType pt) {
		this.host = host;
		this.port = port;
		this.connector = connector;
		this.dispatcher = connector.getClientDispatcher();
		this.pt = pt;
		this.tt = tt;
	}
	
	public long connect() throws Exception {
		this.sessionId = this.connector.connect(host, port, tt, pt);
		return this.sessionId;
	}

	public Connector getConnector() {
		return this.connector;
	}

	public ClientDispatcher getClientDispatcher() {
		return this.dispatcher;
	}

	public long getSessionId() {
		return this.sessionId;
	}

}
