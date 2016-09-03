/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.zhucode.longio.Callback;
import com.zhucode.longio.LongioException;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.Protocol.SerializeException;
import com.zhucode.longio.Request;
import com.zhucode.longio.Response;
import com.zhucode.longio.Response.Status;
import com.zhucode.longio.boot.ClientHandler;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class ServiceHandler {
	
	private ClientHandler clientHandler;
	
	private CallbackFutureRouter router;
	
	private Protocol protocol;
	
	private AtomicLong serialCount = new AtomicLong();
	
	public Request createRequest(Method method, Object... args) throws SerializeException {
		Object body = protocol.serializeParameters(method, args);
		Request request = new Request();
		long serial = produceSerial();
		request.setSerial(serial);
		request.setBody(body);
		return request;
	}
	
	private long produceSerial() {
		long count = serialCount.incrementAndGet();
		return hashCode() << 32 | count;
	}

	public Object doInvoke(Request request, Method method, Callback callback, int timeout) throws Exception {
		CompletableFuture<Response> future = sendRequest(request);
		
		if (callback != null) {
			router.registerCallback(request.getSerial(), callback, timeout);
			return null;
		} 
		long serial = request.getSerial();
		router.registerFuture(serial, future);
		Response response = null;
		try {
			response = future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			router.timeoutFuture(serial);
			throw ex;
		}
		
		Status status = Status.valueOf(response.getStatus());
		
		if (Status.OK == status) {
			return protocol.deserializeReturnValue(method, response.getBody());
		} 
		
		throw new LongioException(status.value(), response.getErr());
	}
	
	private CompletableFuture<Response> sendRequest(Request request) {
		clientHandler.writeRequest(request);
		return new CompletableFuture<Response>();
	}

}
