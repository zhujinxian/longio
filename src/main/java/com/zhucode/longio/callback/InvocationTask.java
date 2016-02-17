/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.callback;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.zhucode.longio.client.reflect.InvokeCall;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class InvocationTask<V> extends FutureTask<V> {

	private Callable<V> callable;
	
	public InvocationTask(Callable<V> callable) {
		super(callable);
		this.callable = callable;
	}

	@Override
	public void set(V v) {
		super.set(v);
	}
	
	
	@Override
	public void run() {
		try {
			this.callable.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public Callable<V> getCallable() {
		return this.callable;
	}
}
