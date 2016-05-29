/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.parameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.exception.UnsupportedException;
import com.zhucode.longio.message.format.Proto;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProtoBufParameterPacker implements ParameterPacker {

	@Override
	public Message pack(MethodInfo mi, Object... args) {
		if (args == null) {
			return null;
		}
		return (Message)args[0];
		
	}

	@Override
	public Object unpack(Class<?> returnCls, Type type, Object msg)
			throws UnsupportedException {
		if (type == Void.TYPE) {
			return null;
		}
		if (!returnCls.isAssignableFrom(Message.class)) {
			throw new UnsupportedException("ProtoBufParameterPacker only support return Message");
		}
		
		Proto.Message mp = (Proto.Message)msg;
		
		try {
			Method m = returnCls.getMethod("parseFrom", ByteString.class);
			return m.invoke(null, mp.getBody());
		} catch (Exception e) {
			throw new UnsupportedException("ProtoBufParameterPacker unpacker error");
		}
		
	} 
}
