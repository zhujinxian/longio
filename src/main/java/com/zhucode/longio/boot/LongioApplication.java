/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.boot;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.client.reflect.DefaultMethodInfoFactory;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.client.reflect.MethodInfoFactory;
import com.zhucode.longio.client.reflect.ProxyInvocationHandler;
import com.zhucode.longio.message.MethodDispatcher;
import com.zhucode.longio.reflect.DefaultMethodRefFactory;
import com.zhucode.longio.reflect.MethodRefFactory;
import com.zhucode.longio.scan.ResourceScanner;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class LongioApplication {
	
	public static Map<Class<? extends Connector>, Connector> connectors = new HashMap<Class<? extends Connector>, Connector>();

	
	public static void run(Class<? extends Connector> connectorCls, int port, TransportType tt, ProtocolType pt) {
		
		MethodDispatcher dispatcher = new MethodDispatcher();
		MethodRefFactory mrf = new DefaultMethodRefFactory();
		for (Object obj : getServiceInvokers()) {
			dispatcher.registerMethodRefs(mrf.createMethodRefs(obj));
		}
		
		try {
			Connector connector = connectors.get(connectorCls);
			if (connector == null) {
				connector = connectorCls.newInstance();
				connectors.put(connectorCls, connector);
			}
			connector.start(port, dispatcher, tt, pt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	private static List<Object> getServiceInvokers() {
		List<Object> invokers = new ArrayList<Object>();
		Collection<String> resources = ResourceScanner.getResources(Pattern.compile(".*Service.class$"));
		for (String name : resources) {
			System.out.println(name);
			try {
				if (name.indexOf("classes/") > -1) {
					int x = name.indexOf("classes/") + "classes/".length();
					name = name.substring(x, name.length());
				}
				name = name.replaceAll("/", ".");
				name = name.replaceAll(".class", "");
				if (name.contains("$")) {
					continue;
				}
				Class<?> cls = Class.forName(name);
				Lservice ls = cls.getAnnotation(Lservice.class);
				if (ls == null) {
					continue;
				}
				invokers.add(cls.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return invokers;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<? extends Connector> connectorCls, Class<T> requiredType) {
		
		MethodInfoFactory mif = new DefaultMethodInfoFactory();
		List<MethodInfo> mis = mif.createMethodInfo(requiredType);
		
		Connector connector = connectors.get(connectorCls);
		if (connector == null) {
			try {
				connector = connectorCls.newInstance();
				connectors.put(connectorCls, connector);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class<?>[]{requiredType}, new ProxyInvocationHandler(connector, requiredType, mis));
	}
}