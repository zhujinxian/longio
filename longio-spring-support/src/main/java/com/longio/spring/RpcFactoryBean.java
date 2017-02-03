/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.zhucode.longio.annotation.Rpc;
import com.zhucode.longio.annotation.RpcController;
import com.zhucode.longio.annotation.RpcService;
import com.zhucode.longio.boot.ClientHandler;
import com.zhucode.longio.boot.ServerHandler;
import com.zhucode.longio.core.client.RpcProxy;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.server.MethodHandler;

/**
 * @author zhu jinxian
 * @date  2016年11月13日 下午10:31:10 
 * 
 */
public class RpcFactoryBean implements FactoryBean<Object> {

	protected Class<?> objectType;

	protected Object object;
		
	protected CmdLookup cmdLookup;
	
	protected AppLookup appLookup;
	
	protected ClientHandler clientHandler;
	
	protected ServerHandler serverHandler;

	@Override
	public Object getObject() throws Exception {
		if (this.object == null) {
			createObject();
		}
		return this.object;
	}

	private void createObject() throws InstantiationException, IllegalAccessException {
		if (this.objectType.isInterface() 
				&& this.objectType.isAnnotationPresent(RpcService.class)) {
			this.object = RpcProxy.proxy(this.objectType, appLookup, cmdLookup, clientHandler);
		} else if (this.objectType.isAnnotationPresent(RpcController.class)) {
			this.object = this.objectType.newInstance();
			registerMethodHandlers();
		}
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public boolean isSingleton() {
		return false;
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

	public void setObjectType(Class<?> objectType) {
		this.objectType = objectType;
	}

	public void setObject(Object object) {
		this.object = object;
	}
	
	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public void setServerHandler(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
	}

	private void registerMethodHandlers() {
		Map<Integer, MethodHandler> map = new HashMap<Integer, MethodHandler>();
		RpcController ls = this.objectType.getAnnotation(RpcController.class);
		for (Method m : this.objectType.getMethods()) {
			Rpc lio = m.getAnnotation(Rpc.class);
			if (lio == null) {
				continue;
			}
			String cmdName = ls.path() + "." + lio.cmd();
			cmdName = cmdName.replaceAll("\\.\\.", ".");
			int cmd = cmdLookup.parseCmd(cmdName);
			boolean asy = lio.asy();
			boolean reply = lio.reply();
			MethodHandler mih = new MethodHandler(cmd, cmdName, this.object, m, asy, reply);
			map.put(cmd, mih);
		}
		this.serverHandler.registerMethodHandlers(map);
	}

	
}
