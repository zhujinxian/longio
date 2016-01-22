/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;

import com.zhucode.longio.annotation.Lfilter;
import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.boot.LongioApplication;
import com.zhucode.longio.message.Dispatcher;
import com.zhucode.longio.reflect.DefaultMethodRefFactory;
import com.zhucode.longio.reflect.MethodRefFactory;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.netty.NettyConnector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
@SuppressWarnings("rawtypes")
public class LioFactoryBean implements FactoryBean, InitializingBean {
	
	protected Class<?> objectType;

	protected Object object;
	
	@Autowired
	protected Environment environment;

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	@Override
	public Object getObject() throws Exception {
		if (this.object == null) {
			createObject();
		}
		return this.object;
	}

	private void createObject() throws Exception {
		
		if (this.objectType.isInterface()) {
			if (!this.objectType.isAnnotationPresent(LsAutowired.class)) {
				throw new Exception("the scaned service interface must be annotated by LsAutowired");
			}
			this.object = LongioApplication.getService(NettyConnector.class, objectType, new EnvProperties(environment));
		} else {
			if (this.objectType.isAnnotationPresent(Lservice.class) || this.objectType.isAnnotationPresent(Lfilter.class)) {
				this.object = this.objectType.newInstance();
			} else {
				throw new Exception("the scaned  class must be annotated by Lservice or Lfilter");
			}
		}
	}

	@Override
	public Class getObjectType() {
		return objectType;
	}
	
	public void setObjectType(Class<?> objectType) {
		this.objectType = objectType;
	}


	@Override
	public boolean isSingleton() {
		return true;
	}

}
