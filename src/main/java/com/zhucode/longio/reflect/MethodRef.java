/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.exception.LongioException;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MethodRef {
	
	static Logger logger = LoggerFactory.getLogger("com.zhucode.longio");
	
	private int cmd;
	private String name;
	private Object obj;
	private Method method;
	private boolean asy;
	private boolean reply;
	
	public MethodRef(int cmd, String name, Object obj, Method method, boolean asy, boolean reply) {
		this.cmd = cmd;
		this.name = name;
		this.obj = obj;
		this.method = method;
		this.asy = asy;
		this.reply = reply;
	}

	public Object handle(Object[] args) throws LongioException {
		try {
			logger.debug("invoke [{}] with args {}", name, Arrays.asList(args));
			return method.invoke(obj, args);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new LongioException(400, "invoke arguments exception");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			if (e.getCause() instanceof LongioException) {
				throw (LongioException)e.getCause();
			} else {
				throw new LongioException(500, e.getCause().getClass().getCanonicalName());
			}
		}
		return null;
	}
	
	public int getCmd() {
		return cmd;
	}
	public void setCmd(int cmd) {
		this.cmd = cmd;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}

	public boolean isAsy() {
		return asy;
	}

	public void setAsy(boolean asy) {
		this.asy = asy;
	}

	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}
}
