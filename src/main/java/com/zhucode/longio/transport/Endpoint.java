/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport;

import com.zhucode.longio.message.Dispatcher;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class Endpoint {
	
	private String pkg;
	private int port;
	private TransportType tt;
	private ProtocolType pt;
	private Dispatcher dispatcher;
	private Connector connector;

	public Endpoint(String pkg, int port, TransportType tt, ProtocolType pt) {
		super();
		this.pkg = pkg;
		this.port = port;
		this.tt = tt;
		this.pt = pt;
	}
	public String getPkg() {
		return pkg;
	}
	public int getPort() {
		return port;
	}
	public TransportType getTt() {
		return tt;
	}
	public ProtocolType getPt() {
		return pt;
	}
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	public Dispatcher getDispatcher() {
		return dispatcher;
	}
	public Connector getConnector() {
		return connector;
	}
	public void setConnector(Connector connector) {
		this.connector = connector;
	}
}
