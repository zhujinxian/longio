/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.message;

import java.lang.reflect.Parameter;

import com.zhucode.longio.context.parameter.ParameterParser;
import com.zhucode.longio.context.parameter.ParameterParserFactory;
import com.zhucode.longio.reflect.MethodRef;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessageProcessTask implements Runnable {
	
	private MessageBlock<?> message;
	
	private MethodRef handler;
	
	private Parameter[] paras;
	
	ParameterParserFactory parameterParserFactory;
	
	MessageProcessTask(MessageBlock<?> mb, MethodRef mih, ParameterParserFactory parameterParserFactory) {
		this.message = mb;
		this.handler = mih;
		this.paras = this.handler.getMethod().getParameters();
		this.parameterParserFactory = parameterParserFactory;
	}

	
	@Override
	public void run() {
		
		if (handler == null) {
			MessageBlock<Object> mret = new MessageBlock<Object>(null);
			mret.setCmd(message.getCmd());
			mret.setSerial(message.getSerial());
			mret.setSessionId(message.getSessionId());
			mret.setConnector(message.getConnector());
			mret.setStatus(404);
			mret.getConnector().sendMessage(mret);
			return;
		}
		
		Object[] args = null;
		Object body = message.getBody();
		ParameterParser pp = parameterParserFactory.getParser(body.getClass());
		args = pp.parse((MessageBlock<?>) message, 
				this.handler.getMethod().getAnnotations(), paras);
		Object ret = this.handler.handle(args);
		
		MessageBlock<Object> mret = new MessageBlock<Object>(ret);
		
		if (mret != null && mret instanceof MessageBlock<?>) {
			mret.setCmd(message.getCmd());
			mret.setSerial(message.getSerial());
			mret.setSessionId(message.getSessionId());
			mret.setConnector(message.getConnector());
			mret.getConnector().sendMessage(mret);
		} else {
			mret.setCmd(message.getCmd());
			mret.setSerial(message.getSerial());
			mret.setSessionId(message.getSessionId());
			mret.setConnector(message.getConnector());
			mret.setStatus(500);
			mret.getConnector().sendMessage(mret);
		}
		
	}



	
	
}
