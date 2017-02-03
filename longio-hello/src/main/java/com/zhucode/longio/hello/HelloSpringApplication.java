/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.longio.spring.RpcBeanFactoryPostProcessor;
import com.longio.spring.RpcBootstrap;
import com.longio.spring.annotation.Boot;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.transport.TransportType;
import com.zhucode.longio.protocol.json.JsonProtocol;
import com.zhucode.longio.protocol.msgpack.MessagePackProtocol;
import com.zhucode.longio.transport.netty.client.NettyClient;
import com.zhucode.longio.transport.netty.client.NettyConnectionFactory;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author zhu jinxian
 * @date  2017年2月3日 上午1:37:25 
 * 
 */
@SpringBootApplication
public class HelloSpringApplication implements CommandLineRunner {
	
	@Bean(name="appLookup")
	AppLookup applookup() {
		return new HelloAppLookup();
	}
	
	@Bean(name="cmdLookup")
	CmdLookup cmdlookup() {
		return new HelloCmdLookup();
	}
	
	@Bean
	RpcBeanFactoryPostProcessor rpcProcessor(
			@Qualifier("appLookup") AppLookup appLookup, 
            @Qualifier("cmdLookup")CmdLookup cmdLookup) {
		
		NettyClient client = new NettyClient(appLookup, null, 
				new NettyConnectionFactory(new NioEventLoopGroup()));

		return new RpcBeanFactoryPostProcessor(appLookup, cmdLookup, client, "com.zhucode.longio.hello");
	}
	
	@Boot(host = "127.0.0.1", port = 8000, protocol = JsonProtocol.class, transport = TransportType.HTTP)
	@Bean(name="rpcBootstrap")
	RpcBootstrap bootstrap() {
		return new RpcBootstrap();
	}
	

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext  ctx = SpringApplication.run(HelloSpringApplication.class, args);
		ctx.getBean(AppLookup.class).discovery("");

		ctx.getBean(RpcBootstrap.class).start();
		
		Thread.currentThread().sleep(100000);
	}

	@Override
	public void run(String... args) throws Exception {
	}

}
