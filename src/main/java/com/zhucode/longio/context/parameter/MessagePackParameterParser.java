/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.context.parameter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.unpacker.Unpacker;

import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.format.MessagePackData;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessagePackParameterParser implements ParameterParser {

	TemplateRegistry tr = new TemplateRegistry(null);
	
	@Override
	public Object[] parse(MessageBlock<?> mb, Annotation[] meta,
			Parameter[] paras) {
		Object[] objs = new Object[paras.length];
		
		if (objs.length == 0) {
			return objs;
		}
		byte[] body = (byte[])mb.getBody();
		Unpack unpack = findUnpack(meta);
		if (unpack == null) {
			MessagePack mp = new MessagePack();
			ByteArrayInputStream in = new ByteArrayInputStream(body);
		    Unpacker unpacker = mp.createUnpacker(in);
			for (int i = 0; i < paras.length; i++) {
				Parameter p = paras[i];
				if (p.getType() == MessageBlock.class) {
					objs[i] = mb;
					continue;
				}
				if (p.getType() == Connector.class) {
					objs[i] = mb.getConnector();
					continue;
				}
				try {
					Type type = p.getParameterizedType() == null? p.getType() : p.getParameterizedType();
					objs[i] = unpacker.read(parseTemplate(type));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				MessagePack mp = new MessagePack();
				Class<?> msgCls = Class.forName(unpack.value());
				Object data = mp.read(body, msgCls);
				for (int i = 0; i < paras.length; i++) {
					Parameter p = paras[i];
					if (p.getType() == MessageBlock.class) {
						objs[i] = mb;
						continue;
					}
					if (p.getType() == data.getClass()) {
						objs[i] = data;
						continue;
					}
					
					Key k = p.getAnnotation(Key.class);
					String key = k.value();
					Object val = getValFromMessage(data, key);
					objs[i] = val;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return objs;
	}
	
	private Object getValFromMessage(Object data, String key) {
		try {
			return data.getClass().getField(key).get(data);
		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Unpack findUnpack(Annotation[] meta) {
		for (Annotation ann : meta) {
			if (ann.annotationType() == Unpack.class) {
				return (Unpack)ann;
			}
		}
		return null;
	}
	

	private Template<?> parseTemplate(Type type) {
		return tr.lookup(type);
	}


}
