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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.paoding.rose.scanning.ResourceRef;
import net.paoding.rose.scanning.RoseScanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.transport.Connector;



/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class LongioBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	private final Log logger = LogFactory
			.getLog(LongioBeanFactoryPostProcessor.class);
	
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		doPostProcessBeanFactory(beanFactory);
	}

	private void doPostProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) {
	

		// 1、获取标注Lio标志的资源(ResourceRef)，即classes目录、在/META-INF/lio.properties或/META-INF/MENIFEST.MF配置了lio属性的jar包
		final List<ResourceRef> resources = findLioResources();

		// 2、从获取的资源(resources)中，把lio=*、lio=LIO、lio=lio的筛选出来，并以URL的形式返回
		List<String> urls = findLioResources(resources);

		// 3、从每个URL中找出符合规范的Lio接口，并将之以LioServiceFactoryBean的形式注册到Spring容器中
		findLioDefinitions(beanFactory, urls);

		// 记录结束
		if (logger.isInfoEnabled()) {
			logger.info("[Lio] exits");
		}
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
		if (logger.isInfoEnabled()) {
			logger.info("[Lio] found " + urls.size() + " Lio urls: " + urls);
		}
		return urls;
	}

	/*
	 * 从获得的目录或jar包中寻找出符合规范的Lio接口，并注册到Spring容器中
	 */
	private void findLioDefinitions(
			ConfigurableListableBeanFactory beanFactory, List<String> urls) {
		LioComponentProvider provider = new LioComponentProvider(LsAutowired.class);
		Set<String> LioClassNames = new HashSet<String>();

		for (String url : urls) {
			if (logger.isInfoEnabled()) {
				logger.info("[Lio] call 'Lio/find'");
			}

			Set<BeanDefinition> dfs = provider.findCandidateComponents(url);
			if (logger.isInfoEnabled()) {
				logger.info("[Lio] found " + dfs.size()
						+ " beanDefinition from '" + url + "'");
			}

			for (BeanDefinition beanDefinition : dfs) {
				String LioClassName = beanDefinition.getBeanClassName();
				if (LioClassNames.contains(LioClassName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("[Lio] ignored replicated Lio class: "
								+ LioClassName + "  [" + url + "]");
					}
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
		propertyValues.addPropertyValue("connector", getConnector(beanFactory));
	
		
		ScannedGenericBeanDefinition scannedBeanDefinition = (ScannedGenericBeanDefinition) beanDefinition;
		scannedBeanDefinition.setPropertyValues(propertyValues);
		scannedBeanDefinition.setBeanClass(LioFactoryBean.class);

		DefaultListableBeanFactory defaultBeanFactory = (DefaultListableBeanFactory) beanFactory;
		defaultBeanFactory.registerBeanDefinition(LioClassName, beanDefinition);

		if (logger.isDebugEnabled()) {
			logger.debug("[Lio] register Lio: " + LioClassName);
		}
	}

	private Object getConnector(ConfigurableListableBeanFactory beanFactory) {
		
		if (beanFactory.containsBeanDefinition("longio.connector")) {
			
			Connector connector = (Connector) beanFactory.getBean(
                     "longio.connector", Connector.class);
			
			return connector;
		}
		return null;
	}
}
