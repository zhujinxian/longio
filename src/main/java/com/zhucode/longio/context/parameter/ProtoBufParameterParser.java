/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.context.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.format.Proto;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProtoBufParameterParser implements ParameterParser {

	@Override
	public Object[] parse(MessageBlock<?> mb, Annotation[] meta,  Parameter[] paras) {
		
		Object[] objs = new Object[paras.length];
		
		if (objs.length == 0) {
			return objs;
		}
		
		@SuppressWarnings("unchecked")
		MessageBlock<Proto.Message> mb0 = (MessageBlock<Proto.Message>)mb;
		
		Annotation ann = findMessageClass(meta);
		
		try {
			if (ann == null) {
				for (int i = 0; i < paras.length; i++) {
					Parameter p = paras[i];
					if (p.getType() == MessageBlock.class) {
						objs[i] = mb;
						continue;
					}
					if (p.getType().getSuperclass() == Message.class) {
						Method m = p.getType().getMethod("parseFrom", ByteString.class);
						Proto.Message body = mb0.getBody();
						Object ret = m.invoke(null, body.getBody());
						objs[i] = ret;
						continue;
					}
				}
			} else {
				Unpack proto = (Unpack)ann;
				
				Class<?> msgCls = Class.forName(proto.value());
				
				Proto.Message body = mb0.getBody();
				
				Method m = msgCls.getMethod("parseFrom", ByteString.class);
				
				Object ret = m.invoke(null, body.getBody());
				
				Message msg = (Message)ret;
				
				Map<FieldDescriptor, Object> kvs = msg.getAllFields();
				
				for (int i = 0; i < paras.length; i++) {
					Parameter p = paras[i];
					if (p.getType() == MessageBlock.class) {
						objs[i] = mb;
						continue;
					}
					if (p.getType() == ret.getClass()) {
						objs[i] = ret;
						continue;
					}
					
					Key k = p.getAnnotation(Key.class);
					String key = k.value();
					Object val = getValFromProto(kvs, key);
					objs[i] = val;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return objs;
	}
	
	private Annotation findMessageClass(Annotation[] meta) {
		for (Annotation ann : meta) {
			if (ann.annotationType() == Unpack.class) {
				return ann;
			}
		}
		return null;
	}
	
	private Object getValFromProto(Map<FieldDescriptor, Object> kvs, String key) {
		for (Entry<FieldDescriptor, Object> kv : kvs.entrySet()) {
			System.out.println(kv.getKey().getFullName());
			System.out.println(kv.getKey().getName());
			if (kv.getKey().getName().equals(key)) {
				return kv.getValue();
			}
		}
		return null;
	}

}
