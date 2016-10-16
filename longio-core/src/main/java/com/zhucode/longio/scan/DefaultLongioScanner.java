/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.zhucode.longio.annotation.RpcController;
import com.zhucode.longio.core.server.HandlerInterceptor;

/**
 * @author zhu jinxian
 * @date  2016年10月15日 下午2:40:09 
 * 
 */
public class DefaultLongioScanner implements LongioScanner {

	@Override
	public List<HandlerInterceptor> scanInterceptors(String path) {
		List<HandlerInterceptor> invokers = new ArrayList<HandlerInterceptor>();
		Collection<String> resources = ResourceScanner.getResources(Pattern.compile(path + ".*RpcInterceptor.class$"));
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
				if (cls.isAssignableFrom(HandlerInterceptor.class)) {
					invokers.add((HandlerInterceptor)cls.newInstance());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return invokers;
	}
		
	@Override
	public List<Object> scanControllers(String path) {
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
