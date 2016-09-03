/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.boot;

import java.util.concurrent.Future;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Protocol.ProtocolException;
import com.zhucode.longio.Response;
import com.zhucode.longio.core.server.MethodRouter;
import com.zhucode.longio.core.server.RequestWrapper;
import com.zhucode.longio.core.server.ResponseWrapper;
import com.zhucode.longio.core.transport.ProtocolType;
import com.zhucode.longio.core.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public abstract class ServerHandler {
		
	private MethodRouter router;
	
	protected int port;
	
	protected TransportType transportType;
	
	protected Protocol protocol;

	protected String host;


	public ServerHandler(MethodRouter router, String host, int port, TransportType transportType, Protocol protocol) {
		super();
		this.router = router;
		this.host = host;
		this.port = port;
		this.transportType = transportType;
		this.protocol = protocol;
	}



	protected void service(RequestWrapper request, ResponseWrapper response) throws ProtocolException {		
		router.route(request, response);
	}
	
	public abstract Future<Void> write(ResponseWrapper response);
	
	
	public abstract void start();
	
}
