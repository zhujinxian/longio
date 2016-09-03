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

import com.zhucode.longio.Callback;
import com.zhucode.longio.Request;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class MethodInvocationHandler implements InvocationHandler {
	
	private int cmd;
	
	private float version;
	
	private int timeout;
	
	private ServiceHandler service;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Request request = service.createRequest(method, args);
		request.setCmd(cmd);
		request.setVersion(version);	
		
		int uid = UidParser.parseUid(method, args);
		int cmd = CMDParser.parseCMD(method, args);
		
		if (uid > 0) {
			request.setUid(uid);
		}
		
		if (cmd > 0) {
			request.setCmd(cmd);
		}

		
		Callback callback = null;
		if (args.length > 0 && args[args.length-1] instanceof Callback) {
			callback = (Callback)args[args.length-1];
		}
		
		return service.doInvoke(request, method, callback, timeout);
	}
}
