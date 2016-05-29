/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport;

import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.message.MessageBlock;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class Beginpoint  {
	
	private String app;
	private String host;
	private int port;
	private Connector connector;
	private CallbackDispatcher dispatcher;
	private ProtocolType pt;
	private TransportType tt;
	private long sessionId;
	private Client client;
	
	
	public Beginpoint(Connector connector, String app,  String host, int port, TransportType tt, ProtocolType pt) {
		this.app = app;
		this.host = host;
		this.port = port;
		this.connector = connector;
		this.dispatcher = connector.getCallbackDispatcher();
		this.pt = pt;
		this.tt = tt;
		init();
	}
	
	
	private void init() {
		try {
			client =  this.connector.createClient(host, port, tt, pt);
			client.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(MessageBlock mb) throws Exception {
		client.send(mb);
	}

	public Connector getConnector() {
		return this.connector;
	}

	public CallbackDispatcher getClientDispatcher() {
		return this.dispatcher;
	}

	public long getSessionId() {
		return this.sessionId;
	}


	public String getApp() {
		return app;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}
}
