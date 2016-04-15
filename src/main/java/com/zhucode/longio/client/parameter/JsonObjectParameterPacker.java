/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.client.parameter;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.context.parameter.Body;
import com.zhucode.longio.context.parameter.CMD;
import com.zhucode.longio.context.parameter.Key;
import com.zhucode.longio.context.parameter.Uid;
import com.zhucode.longio.message.MessageCallback;
import com.zhucode.longio.utils.ClassUtils;


/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JsonObjectParameterPacker implements ParameterPacker<JSONObject> {

	@Override
	public JSONObject pack(MethodInfo mi, Object... args) {
		if (args == null) {
			return null;
		}
		if (args.length <= 2 && (args[0] instanceof JSONObject)) {
			return (JSONObject)args[0];
		}
		Parameter[] paras = mi.getMethod().getParameters();
		JSONObject js = new JSONObject();
		for (int i = 0; i < paras.length; i++) {
			Parameter pa = paras[i];
			if (pa.getType().isAssignableFrom(MessageCallback.class)) {
				continue;
			}
			if (pa.isAnnotationPresent(Body.class)) {
				return (JSONObject)args[i];
			}
			if (pa.isAnnotationPresent(Uid.class)) {
				continue;
			}
			if (pa.isAnnotationPresent(CMD.class)) {
				continue;
			}
			Object val = serialize(args[i]);
			String key = pa.getAnnotation(Key.class).value();
			js.put(key, val);
		}
		
		return js;
	}

	@Override
	public Object unpack(Class<?> returnCls, Type returnType, Object msg) {
		if (returnType == Void.TYPE) {
			return null;
		}
		JSONObject ret = (JSONObject)msg;
		if (ret == null) {
			return null;
		}
		if (ClassUtils.isPrimitive(returnCls)) {
			return ret.getObject("_ret_", returnCls);
		}
		if (returnCls.isAssignableFrom(List.class)) {
			return ret.get("_ret_");
		}
		if (returnCls.isAssignableFrom(Set.class)) {
			return new HashSet<Object>(ret.getJSONArray("_ret_"));
		}
		return JSON.parseObject(ret.toJSONString(), returnType);
	}
	
	private Object serialize(Object obj) {
		if (obj == null) {
			return null;
		}
		if (ClassUtils.isPrimitive(obj.getClass())) {
			return obj;
		}
		return JSON.toJSON(obj);
	}

}
