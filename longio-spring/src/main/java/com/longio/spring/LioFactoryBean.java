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

import com.zhucode.longio.annotation.LsAutowired;
import com.zhucode.longio.annotation.LsFilter;
import com.zhucode.longio.annotation.Lservice;
import com.zhucode.longio.boot.LongioApplication;
import com.zhucode.longio.conf.AppLookup;
import com.zhucode.longio.conf.CmdLookup;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
@SuppressWarnings("rawtypes")
public class LioFactoryBean implements FactoryBean, InitializingBean {
	
	protected Class<?> objectType;

	protected Object object;
	
	protected Connector connector;
	
	protected CmdLookup cmdLookup;
	protected AppLookup appLookup;

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
			this.object = LongioApplication.getService(connector, objectType, appLookup, cmdLookup);
		} else {
			if (this.objectType.isAnnotationPresent(Lservice.class) || this.objectType.isAnnotationPresent(LsFilter.class)) {
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

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	
	
	public CmdLookup getCmdLookup() {
		return cmdLookup;
	}

	public void setCmdLookup(CmdLookup cmdLookup) {
		this.cmdLookup = cmdLookup;
	}

	public AppLookup getAppLookup() {
		return appLookup;
	}

	public void setAppLookup(AppLookup appLookup) {
		this.appLookup = appLookup;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
