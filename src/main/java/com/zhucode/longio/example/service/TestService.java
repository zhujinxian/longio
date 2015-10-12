/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.example.service;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.context.parameter.Key;
import com.zhucode.longio.context.parameter.Unpack;
import com.zhucode.longio.example.message.Res;
import com.zhucode.longio.example.message.UserMsg;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
@Lservice(path = "com.lehuihome")
public class TestService {
	
	@Lio(cmd = "getUser")
	@Unpack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>>getUser(@Key("user_id")long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		JSONObject ret = new JSONObject();
		ret.put("status", "success");
		Map<String, UserMsg> rm = new HashMap<>();
		UserMsg um = new UserMsg();
		um.user_id = 9999;
		rm.put("1234", um);
		
		Map<String, Map<String, UserMsg>> m = new HashMap<>();
		m.put("1234", rm);
		return m;
		//return new String[]{"status", "true", "dddd"};
	}
	
	@Lio(cmd = "getUser1")
	@Unpack("com.zhucode.longio.example.message.User$Data")
	public Res.Data getUser1(@Key("user_id")long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		return Res.Data.newBuilder().setStatus("success").build();
	}
	
	@Lio(cmd = "getVoid")
	public void testVoid() {
		System.out.println("++++++++++++test void+++++++++++++++");
	}
	
	@Lio(cmd = "getInt")
	public int testInt() {
		System.out.println("++++++++++++test int+++++++++++++++");
		return 98800;
	}
	
	@Lio(cmd = "getString")
	public String testString() {
		System.out.println("++++++++++++test string+++++++++++++++");
		return "dddddddddfvvvv";
	}
}
