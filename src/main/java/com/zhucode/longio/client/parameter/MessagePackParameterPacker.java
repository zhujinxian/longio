/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.parameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;

import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.context.parameter.Key;
import com.zhucode.longio.context.parameter.Pack;
import com.zhucode.longio.message.format.MessagePackData;
import com.zhucode.longio.utils.ClassUtils;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessagePackParameterPacker implements ParameterPacker<Object> {

	TemplateRegistry tr = new TemplateRegistry(null);
	
	@Override
	public Object pack(MethodInfo mi, Object... args) {
		if (args == null) {
			return null;
		}
		Parameter[] paras = mi.getMethod().getParameters();
		Pack pack = mi.getMethod().getAnnotation(Pack.class);
		try {
			if (pack != null) {
				Class<?> cls = Class.forName(pack.value());
				Object obj = cls.newInstance();
				
				for (int i = 0; i < paras.length; i++) {
					Parameter pa = paras[i];
					Key key = pa.getAnnotation(Key.class);
					Field f = cls.getField(key.value());
					f.set(obj, args[i]);
				}
				return obj;
			} else {
				MessagePack mp = new MessagePack();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		        Packer packer = mp.createPacker(out);
		        for (Object arg : args) {
					packer.write(arg);
				}
		        return out.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return null;
	}

	@Override
	public Object unpack(Class<?> returnCls, Type returnType, Object msg) {
		if (returnType == Void.TYPE) {
			return null;
		}
		if (ClassUtils.isPrimitive(returnCls.getClass())) {
			return msg;
		}
	
		MessagePackData mpd = (MessagePackData)msg;
		byte[] ret = mpd.data;
		
		MessagePack mp = new MessagePack();
		try {
			return mp.read(ret, parseTemplate(returnCls, returnType));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Template<?> parseTemplate(Class<?> returnCls, Type returnType) {
		return tr.lookup(returnType);
	}

}
