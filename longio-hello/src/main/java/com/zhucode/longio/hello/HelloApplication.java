/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.hello;

import com.zhucode.longio.App;
import com.zhucode.longio.core.client.RpcProxy;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.transport.TransportType;
import com.zhucode.longio.hello.rpc.HelloRpcService;
import com.zhucode.longio.protocol.json.JsonProtocol;
import com.zhucode.longio.protocol.msgpack.MessagePackProtocol;
import com.zhucode.longio.scan.DefaultLongioScanner;
import com.zhucode.longio.scan.LongioScanner;
import com.zhucode.longio.transport.netty.client.NettyClient;
import com.zhucode.longio.transport.netty.client.NettyConnectionFactory;
import com.zhucode.longio.transport.netty.server.NettyServer;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author zhu jinxian
 * @date  2016年9月3日 下午6:52:37 
 * 
 */
public class HelloApplication {
	
	public static void main(String[] args) throws InterruptedException {
		AppLookup appLookup = new HelloAppLookup();
		CmdLookup cmdLookup = new HelloCmdLookup();
		
		NettyConnectionFactory nettyConnectionFactory = new NettyConnectionFactory(new NioEventLoopGroup());
		
		LongioScanner scanner = new DefaultLongioScanner();
		
		NettyServer server = new NettyServer(appLookup, cmdLookup);
		new Thread(()->{
				App app = new App();
				app.setHost(null);
				app.setPort(8000);
				app.setTransportType(TransportType.HTTP);
				app.setProtocol(new MessagePackProtocol());
				server.start(app, scanner);
			}
		).start();
		
		Thread.sleep(5000);
		
		NettyClient client = new NettyClient(appLookup, null, nettyConnectionFactory);
		HelloRpcService helloService = RpcProxy.proxy(HelloRpcService.class, appLookup, cmdLookup, client);
		Thread.sleep(5000);

		helloService.hello();
		int intv = helloService.getInt(1000);
		System.out.println(intv);
		
		String msg = helloService.getString("hello msgpack rpc str");
		System.out.println(msg);
		
		System.out.println("---rpc end---");
		
		Thread.sleep(50000000);
	}
	
}
