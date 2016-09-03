/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.boot;

import com.zhucode.longio.Request;
import com.zhucode.longio.Response;
import com.zhucode.longio.core.client.CallbackFutureRouter;
import com.zhucode.longio.core.transport.ProtocolType;
import com.zhucode.longio.core.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public abstract class ClientHandler {
	
	private CallbackFutureRouter router;
	
	protected TransportType transportType;
	
	protected ProtocolType protocolType;
	
	public ClientHandler(CallbackFutureRouter router, TransportType transportType, ProtocolType protocolType) {
		super();
		this.router = router;
		this.transportType = transportType;
		this.protocolType = protocolType;
	}

	public void handleresponse(Response response) {
		router.route(response);
	}
	
	public abstract void writeRequest(Request request);
	
}
