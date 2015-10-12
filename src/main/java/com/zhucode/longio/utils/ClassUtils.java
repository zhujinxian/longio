/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.utils;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ClassUtils {
	public static boolean isPrimitive(Class<?> cls) {
		
		if (cls.isPrimitive()) {
			return true;
		}
		
		if (cls == Boolean.class) {
			return true;
		}
		
		if (cls == Byte.class) {
			return true;
		}
		
		if (cls == Short.class) {
			return true;
		}
		
		if (cls == Character.class) {
			return true;
		}
		
		if (cls == Integer.class) {
			return true;
		}
		
		if (cls == Long.class) {
			return true;
		}
		
		if (cls == Float.class) {
			return true;
		}
		
		if (cls == Double.class) {
			return true;
		}
		
		if (cls == String.class) {
			return true;
		}
		
		return false;
	}
}
