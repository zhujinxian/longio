/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.annotation.Rpc;
import com.zhucode.longio.annotation.RpcService;
import com.zhucode.longio.boot.ClientHandler;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;

/**
 * @author zhu jinxian
 * @date  2016年10月16日 上午1:30:15 
 * 
 */
public class RpcProxy {
	
	@SuppressWarnings("unchecked")
	public static <T> T proxy(Class<?> rpc, AppLookup appLookup, CmdLookup cmdLookup, ClientHandler handler) {
		RpcService service = rpc.getAnnotation(RpcService.class);
		Map<Method, RpcMethodInfo> rpcMap = createMethodInfo(cmdLookup, rpc);
		Protocol protocol = null;
		try {
			protocol = service.protocolClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		MethodInvocationHandler invokeHandler = new MethodInvocationHandler(service.app(), rpcMap, handler, protocol);
		handler.connect(service.app(), service.tt(), protocol);
		return (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(), new Class[]{rpc}, invokeHandler);
	}
	
	private static Map<Method, RpcMethodInfo> createMethodInfo(CmdLookup cmdLookup, Class<?> cls) {
		Map<Method, RpcMethodInfo> ms = new HashMap<Method, RpcMethodInfo>();
		RpcService service = cls.getAnnotation(RpcService.class);
		if (service == null) {
			return ms;
		}
		
		for (Method m : cls.getMethods()) {
			Rpc rpc = m.getAnnotation(Rpc.class);
			if (rpc == null) {
				continue;
			}
			String cmdName = service.path() + "." + rpc.cmd();
			cmdName = cmdName.replaceAll("\\.\\.", ".");
			int cmd = cmdLookup.parseCmd(cmdName);
			boolean asy = rpc.asy();
			int timeout = rpc.timeout();
			float version = rpc.version();
			RpcMethodInfo mi = new RpcMethodInfo(cmd, cmdName, version, cls, m, asy, timeout);
			ms.put(m, mi);
		}
		return ms;
	}


}
