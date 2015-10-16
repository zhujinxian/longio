package com.longio.example.service;

import java.util.Map;

import com.zhucode.longio.annotation.Lio;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.context.parameter.Key;
import com.zhucode.longio.context.parameter.Pack;
import com.zhucode.longio.example.message.UserMsg;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;


@LsAutowired(app = "com.lehuihome", path = "com.lehuihome", tt=TransportType.SOCKET, ip="127.0.0.1", port=5001, pt=ProtocolType.MESSAGE_PACK)
public interface HelloService {
	
	@Lio(cmd = "getUser")
	@Pack("com.zhucode.longio.example.message.UserMsg")
	public Map<String, Map<String, UserMsg>> getUser(@Key("user_id")int userId);

	@Lio(cmd = "getVoid")
	public void testVoid();
	
	@Lio(cmd = "getInt")
	public int testInt();
	
	@Lio(cmd = "getString")
	public String testString();
}
