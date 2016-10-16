/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client;

import java.lang.reflect.Method;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class RpcMethodInfo {
	private int cmd;
	private String name;
	private Class<?> cls;
	private Method method;
	private boolean asy;
	private int timeout;
	private float version;
	
	public RpcMethodInfo(int cmd, String name, float version, Class<?> cls, Method method, boolean asy, int timeout) {
		super();
		this.cmd = cmd;
		this.name = name;
		this.cls = cls;
		this.method = method;
		this.asy = asy;
		this.timeout = timeout;
		this.version = version;
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

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public float getVersion() {
		return version;
	}
	
	
	
}
