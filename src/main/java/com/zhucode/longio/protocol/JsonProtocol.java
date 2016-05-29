/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.context.parameter.Body;
import com.zhucode.longio.context.parameter.CMD;
import com.zhucode.longio.context.parameter.Uid;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.exception.SerializeException;
import com.zhucode.longio.exception.UnsupportedException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.MessageCallback;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.utils.ClassUtils;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JsonProtocol implements Protocol {

	@Override
	public MessageBlock decode(byte[] bytes) throws ProtocolException {
		try {
			String str = new String(bytes, "utf-8");
			JSONObject json = JSONObject.parseObject(str);
			Object data = json.get("data");
			MessageBlock mb = new MessageBlock(data);			
			mb.setSerial(json.getLongValue("serial"));
			mb.setUid(json.getLongValue("uid"));
			mb.setCmd(json.getIntValue("cmd"));
			mb.setVersion(json.getFloatValue("version"));
			mb.setStatus(json.getIntValue("status"));
			mb.setErr(json.getString("err"));
			return mb;
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("decode bytes[] with utf-8 error");
		}
	}

	@Override
	public byte[] encode(MessageBlock mb) throws ProtocolException {
		JSONObject res = new JSONObject();
		res.put("serial", mb.getSerial());
		res.put("uid", mb.getUid());
		res.put("cmd", mb.getCmd());
		res.put("version", mb.getVersion());
		res.put("status", mb.getStatus());
		res.put("err", mb.getErr());
		res.put("data", mb.getBody());		
		try {
			return res.toJSONString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("encode bytes[] with utf-8 error");
		}
	}

	
	@Override
	public byte[] getHeartBeat() {
		JSONObject res = new JSONObject();
		res.put("cmd", 0);
		res.put("serial", 0);
		res.put("uid", 0);
		res.put("status", 0);
		res.put("data", null);
		try {
			return res.toJSONString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.addSuppressed(e);
		}
		return null;
		
	}

	@Override
	public Object[] unpackMethodInvokeParameters(MessageBlock mb, Parameter[] paras) {
		Object[] objs = new Object[paras.length];
		JSONArray data = (JSONArray) mb.getBody();
		int j = 0;
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
			objs[i] = parseObject(p.getType(), p.getParameterizedType(), data.get(j));
			j++;
		}
		return objs;
	}
	
	private Object parseObject(Class<?> cls, Type type, Object val) {
		
		if (val == null) {
			return null;
		}
		
		if (ClassUtils.isPrimitive(cls)) {
			if (cls == String.class) {
				return val.toString();
			}

			return JSON.parseObject(val.toString(), cls);
		}
		
		return JSON.parseObject(val.toString(), type);
	}

	@Override
	public Object serializeMethodReturnValue(Object ret) throws SerializeException {
		return JSON.toJSON(ret);
	}

	@Override
	public Object packMethodInvokeParameters(MethodInfo mi, Object... args) {
		JSONArray ja = new JSONArray();
		Parameter[] paras = mi.getMethod().getParameters();
		for (int i = 0; i < paras.length; i++) {
			Parameter pa = paras[i];
			if (pa.getType().isAssignableFrom(MessageCallback.class)) {
				continue;
			}
			if (pa.isAnnotationPresent(Body.class)) {
				return (JSONArray)args[i];
			}
			if (pa.isAnnotationPresent(Uid.class)) {
				continue;
			}
			if (pa.isAnnotationPresent(CMD.class)) {
				continue;
			}
			ja.add(JSON.toJSON(args[i]));
		}
		return ja;
	}

	@Override
	public Object deserializeMethodReturnValue(Class<?> returnCls, Type returnType, Object msg) throws UnsupportedException {
		if (returnType == Void.TYPE || msg == null) {
			return null;
		}
		
		if (ClassUtils.isPrimitive(returnCls)) {
			if (returnCls == String.class) {
				return msg.toString();
			}
			return JSON.parseObject(msg.toString(), returnCls);
		}
		
		return JSON.parseObject(msg.toString(), returnType);
	}

}
