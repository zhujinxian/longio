/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.utils.ClassUtils;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JSONObjectProtocolParser implements ProtocolParser<JSONObject> {
	
	@Override
	public MessageBlock<JSONObject> decode(byte[] bytes) throws ProtocolException {
		try {
			String str = new String(bytes, "utf-8");
			JSONObject json = JSONObject.parseObject(str);
			Object data = json.get("data");
			MessageBlock<JSONObject> mb = null;
			if (data instanceof JSONObject) {
				mb = new MessageBlock<JSONObject>((JSONObject)data);
			} else if (data == null) {
				mb = new MessageBlock<JSONObject>(null);
			} else if (ClassUtils.isPrimitive(data.getClass())) {
				JSONObject js = new JSONObject();
				js.put("_ret_", data);
				mb = new MessageBlock<JSONObject>(js);
			}
			
			mb.setSerial(json.getLongValue("serial"));
			mb.setCmd(json.getIntValue("cmd"));
			mb.setUid(json.getIntValue("uid"));
			mb.setStatus(json.getIntValue("status"));
			mb.setErr(json.getString("err"));
			return mb;
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("decode bytes[] with utf-8 error");
		}
	}

	@Override
	public byte[] encode(MessageBlock<?> mb) throws ProtocolException {
		JSONObject res = new JSONObject();
		res.put("cmd", mb.getCmd());
		res.put("serial", mb.getSerial());
		res.put("uid", mb.getUid());
		res.put("status", mb.getStatus());
		res.put("err", mb.getErr());
		
		if (mb.getBody() == null) {
			res.put("data", null);
		} else if (ClassUtils.isPrimitive(mb.getBody().getClass())) {
			res.put("data", mb.getBody());
		} else {
			JSONObject body = (JSONObject)JSONObject.toJSON(mb.getBody());
			res.put("data", body);
		}
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

}
