/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.message;

import java.lang.reflect.Parameter;
import java.util.List;

import com.zhucode.longio.exception.LongioException;
import com.zhucode.longio.protocol.Protocol;
import com.zhucode.longio.reflect.MethodRef;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessageProcessTask implements Runnable {
	
	private MessageBlock message;
	
	private MethodRef handler;
	
	private List<MessageFilter> filters;
	
	
	MessageProcessTask(MessageBlock mb, MethodRef mih) {
		this.message = mb;
		this.handler = mih;
	}

	
	@Override
	public void run() {
		
		for (MessageFilter filter : filters) {
			if (!filter.preFilter(message)) {
				return;
			}
		}
		
		MessageBlock mret =  new MessageBlock(null);
		mret.setCmd(message.getCmd());
		mret.setUid(message.getUid());
		mret.setSerial(message.getSerial());
		mret.setSessionId(message.getSessionId());
		mret.setConnector(message.getConnector());
		mret.setProtocol(message.getProtocol());
		
		if (handler == null) {
			mret.setStatus(404);
			mret.setErr("Invoked method not found");
		} else {
			try {
				Object[] args = null;
				Object body = message.getBody();
				Protocol pp = mret.getProtocol();
				if (body != null) {
					Parameter[] paras  = this.handler.getMethod().getParameters();
					args = pp.unpackMethodInvokeParameters(message, paras);
				}
				if (args == null) {
					args = new Object[0];
				}
				Object ret = this.handler.handle(args);
				mret.setStatus(200);
				mret.setBody(pp.serializeMethodReturnValue(ret));
			} catch (LongioException e1) {
				mret.setStatus(e1.getErrcode());
				mret.setErr(e1.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				mret.setStatus(500);
				mret.setErr("No defined exception");
			}
		}
		
		if (this.handler.isReply()) {
			mret.getConnector().sendMessage(mret);
		}
		
		for (MessageFilter filter : filters) {
			filter.postFilter(message, mret);
		}
	}


	public void setFilters(List<MessageFilter> filters) {
		this.filters = filters;
	}

	
}
