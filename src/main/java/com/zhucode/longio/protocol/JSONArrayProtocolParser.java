/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSONArray;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.MessageBlock;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JSONArrayProtocolParser implements ProtocolParser<JSONArray> {

	@Override
	public MessageBlock<JSONArray> decode(byte[] bytes)
			throws ProtocolException {
		try {
			String str = new String(bytes, "utf-8");
			JSONArray ja = JSONArray.parseArray(str);
			MessageBlock<JSONArray> mb = new MessageBlock<JSONArray>(ja);
			mb.setCmd(ja.getIntValue(0));
			mb.setSerial(ja.getLongValue(1));
			return mb;
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("decode bytes[] with utf-8 error");
		}
	}

	@Override
	public byte[] encode(MessageBlock<?> mb) throws ProtocolException {
		JSONArray body = new JSONArray();
		body.add(0, mb.getCmd());
		body.add(1, mb.getSerial());
		body.add(2, mb.getStatus());
		try {
			body.add((JSONArray)mb.getBody());
		} catch (Exception e1) {
			throw new ProtocolException("MessageBlock's body must be JSONArray");
		}
		
		try {
			return body.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ProtocolException("encode bytes[] with utf-8 error");
		}
	}

	
	@Override
	public byte[] getHeartBeat() {
		JSONArray body = new JSONArray();
		body.add(0, 0);
		body.add(1, 0);
		body.add(new JSONArray());
		try {
			return body.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
