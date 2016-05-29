package com.longio.example.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.example.message.UserMsg;
import com.zhucode.longio.exception.LongioException;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;


@LsAutowired(app = "com.lehuihome", path = "com.lehuihome", tt=TransportType.SOCKET, ip="127.0.0.1", port=5002, pt=ProtocolType.MESSAGE_PACK)
public interface HelloService {
	
	@Lio(cmd = "getUser")
	public Map<String, Map<String, UserMsg>> getUser(int userId);

	@Lio(cmd = "getVoid")
	public void testVoid();
	
	@Lio(cmd = "getInt")
	public int testInt(int x);
	
	@Lio(cmd = "getString")
	public String testString(String str);
	
	@Lio(cmd = "getException")
	public void testException() throws LongioException;
	
	@Lio(cmd = "list_to_list")
	public List<String> testLiat(List<String> strs);
	
	@Lio(cmd = "set_to_set")
	public Set<String> testSet(Set<String> strs);

	//@Lio(cmd="str_int_to_js")
	public List<JSONObject> testJsonObject(String k, int v);
	
	//@Lio(cmd="str_int_to_ja")
	public JSONArray testJsonArray(String k, int v);
	
	@Lio(cmd="creat_user")
	public UserMsg createUser();

}
