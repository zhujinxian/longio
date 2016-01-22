/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.context.parameter.ParameterParserFactory;
import com.zhucode.longio.reflect.MethodRef;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MethodDispatcher implements Dispatcher {
	
	static Logger logger = LoggerFactory.getLogger(MethodDispatcher.class);
	
	private ExecutorService es = Executors.newCachedThreadPool();
	
	private Map<Integer, MethodRef> invokers = new HashMap<Integer, MethodRef>();
	
	private List<MessageFilter> filters;
	
	private ParameterParserFactory parameterParserFactory;
	
	AtomicLong num = new AtomicLong();
	
	public MethodDispatcher() {
		parameterParserFactory = new ParameterParserFactory();
	}
	
	@Override
	public void registerMethodRefs(List<MethodRef> refs) {
		for (MethodRef ref : refs) {
			this.invokers.put(ref.getCmd(), ref);
		}
	}
	
	@Override
	public void dispatch(MessageBlock<?> mb) {
		int cmd = mb.getCmd();
		MethodRef mih = invokers.get(cmd);
		MessageProcessTask mpt = new MessageProcessTask(mb, mih, parameterParserFactory);
		mpt.setFilters(filters);
		if (mih == null || mih.isAsy()) {
			this.es.submit(mpt);
		} else {
			mpt.run();
		}
		logger.info("invoke num = " + num.getAndIncrement());
	}

	public ParameterParserFactory getParameterParserFactory() {
		return parameterParserFactory;
	}

	
	@Override
	public void registerMessageFilters(List<MessageFilter> filters) {
		this.filters = filters;
	}
	
	
}
