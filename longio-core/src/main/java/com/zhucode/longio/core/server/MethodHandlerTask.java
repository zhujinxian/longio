/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.server;

import java.util.ArrayList;
import java.util.List;

import com.zhucode.longio.Response.Status;

/**
 * @author zhu jinxian
 * @date  2016年8月14日 下午6:08:01 
 * 
 */
public class MethodHandlerTask implements Runnable {;
	
	private MethodHandler handler;
	private List<HandlerInterceptor> inteceptors;
	private RequestWrapper request;
	private ResponseWrapper response;
	

	public MethodHandlerTask(MethodHandler handler, List<HandlerInterceptor> inteceptors, RequestWrapper requestWrapper,
			ResponseWrapper responseWrapper) {
		this.handler = handler;
		this.inteceptors = inteceptors;
		this.request = requestWrapper;
		this.response = responseWrapper;
		if (this.inteceptors == null) {
			this.inteceptors = new ArrayList<HandlerInterceptor>();
		}
	}

	@Override
	public void run() {
		
		if (handler == null) {
			response.setStatus(Status.Notfound.value());
			response.setErr("invoke method not found");
			response.getServerHandler().write(response);
			return;
		}
		
		for (HandlerInterceptor interceptor : inteceptors) {
			try {
				boolean flag = interceptor.preHandle(request, response, handler);
				if (!flag) return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		handler.handle(request, response);
		
		for (HandlerInterceptor interceptor : inteceptors) {
			try {
				interceptor.postHandle(request, response, handler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		if (handler.isReply()) {
			response.getServerHandler().write(response);
		}
		
		for (HandlerInterceptor interceptor : inteceptors) {
			try {
				interceptor.afterCompletion(request, response, handler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
