/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.callback.CallbackDispatcher;
import com.zhucode.longio.callback.InvocationTask;
import com.zhucode.longio.client.cluster.ClientCluster;
import com.zhucode.longio.client.cluster.GroupClientCluster;
import com.zhucode.longio.client.cluster.LoadBalance;
import com.zhucode.longio.client.cluster.SingleClientCluster;
import com.zhucode.longio.client.parameter.CMDParser;
import com.zhucode.longio.client.parameter.ParameterPackerFactory;
import com.zhucode.longio.client.parameter.UidParser;
import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.exception.LongioException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.MessageCallback;
import com.zhucode.longio.message.MessageSerial;
import com.zhucode.longio.protocol.Protocol;
import com.zhucode.longio.protocol.ProtocolFactory;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProxyInvocationHandler implements InvocationHandler {

	private static ParameterPackerFactory ppf = new ParameterPackerFactory();
	
	private Map<Method, MethodInfo> methods;
	
	private Protocol pp;

	private Connector connector;

	private CallbackDispatcher dispatcher;
	
	private Class<?> proxyCls;
	
	private ClientCluster client;
	
	private AppLookup appLookup;
	
	public ProxyInvocationHandler(Connector connector, AppLookup appLookup, Class<?> requiredType, List<MethodInfo> methods) {
		this.connector = connector;
		this.dispatcher = connector.getCallbackDispatcher();
		this.proxyCls = requiredType;
		this.methods =  new HashMap<Method, MethodInfo>();
		for (MethodInfo mi : methods) {
			this.methods.put(mi.getMethod(), mi);
		}
		this.appLookup = appLookup;
		initClientClusterAndPacker();
	}
	

	private void initClientClusterAndPacker() {
		LsAutowired lsa = this.proxyCls.getAnnotation(LsAutowired.class);
		String app = lsa.app();
		String ip = lsa.ip();
		int port = lsa.port();
		TransportType tt = lsa.tt();
		ProtocolType pt = lsa.pt();
		LoadBalance lb = lsa.lb();
		this.pp = ProtocolFactory.getProtocol(pt);
		if (appLookup.parseHosts(app) == null) {
			this.client = new SingleClientCluster(ip, port, tt, pt, connector);
		} else {
			this.client = new GroupClientCluster(connector, appLookup, app, tt, pt, lb);
		}
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		MethodInfo mi = methods.get(method);
		Object po = pp.packMethodInvokeParameters(mi, args);
		
		MessageBlock mb = new MessageBlock(po);
		mb.setCmd(mi.getCmd());
		mb.setSerial(MessageSerial.newSerial());
		mb.setProtocol(pp);
		
		int uid = UidParser.parseUid(mi.getMethod(), args);
		int cmd = CMDParser.parseCMD(mi.getMethod(), args);
		
		if (uid > 0) {
			mb.setUid(uid);
		}
		
		if (cmd > 0) {
			mb.setCmd(cmd);
		}
		
		InvokeCall<MessageBlock> call = new InvokeCall<MessageBlock>(dispatcher, client, mb, 2);
		
		InvocationTask<MessageBlock> task = new InvocationTask<MessageBlock>(call);
		if (args != null && args.length > 0 && args[args.length-1] instanceof MessageCallback) {
			MessageCallback callback = (MessageCallback)args[args.length-1];
			this.dispatcher.registCallback(mb.getSerial(), task, callback, mi.getTimeout());
			return null;
		} else {
			this.dispatcher.registTask(mb.getSerial(), task);

			try {
				MessageBlock ret = task.get(mi.getTimeout(), TimeUnit.MILLISECONDS);
				client.sendSuccess(call.getPoint(), call.getMb());
				if (ret.getStatus() < 400) {
					return pp.deserializeMethodReturnValue(mi.getMethod().getReturnType(), mi.getMethod().getGenericReturnType(), ret.getBody());
				}
				throw new LongioException(ret.getStatus(), ret.getErr());
			} catch (TimeoutException e) {
				this.dispatcher.unregist(mb.getSerial());
				client.sendTimeout(call.getPoint(), call.getMb());
				e.printStackTrace();
			}
			
			throw new Exception("server invoke timeout");
		}
		
		
	}

}
