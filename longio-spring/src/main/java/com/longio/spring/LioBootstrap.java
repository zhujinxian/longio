/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.longio.spring.annotation.Boot;
import com.longio.spring.annotation.Boots;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.annotation.LsFilter;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.conf.CmdLookup;
import com.zhucode.longio.message.Dispatcher;
import com.zhucode.longio.message.MessageFilter;
import com.zhucode.longio.message.MethodDispatcher;
import com.zhucode.longio.reflect.DefaultMethodRefFactory;
import com.zhucode.longio.reflect.MethodRefFactory;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.netty.NettyConnector;


/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class LioBootstrap implements ApplicationContextAware {
	
	static Logger logger = LoggerFactory.getLogger(LioBootstrap.class);
	
	String[] basePackages = new String[]{};
	
	@Autowired
	private CmdLookup cmdLookup;
	
	public LioBootstrap(String[] basePackages) {
		this.basePackages = basePackages;
	}
	
	public LioBootstrap(String basePackages) {
		this.basePackages = new String[]{basePackages};
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext app)
			throws BeansException {
		ConfigurableApplicationContext app0 = (ConfigurableApplicationContext)app;
		try {
			boot(app0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void boot(ConfigurableApplicationContext app) throws Exception {
		DefaultListableBeanFactory bf = (DefaultListableBeanFactory)app.getBeanFactory();
		
		for (String name : bf.getBeanDefinitionNames()) {		
			if (name.equalsIgnoreCase("longio.bootstrap")) {
				bootEndpoints(bf, name);
				break;
			}
		}
		
		doScanAndRegist(bf);
		
		resolveLservice(app, bf);
		
		resolveLfilters(app, bf);
	}

	private void resolveLservice(ApplicationContext app, DefaultListableBeanFactory bf) {
		for (String name : bf.getBeanDefinitionNames()) {
			AbstractBeanDefinition bd = (AbstractBeanDefinition)bf.getBeanDefinition(name);
			
			if (!bd.hasBeanClass()) {
				continue;
			}
			
			if (!name.endsWith("Service")) {
				continue;
			}
			
			Class<?> cls = bd.getBeanClass();
			if (cls != LioFactoryBean.class) {
				continue;
			}
			
			Object obj = app.getBean(name);
            app.getAutowireCapableBeanFactory().autowireBean(obj);
			Lservice ls = obj.getClass().getAnnotation(Lservice.class);
			if (ls != null) {
				String pkg = ls.path();
				Connector connector = getConnector(bf);
				for (Dispatcher d : connector.getDispatcheres(pkg)) {
					MethodRefFactory mrf = new DefaultMethodRefFactory(cmdLookup);
					d.registerMethodRefs(mrf.createMethodRefs(obj));
				}
				logger.info("load longio [" + pkg + "] service");
			}
			LsAutowired lsa = obj.getClass().getAnnotation(LsAutowired.class);
			if (lsa != null) {
				String pkg = lsa.path();
				logger.info("load longio  client [" + pkg + "] service");
			}
			
		}
		
	}
	
	
	private void resolveLfilters(ApplicationContext app, DefaultListableBeanFactory bf) {
		
		List<MessageFilter> filters = new ArrayList<MessageFilter>();
		for (String name : bf.getBeanDefinitionNames()) {
			AbstractBeanDefinition bd = (AbstractBeanDefinition)bf.getBeanDefinition(name);
			//System.out.println("+++++++++++++++" + name);
			if (!bd.hasBeanClass()) {
				continue;
			}
			
			if (!name.endsWith("Filter")) {
				continue;
			}
			
			Class<?> cls = bd.getBeanClass();
			if (cls != LioFactoryBean.class) {
				continue;
			}
			
			Object obj = app.getBean(name);
            app.getAutowireCapableBeanFactory().autowireBean(obj);
            LsFilter lf = obj.getClass().getAnnotation(LsFilter.class);
            if (lf != null) {
            	filters.add((MessageFilter)obj);
            }
		}
		
		Connector connector = getConnector(bf);
		for (Dispatcher d : connector.getDispatcheres("*")) {
			d.registerMessageFilters(filters);
			for (MessageFilter filter : filters) {
				logger.info("load longio [" + filter.getClass().getCanonicalName() + "] message filter");
			}
			
		}
		
	}


	private void bootEndpoints(DefaultListableBeanFactory bf, String name) {
		RootBeanDefinition bd = (RootBeanDefinition)bf.getBeanDefinition(name);
		String fbMethod = bd.getFactoryMethodName();
		String fbName = bd.getFactoryBeanName();
		Object fb = bf.getBean(fbName);
		
		if (!bf.containsBeanDefinition("longio.connector")) {
			GenericBeanDefinition bdd = new GenericBeanDefinition();
			bdd.setBeanClass(NettyConnector.class);
			bf.registerBeanDefinition("longio.connector", bdd);
		}
		
		Connector connector = bf.getBean("longio.connector", Connector.class);
		
		Class<?> fbCls = fb.getClass().getSuperclass();
		Method m;
		try {
			m = fbCls.getDeclaredMethod(fbMethod);
			Boots boots = m.getAnnotation(Boots.class);
			if (boots == null) {
				MethodDispatcher dispatcher = new MethodDispatcher();
				Boot b = m.getAnnotation(Boot.class);
				connector.start(b.port(), dispatcher, b.tt(), b.pt(), b.pkg());
				logger.info("connector start at port [" + b.port()
						+  "] with tt = " + b.tt() + " and pt = " + b.pt() + " for pkg = " + b.pkg());
			} else {
				for (Boot b : boots.value()) {
					MethodDispatcher dispatcher = new MethodDispatcher();
					connector.start(b.port(), dispatcher, b.tt(), b.pt(), b.pkg());
					logger.info("connector start at port [" + b.port()
							+  "] with tt = " + b.tt() + " and pt = " + b.pt() + " for pkg = " + b.pkg());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void doScanAndRegist(
			ConfigurableListableBeanFactory beanFactory) {
		LioScanningCandidateComponentProvider provider = new LioScanningCandidateComponentProvider();
		provider.addIncludeFilter(new AnnotationTypeFilter(Lservice.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(LsFilter.class));
		Set<String> LioClassNames = new HashSet<String>();
		for (String pkg : basePackages) {
			Set<BeanDefinition> dfs = provider.findCandidateComponents(pkg);

			for (BeanDefinition beanDefinition : dfs) {
				String LioClassName = beanDefinition.getBeanClassName();
				if (LioClassNames.contains(LioClassName)) {
					continue;
				}
				LioClassNames.add(LioClassName);
				registerLioDefinition(beanFactory, beanDefinition);
			}
		}
	}

	
	/*
	 * 将找到的一个Lio接口注册到Spring容器中
	 */
	private void registerLioDefinition(
			ConfigurableListableBeanFactory beanFactory,
			BeanDefinition beanDefinition) {
		final String LioClassName = beanDefinition.getBeanClassName();
		MutablePropertyValues propertyValues = beanDefinition
				.getPropertyValues();
		/*
		 * 属性及其设置要按 MongoFactoryBean 的要求来办
		 */
		propertyValues.addPropertyValue("objectType", LioClassName);
		
		ScannedGenericBeanDefinition scannedBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
		scannedBeanDefinition.setPropertyValues(propertyValues);
		scannedBeanDefinition.setBeanClass(LioFactoryBean.class);
		scannedBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
		defaultBeanFactory.registerBeanDefinition(LioClassName, beanDefinition);
	}

	private Connector getConnector(ConfigurableListableBeanFactory beanFactory) {
		
		if (beanFactory.containsBeanDefinition("longio.connector")) {
			
			Connector connector = (Connector) beanFactory.getBean(
                     "longio.connector", Connector.class);
			
			return connector;
		}
		return null;
	}



}
