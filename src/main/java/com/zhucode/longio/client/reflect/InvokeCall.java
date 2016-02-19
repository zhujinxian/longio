/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.reflect;

import java.util.concurrent.Callable;

import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.client.cluster.ClientCluster;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.transport.Beginpoint;


/**
 * @author zhu jinxian
 * @date  2016年2月16日
 * 
 */
public class InvokeCall<V> implements Callable<V> {
	
	private ClientCluster client;
	private Beginpoint point = null;
	private MessageBlock<?> mb;
	private int retry;
	CallbackDispatcher dispatcher;
	
	public InvokeCall(CallbackDispatcher dispatcher, ClientCluster client, MessageBlock<?> mb, int retry) {
		super();
		this.client = client;
		this.mb = mb;
		this.retry = retry;
		this.dispatcher = dispatcher;
	}

	

	@Override
	public V call() throws Exception {
		send: while (retry-- > 0) {
			try {
				point = client.getNextPoint();
				point.send(mb);
			} catch (Exception e) {
				if (retry > 0) {
					client.sendFail(point, mb);
					continue send;
				} else {
					mb.setBody(null);
					mb.setStatus(500);
					dispatcher.setReturnValue(mb);
				}
				e.printStackTrace();
			}
			break;
		}
		return null;
	}



	public ClientCluster getClient() {
		return client;
	}

	public Beginpoint getPoint() {
		return point;
	}

	public MessageBlock<?> getMb() {
		return mb;
	}
	
	
}
