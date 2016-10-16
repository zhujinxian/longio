/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.conf.CmdLookup;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.netty.NettyConnector;



/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class LongioBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	private final Log logger = LogFactory
			.getLog(LongioBeanFactoryPostProcessor.class);
	
	AppLookup appLookup;
	
	CmdLookup cmdLookup;
	
	String[] basePackages;
	
	public LongioBeanFactoryPostProcessor(AppLookup appLookup, CmdLookup cmdLookup, String basePackages) {
		this.appLookup = appLookup;
		this.cmdLookup = cmdLookup;
		this.basePackages = new String[]{basePackages};
	}
	
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		doPostProcessBeanFactory(beanFactory);
	}

	private void doPostProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) {
		LioScanningCandidateComponentProvider provider = new LioScanningCandidateComponentProvider();
		provider.addIncludeFilter(new AnnotationTypeFilter(LsAutowired.class));
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
		propertyValues.addPropertyValue("connector", getConnector((DefaultListableBeanFactory)beanFactory));
		propertyValues.addPropertyValue("appLookup", appLookup);
		propertyValues.addPropertyValue("cmdLookup", cmdLookup);
		
		ScannedGenericBeanDefinition scannedBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
		scannedBeanDefinition.setPropertyValues(propertyValues);
		scannedBeanDefinition.setBeanClass(LioFactoryBean.class);

		DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
		defaultBeanFactory.registerBeanDefinition(LioClassName, beanDefinition);

		if (logger.isDebugEnabled()) {
			logger.debug("[Lio] register Lio: " + LioClassName);
		}
	}

	private Object getConnector(DefaultListableBeanFactory bf) {
		if (!bf.containsBeanDefinition("longio.connector")) {
			GenericBeanDefinition bdd = new GenericBeanDefinition();
			bdd.setBeanClass(NettyConnector.class);
			bf.registerBeanDefinition("longio.connector", bdd);
		}
		
		if (bf.containsBeanDefinition("longio.connector")) {
			
			Connector connector = (Connector) bf.getBean(
                     "longio.connector", Connector.class);
			
			return connector;
		}
		return null;
	}
}
