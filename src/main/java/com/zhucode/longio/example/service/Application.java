/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.example.service;

import java.util.Map;

import com.zhucode.longio.boot.LongioApplication;
import com.zhucode.longio.example.message.UserMsg;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;
import com.zhucode.longio.transport.netty.NettyConnector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class Application {

	public static void main(String[] args) {
		LongioApplication.run(
				NettyConnector.class, 9000, TransportType.HTTP, ProtocolType.JSONARRAY);
		
		LongioApplication.run(
				NettyConnector.class, 9002, TransportType.HTTP, ProtocolType.JSON);
	
		LongioApplication.run(
				NettyConnector.class, 9001, TransportType.SOCKET, ProtocolType.MESSAGE_PACK);
	
		Connector connector = LongioApplication.connectors.get(NettyConnector.class);
		
		ITestClient client = LongioApplication.getService(NettyConnector.class, ITestClient.class);
		Map<String, Map<String, UserMsg>> map = client.getUser(1000);
		System.out.println(map);
		client.testVoid();
		
		System.out.println(client.testInt());
		System.out.println(client.testString());
		
		System.out.println("=============ge-------------");
		
		TestClient1 client1 = LongioApplication.getService(NettyConnector.class, TestClient1.class);
		Map<String, Map<String, UserMsg>> map1 = client1.getUser(1234);
		System.out.println(map1.get("1234").get("1234").user_id);
		
		client1.testVoid();
		System.out.println(client1.testInt());
		System.out.println(client1.testString());
		
//		String[] a = client1.getUser(1234);
//		for (String a0 : a) {
//			System.out.println(a0);
//		}
		
	}

}
