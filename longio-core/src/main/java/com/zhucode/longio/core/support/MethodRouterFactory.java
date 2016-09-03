/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.zhucode.longio.annotation.Rpc;
import com.zhucode.longio.annotation.RpcController;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.server.HandlerInterceptor;
import com.zhucode.longio.core.server.MethodHandler;
import com.zhucode.longio.core.server.MethodRouter;
import com.zhucode.longio.scan.ResourceScanner;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class MethodRouterFactory {
	
	private CmdLookup cmdLookup;
	
	
	public MethodRouterFactory(CmdLookup cmdLookup) {
		super();
		this.cmdLookup = cmdLookup;
	}

	public MethodRouter createRouter(String path, List<HandlerInterceptor> inteceptors) {
		
		List<Object> controllerObjs = scanControllers(path);
		Map<Integer, MethodHandler> routeMap = createRouteMap(controllerObjs);
		
		return new MethodRouter(routeMap, inteceptors);
	}
	
	private Map<Integer, MethodHandler> createRouteMap(List<Object> controllerObjs) {
		Map<Integer, MethodHandler> map = new HashMap<Integer, MethodHandler>();
		for (Object obj : controllerObjs) {
			Class<?> cls = obj.getClass();
			RpcController ls = cls.getAnnotation(RpcController.class);
			if (ls == null) {
				continue;
			}
			for (Method m : cls.getMethods()) {
				Rpc lio = m.getAnnotation(Rpc.class);
				if (lio == null) {
					continue;
				}
				String cmdName = ls.path() + "." + lio.cmd();
				cmdName = cmdName.replaceAll("\\.\\.", ".");
				int cmd = cmdLookup.parseCmd(cmdName);
				boolean asy = lio.asy();
				boolean reply = lio.reply();
				MethodHandler mih = new MethodHandler(cmd, cmdName, obj, m, asy, reply);
				map.put(cmd, mih);
			}
			
		}
		return map;
	}

	private static List<Object> scanControllers(String path) {
		List<Object> invokers = new ArrayList<Object>();
		Collection<String> resources = ResourceScanner.getResources(Pattern.compile(path + ".*RpcController.class$"));
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
				RpcController ls = cls.getAnnotation(RpcController.class);
				if (ls == null) {
					continue;
				}
				invokers.add(cls.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return invokers;
	}

	

}
