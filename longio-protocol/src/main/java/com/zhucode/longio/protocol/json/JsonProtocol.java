/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol.json;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.Request;
import com.zhucode.longio.Response;
import com.zhucode.longio.annotation.Body;
import com.zhucode.longio.annotation.CMD;
import com.zhucode.longio.annotation.Uid;
import com.zhucode.longio.protocol.utils.ClassUtils;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public class JsonProtocol implements Protocol {

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
	public void decodeRequest(Request request, byte[] bytes) throws ProtocolException {
		try {
			String str = new String(bytes, "utf-8");
			JSONObject json = JSONObject.parseObject(str);
			Object data = json.get("data");
			request.setSerial(json.getLongValue("serial"));
			request.setUid(json.getLongValue("uid"));
			request.setCmd(json.getIntValue("cmd"));
			request.setVersion(json.getFloatValue("version"));
			request.setBody(data);
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException(e);
		}

	}


	@Override
	public void decodeResponse(Response response, byte[] bytes) throws ProtocolException {
		try {
			String str = new String(bytes, "utf-8");
			JSONObject json = JSONObject.parseObject(str);
			Object data = json.get("data");
			response.setSerial(json.getLongValue("serial"));
			response.setCmd(json.getIntValue("cmd"));
			response.setVersion(json.getFloatValue("version"));
			response.setStatus(json.getIntValue("status"));
			response.setErr(json.getString("err"));
			response.setBody(data);
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException(e);
		}		
	}


	@Override
	public byte[] encodeRequest(Request request) throws ProtocolException {
		JSONObject res = new JSONObject();
		res.put("serial", request.getSerial());
		res.put("uid", request.getUid());
		res.put("cmd", request.getCmd());
		res.put("version", request.getVersion());
		res.put("data", request.getBody());		
		try {
			return res.toJSONString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException(e);
		}
	}


	@Override
	public byte[] encodeResponse(Response response) throws ProtocolException {
		JSONObject res = new JSONObject();
		res.put("serial", response.getSerial());
		res.put("cmd", response.getCmd());
		res.put("version", response.getVersion());
		res.put("status", response.getStatus());
		res.put("err", response.getErr());
		res.put("data", response.getBody());		
		try {
			return res.toJSONString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException(e);
		}

	}


	@Override
	public Object[] deserializeParameters(Method method, Request request, Response response) throws SerializeException {
		Parameter[] paras = method.getParameters();
		Object[] objs = new Object[paras.length];
		JSONArray data = (JSONArray)request.getBody();
		int j = 0;
		for (int i = 0; i < paras.length; i++) {
			Parameter p = paras[i];
			if (p.getType().isAssignableFrom(Request.class)) {
				objs[i] = request;
				continue;
			}
			if (p.getType().isAssignableFrom(Response.class)) {
				objs[i] = response;
				continue;
			}
			objs[i] = parseObject(p.getType(), p.getParameterizedType(), data.get(j));
			j++;
		}
		return objs;
	}


	@Override
	public Object serializeParameters(Method method, Object... args) throws SerializeException {
		JSONArray ja = new JSONArray();
		Parameter[] paras = method.getParameters();
		for (int i = 0; i < paras.length; i++) {
			Parameter pa = paras[i];
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
	public Object serializeReturnValue(Method method, Object ret) throws SerializeException {
		return JSON.toJSON(ret);
	}


	@Override
	public Object deserializeReturnValue(Method method, Object msg) throws SerializeException {
		Class<?> returnCls = method.getReturnType();
		Type returnType = method.getGenericReturnType();
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
