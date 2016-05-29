package com.longio.example.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.example.message.Res;
import com.zhucode.longio.example.message.UserMsg;
import com.zhucode.longio.exception.LongioException;

@Lservice(path = "com.lehuihome")
public class ExeHelloService {
	@Lio(cmd = "getUser")
	public Map<String, Map<String, UserMsg>>getUser(long userId) {
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
	public Res.Data getUser1(long userId) {
		System.out.println("++++++++++++++++++++++++++++++++++++++");
		return Res.Data.newBuilder().setStatus("success").build();
	}
	
	@Lio(cmd = "getVoid")
	public void testVoid() {
		System.out.println("++++++++++++test void+++++++++++++++");
	}
	
	@Lio(cmd = "getInt", asy=false)
	public int testInt(int x) {
//		System.out.println("++++++++++++test int+++++++++++++++");
		return x;
	}
	
	@Lio(cmd = "getString")
	public String testString(String string) {
//		System.out.println("++++++++++++test string+++++++++++++++");
		int a = 0;
		//a = a /a;
		return string;
	}
	
	@Lio(cmd = "getStringAsy", asy=false)
	public String testStringAsy(String string) {
//		System.out.println("++++++++++++test string+++++++++++++++");
		return string;
	}
	
	@Lio(cmd = "getException")
	public void testException() throws LongioException {
//		System.out.println("++++++++++++test string+++++++++++++++");
		throw new LongioException(1111, "test exception");
	}
	
	@Lio(cmd = "list_to_list")
	public List<String> testLiat(List<String> strs)  {
//		System.out.println("++++++++++++test string+++++++++++++++");
		return strs;
	}
	
	@Lio(cmd = "set_to_set")
	public Set<String> testSet(Set<String> strs) {
		return strs;
	}
	
	@Lio(cmd="str_int_to_js")
	public List<JSONObject> testJsonObject(String k, int v) {
		JSONObject js = new JSONObject();
		js.put(k, v);
		return Arrays.asList(js);
	}
	
	@Lio(cmd="str_int_to_ja")
	public JSONArray testJsonArray(String k, int v) {
		JSONArray js = new JSONArray();
		js.add(k);
		js.add(v);
		return js;
	}
	
	@Lio(cmd="creat_user")
	public UserMsg createUser() {
		return new UserMsg();
	}


}
