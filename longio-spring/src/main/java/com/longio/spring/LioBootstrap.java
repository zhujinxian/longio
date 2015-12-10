/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.longio.spring.annotation.Boot;
import com.longio.spring.annotation.Boots;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.message.Dispatcher;
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
					MethodRefFactory mrf = new DefaultMethodRefFactory();
					d.registerMethodRefs(mrf.createMethodRefs(obj));
				}
				System.out.println("load longio [" + pkg + "] service");
			}
			LsAutowired lsa = obj.getClass().getAnnotation(LsAutowired.class);
			if (lsa != null) {
				String pkg = lsa.path();
				System.out.println("load longio  client [" + pkg + "] service");
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
				System.out.println("connector start at port [" + b.port()
						+  "] with tt = " + b.tt() + " and pt = " + b.pt() + " for pkg = " + b.pkg());
			} else {
				MethodDispatcher dispatcher = new MethodDispatcher();
				for (Boot b : boots.value()) {
					connector.start(b.port(), dispatcher, b.tt(), b.pt(), b.pkg());
					System.out.println("connector start at port [" + b.port()
							+  "] with tt = " + b.tt() + " and pt = " + b.pt() + " for pkg = " + b.pkg());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void doScanAndRegist(
			ConfigurableListableBeanFactory beanFactory) {
	

		// 1、获取标注Lio标志的资源(ResourceRef)，即classes目录、在/META-INF/lio.properties或/META-INF/MENIFEST.MF配置了lio属性的jar包
		final List<ResourceRef> resources = findLioResources();

		// 2、从获取的资源(resources)中，把lio=*、lio=LIO、lio=lio的筛选出来，并以URL的形式返回
		List<String> urls = findLioResources(resources);

		// 3、从每个URL中找出符合规范的Lio接口，并将之以LioServiceFactoryBean的形式注册到Spring容器中
		findLioDefinitions(beanFactory, urls);
	}

	/*
	 * 找出含有Lio标帜的目录或jar包
	 */
	private List<ResourceRef> findLioResources() {
		final List<ResourceRef> resources;
		try {
			resources = RoseScanner.getInstance()
					.getJarOrClassesFolderResources();
		} catch (IOException e) {
			throw new ApplicationContextException(
					"error on getJarResources/getClassesFolderResources", e);
		}
		return resources;
	}

	/*
	 * 找出含有Lio标识的url
	 */
	private List<String> findLioResources(final List<ResourceRef> resources) {
		List<String> urls = new LinkedList<String>();
		for (ResourceRef ref : resources) {
			if (ref.hasModifier("Lio") || ref.hasModifier("LIO")) {
				try {
					Resource resource = ref.getResource();
					File resourceFile = resource.getFile();
					if (resourceFile.isFile()) {
						urls.add("jar:file:" + resourceFile.toURI().getPath()
								+ ResourceUtils.JAR_URL_SEPARATOR);
					} else if (resourceFile.isDirectory()) {
						urls.add(resourceFile.toURI().toString());
					}
				} catch (IOException e) {
					throw new ApplicationContextException(
							"error on resource.getFile", e);
				}
			}
		}
		return urls;
	}

	/*
	 * 从获得的目录或jar包中寻找出符合规范的Lio接口，并注册到Spring容器中
	 */
	private void findLioDefinitions(
			ConfigurableListableBeanFactory beanFactory, List<String> urls) {
		LioComponentProvider provider = new LioComponentProvider(Lservice.class);
		Set<String> LioClassNames = new HashSet<String>();

		for (String url : urls) {
		
			Set<BeanDefinition> dfs = provider.findCandidateComponents(url);
			

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
