package com.longio.example.service;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.context.parameter.Key;
import com.zhucode.longio.context.parameter.Unpack;
import com.zhucode.longio.example.message.Res;
import com.zhucode.longio.example.message.UserMsg;

@Lservice(path = "com.lehuihome")
public class ExeHelloService {
	@Lio(cmd = "getUser")
	@Unpack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>>getUser(@Key("user_id")long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		JSONObject ret = new JSONObject();
		ret.put("status", "success");
		Map<String, UserMsg> rm = new HashMap<String, UserMsg>();
		UserMsg um = new UserMsg();
		um.user_id = 9999;
		rm.put("1234", um);
		
		Map<String, Map<String, UserMsg>> m = new HashMap<String, Map<String, UserMsg>>();
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
