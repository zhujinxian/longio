/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.Callback;
import com.zhucode.longio.Response;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class CallbackFutureRouter {
	
	private Logger logger = LoggerFactory.getLogger(CallbackFutureRouter.class);
	
	private ConcurrentHashMap<Long, CompletableFuture<Response>> routeMap = new ConcurrentHashMap<Long, CompletableFuture<Response>>();
	private ConcurrentHashMap<Long, Callback> callbackMap = new ConcurrentHashMap<Long, Callback>();
	
	private static ScheduledExecutorService timeoutChecker = Executors.newScheduledThreadPool(1);
	
	public void route(Response response) {
		long serial = response.getSerial();
		CompletableFuture<Response> future = routeMap.get(serial);
		if (future != null) {
			future.complete(response);
		} else {
			Callback callback = callbackMap.get(serial);
			if (callback != null) {
				callback.callback(response);
			}
		}
	}
	
	public void registerFuture(long serial, CompletableFuture<Response> future) {
		routeMap.put(serial, future);
	}

	public void registerCallback(long serial, Callback callback, int timeout) {
		callbackMap.put(serial, callback);
		timeoutChecker.schedule(() -> {
			if (callbackMap.remove(serial) != null) {
				logger.error("callback timeout with serial [{}]", serial);
			}

		}, timeout, TimeUnit.MILLISECONDS);
	}

	public void timeoutFuture(long serial) {
		routeMap.remove(serial);
	}
	
}
