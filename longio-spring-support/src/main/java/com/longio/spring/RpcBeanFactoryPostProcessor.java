/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.zhucode.longio.boot.ClientHandler;
import com.zhucode.longio.boot.ServerHandler;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.transport.netty.server.NettyServer;

/**
 * @author zhu jinxian
 * @date  2016年11月13日 下午10:15:53 
 * Ø
 */
public class RpcBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {
	
	private Logger logger = LoggerFactory.getLogger(RpcBeanFactoryPostProcessor.class);
	
	private AppLookup appLookup;
	
	private CmdLookup cmdLookup;
	
	private String[] basePackages;
	
	private ClientHandler clientHandler;
	
	protected ServerHandler serverHandler;

	private ApplicationContext applicationContext;
	
	public RpcBeanFactoryPostProcessor(AppLookup appLookup, CmdLookup cmdLookup, ClientHandler clientHandler, String... basePackages) {
		this.appLookup = appLookup;
		this.cmdLookup = cmdLookup;
		this.clientHandler = clientHandler;
		this.basePackages = basePackages;
		this.serverHandler = new NettyServer(appLookup, cmdLookup);
	}
	
	public RpcBeanFactoryPostProcessor(AppLookup appLookup, CmdLookup cmdLookup, String... basePackages) {
		this(appLookup, cmdLookup, null, basePackages);
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		RpcScanner rpcScanner = new RpcScanner((BeanDefinitionRegistry)beanFactory, appLookup, cmdLookup, 
				serverHandler, clientHandler);
		rpcScanner.setResourceLoader(applicationContext);
		rpcScanner.scan(basePackages);
	}
	
	public ServerHandler getServerHandler() {
		return this.serverHandler;
	}

}
