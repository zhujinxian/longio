/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.MessageCallback;

/**
 * @author zhu jinxian
 * @date  2016年1月15日
 * 
 */
public class CallbackDispatcher {
	
	static Logger log = LoggerFactory.getLogger(CallbackDispatcher.class);
	
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	private static ScheduledExecutorService scheduledexecutor = Executors.newScheduledThreadPool(1);
	
	
	private ConcurrentHashMap<Long, InvocationTask<MessageBlock<?>>> tasks = new ConcurrentHashMap<Long, InvocationTask<MessageBlock<?>>>();
	
	private ConcurrentHashMap<Long, MessageCallback> callbacks = new ConcurrentHashMap<Long,MessageCallback>();

	
	public void setReturnValue(MessageBlock<?> mb) {
		long serial = mb.getSerial();
		InvocationTask<MessageBlock<?>> task = this.tasks.get(serial);
		if (task != null && !task.isCancelled()) {
			task.set(mb);
			unregist(serial);
		} else {
			MessageCallback callback = callbacks.remove(serial);
			if (callback != null) {
				executor.submit(() -> {callback.callback(mb);});
			} else {
				System.out.println("maybe timeout message : " + mb.toString());
			}
		}
	}
	
	
	public void registTask(long serial, InvocationTask<MessageBlock<?>> task) {
		this.tasks.put(serial, task);
		executor.submit(task);
	}
	
	public void registCallback(long serial, InvocationTask<MessageBlock<?>> task, MessageCallback callback, int timeout) {
		this.callbacks.put(serial, callback);
		executor.submit(task);
		scheduledexecutor.schedule(() -> {
			if (callbacks.remove(serial) != null) {
				log.error("callback timeout with serial {{}}", serial);
			}
		}, timeout, TimeUnit.MILLISECONDS);
	}
	

	public void unregist(long serial) {
		this.tasks.remove(serial);
	}
}
