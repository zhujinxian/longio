/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.longio.spring.annotation.Boot;
import com.longio.spring.annotation.Boots;
import com.zhucode.longio.App;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.boot.ServerHandler;
import com.zhucode.longio.protocol.factory.DefaultProtocolFactory;

/**
 * @author zhu jinxian
 * @date  2016年11月13日 下午9:49:52 
 * 
 */
public class RpcBootstrap implements ApplicationContextAware {
	
	private Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);
	
	private ApplicationContext applicationContext;
		
	public RpcBootstrap() {
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void start() throws Exception {
		ServerHandler server = this.applicationContext.getBean(RpcBeanFactoryPostProcessor.class).getServerHandler();
		ConfigurableListableBeanFactory beanFactory =  ((ConfigurableApplicationContext)this.applicationContext).getBeanFactory();
		BeanDefinition factoryDefinition = beanFactory.getBeanDefinition("rpcBootstrap");
		
		Class<?> factoryClass = beanFactory.getType(factoryDefinition.getFactoryBeanName()).getSuperclass();
		Method bootMethod = ReflectionUtils.findMethod(factoryClass,
				factoryDefinition.getFactoryMethodName());

		Boots boots = bootMethod.getAnnotation(Boots.class);
		if (boots == null) {
			Boot boot = bootMethod.getAnnotation(Boot.class);
			if (boot == null) {
				return;
			}
			App app = new App();
			app.setHost(boot.host());
			app.setPort(boot.port());
			Protocol protocol = DefaultProtocolFactory.getProtocol(boot.protocol());
			if (protocol == null) {
				protocol = this.applicationContext.getBean(boot.protocol());
			}
			app.setProtocol(protocol);
			app.setTransportType(boot.transport());
			server.start(app);
			return;
		}
		
		for (Boot boot : boots.value()) {
			App app = new App();
			app.setHost(boot.host());
			app.setPort(boot.port());
			Protocol protocol = DefaultProtocolFactory.getProtocol(boot.protocol());
			if (protocol == null) {
				protocol = this.applicationContext.getBean(boot.protocol());
			}
			app.setProtocol(protocol);
			app.setTransportType(boot.transport());
			server.start(app);
		}
	}

}
