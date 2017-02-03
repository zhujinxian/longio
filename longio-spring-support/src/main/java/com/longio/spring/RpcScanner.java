/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.zhucode.longio.annotation.RpcController;
import com.zhucode.longio.annotation.RpcService;
import com.zhucode.longio.boot.ClientHandler;
import com.zhucode.longio.boot.ServerHandler;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;

/**
 * @author zhu jinxian
 * @date  2017年2月1日 下午11:30:42 
 * 
 */
public class RpcScanner extends ClassPathBeanDefinitionScanner {
	
	private AppLookup appLookup;
	
	private CmdLookup cmdLookup;
		
	private ClientHandler clientHandler;

	private ServerHandler serverHandler;

	public RpcScanner(BeanDefinitionRegistry registry, AppLookup appLookup, CmdLookup cmdLookup, 
			ServerHandler serverHandler, ClientHandler clientHandler) {
		super(registry);
		this.appLookup = appLookup;
		this.cmdLookup = cmdLookup;
		this.serverHandler = serverHandler;
		this.clientHandler = clientHandler;
		if (clientHandler != null) {
			this.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
		}
		if (serverHandler != null) {
			this.addIncludeFilter(new AnnotationTypeFilter(RpcController.class));
		}

	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions =   super.doScan(basePackages);
	       for (BeanDefinitionHolder holder : beanDefinitions) {
	           GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
	           
	           Class<?> clazz = null;
	           try {
	        	   clazz = Class.forName(definition.getBeanClassName());
	           } catch (ClassNotFoundException e) {
	        	   e.printStackTrace();
	           }
	           MutablePropertyValues propertyValues = definition.getPropertyValues();
	           /*
	            * 属性及其设置要按 MongoFactoryBean 的要求来办
	            */
	           propertyValues.addPropertyValue("objectType", clazz);
	           propertyValues.addPropertyValue("appLookup", appLookup);
	           propertyValues.addPropertyValue("cmdLookup", cmdLookup);
	           propertyValues.addPropertyValue("serverHandler", serverHandler);
	           propertyValues.addPropertyValue("clientHandler", clientHandler);
			
	           definition.setPropertyValues(propertyValues);
	           definition.setBeanClass(RpcFactoryBean.class);
	       }
		return beanDefinitions;
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return true;
	}

	
	

}
