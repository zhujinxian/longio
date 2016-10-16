/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Protocol.SerializeException;
import com.zhucode.longio.Response.Status;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class MethodHandler {
	
	private Object target;
	
	private Method method;

	private int cmd;

	private String cmdName;

	private boolean asy;

	private boolean reply;
	
	public MethodHandler(int cmd, String cmdName, Object obj, Method m, boolean asy, boolean reply) {
		this.cmd = cmd;
		this.cmdName = cmdName;
		this.target = obj;
		this.method = m;
		this.asy = asy;
		this.reply = reply;
				
	}
	
	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	public int getCmd() {
		return cmd;
	}
	
	public String getCmdName() {
		return cmdName;
	}

	public boolean isAsy() {
		return asy;
	}

	public boolean isReply() {
		return reply;
	}


	public void handle(RequestWrapper request, ResponseWrapper response) {
		Protocol protocol = request.getProtocol();
		try {
			Object[] args = protocol.deserializeParameters(method, request, response);
			Object ret = method.invoke(target, args);
			ret = protocol.serializeReturnValue(method, ret);
			response.setBody(ret);
			response.setStatus(Status.OK.value());
		} catch (SerializeException e) {
			response.setStatus(Status.SERVER_ERR.value());
			response.setErr(e.getMessage());
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERR.value());
			response.setErr(e.getMessage());
		} 
	}
	
}
