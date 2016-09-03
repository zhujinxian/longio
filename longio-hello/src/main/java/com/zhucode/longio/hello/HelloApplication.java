/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.hello;

import java.util.Arrays;

import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.server.MethodRouter;
import com.zhucode.longio.core.support.MethodRouterFactory;
import com.zhucode.longio.core.transport.ProtocolType;
import com.zhucode.longio.core.transport.TransportType;
import com.zhucode.longio.protocol.json.JsonProtocol;
import com.zhucode.longio.transport.netty.server.NettyServer;

/**
 * @author zhu jinxian
 * @date  2016年9月3日 下午6:52:37 
 * 
 */
public class HelloApplication {
	
	public static void main(String[] args) {
		
		CmdLookup cmdLookup = new HelloCmdLookup();
		MethodRouterFactory mrf = new MethodRouterFactory(cmdLookup);
		
		MethodRouter router = mrf.createRouter("", Arrays.asList());
				
		NettyServer server = new NettyServer(router, null, 8000, TransportType.HTTP, new JsonProtocol());
		
		server.start();
	}
	
}
