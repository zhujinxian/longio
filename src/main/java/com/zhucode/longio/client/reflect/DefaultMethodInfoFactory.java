/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.conf.DefaultCmdLookup;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class DefaultMethodInfoFactory implements MethodInfoFactory {

	private DefaultCmdLookup cnm = new DefaultCmdLookup();
	
	@Override
	public List<MethodInfo> createMethodInfo(Class<?> cls) {
		List<MethodInfo> ms = new ArrayList<MethodInfo>();
		LsAutowired ls = cls.getAnnotation(LsAutowired.class);
		if (ls == null) {
			return ms;
		}
		
		for (Method m : cls.getMethods()) {
			Lio lio = m.getAnnotation(Lio.class);
			if (lio == null) {
				continue;
			}
			String cmdName = ls.path() + "." + lio.cmd();
			cmdName = cmdName.replaceAll("\\.\\.", ".");
			int cmd = cnm.parseCmd(cmdName);
			boolean asy = lio.asy();
			long timeout = lio.timeout();
			MethodInfo mi = new MethodInfo(cmd, cmdName, cls, m, asy, timeout);
			ms.add(mi);
		}
		return ms;
	}

	

}
