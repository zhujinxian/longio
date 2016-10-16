/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
public class MethodInvocationHandler implements InvocationHandler {
	
	private String app;
		
	private ClientHandler clientHandler;

	private Protocol protocol;
		
	private static AtomicLong serialCount = new AtomicLong();
	
	private Map<Method, RpcMethodInfo> rpcMap = new HashMap<>();

	public MethodInvocationHandler(String app, Map<Method, RpcMethodInfo> rpcMap, ClientHandler clientHandler, Protocol protocol) {
		super();
		this.app = app;
		this.rpcMap = rpcMap;
		this.clientHandler = clientHandler;
		this.protocol = protocol;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcMethodInfo rpc = rpcMap.get(method);
		Request request = createRequest(method, args);
		request.setCmd(rpc.getCmd());
		request.setVersion(rpc.getVersion());	
		
		int uid = UidParser.parseUid(method, args);
		int cmd = CMDParser.parseCMD(method, args);
		
		if (uid > 0) {
			request.setUid(uid);
		}
		
		if (cmd > 0) {
			request.setCmd(cmd);
		}

		Callback callback = null;
		if (args != null && args.length > 0 && args[args.length-1] instanceof Callback) {
			callback = (Callback)args[args.length-1];
		}
		
		return doInvoke(request, method, callback, rpc.getTimeout());
	}

	private Request createRequest(Method method, Object... args) throws SerializeException {
		Object body = protocol.serializeParameters(method, args);
		Request request = new Request();
		long serial = produceSerial();
		request.setSerial(serial);
		request.setBody(body);
		return request;
	}
	
	private CompletableFuture<Response> sendRequest(Request request) {
		clientHandler.writeRequest(app, request);
		return new CompletableFuture<Response>();
	}

	
	private long produceSerial() {
		long count = serialCount.incrementAndGet();
		return hashCode() << 32 | count;
	}

	
	private Object doInvoke(Request request, Method method, Callback callback, int timeout) throws Exception {
		CompletableFuture<Response> future = sendRequest(request);
		 CallbackFutureRouter router = this.clientHandler.getRouter();
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
	

}
