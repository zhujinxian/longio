/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client;

import java.util.concurrent.ConcurrentHashMap;

import com.zhucode.longio.message.MessageBlock;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ClientDispatcher {
	
	private ConcurrentHashMap<Long, InvocationTask<MessageBlock<?>>> tasks = new ConcurrentHashMap<Long, InvocationTask<MessageBlock<?>>>();
	
	public void setReturnValue(MessageBlock<?> mb) {
		long serial = mb.getSerial();
		InvocationTask<MessageBlock<?>> task = this.tasks.get(serial);
		if (task != null & !task.isCancelled()) {
			task.set(mb);
			unregist(serial);
		} else {
			System.out.println("maybe timeout message : " + mb.toString());
		}
	}
	
	
	public void registTask(long serial, InvocationTask<MessageBlock<?>> task) {
		this.tasks.put(serial, task);
	}

	public void unregist(long serial) {
		this.tasks.remove(serial);
	}
	
}
