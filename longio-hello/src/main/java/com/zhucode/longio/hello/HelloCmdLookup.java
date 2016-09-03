/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.hello;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.core.conf.CmdLookup;

/**
 * @author zhu jinxian
 * @date  2016年9月3日 下午7:01:23 
 * 
 */
public class HelloCmdLookup implements CmdLookup {

	Logger logger = LoggerFactory.getLogger(HelloCmdLookup.class);
	
	private Map<String, Integer> nameToCmd = new HashMap<String, Integer>();
	private Map<Integer, String> cmdToName = new HashMap<Integer, String>();
	
	private String file = "/cmd.properties";
	
	public HelloCmdLookup() {
		Properties prop = new Properties();
		try {
			InputStream is = this.getClass().getResourceAsStream(file);
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Entry<Object, Object> kv : prop.entrySet()) {
			String key = kv.getKey().toString();
			String val = (String)kv.getValue();
			put(Integer.parseInt(val), key);
		}
		
		put(100, "com.lehuihome.getUser");
		put(101, "com.lehuihome.getUser1");
		put(102, "com.lehuihome.getVoid");
		put(103, "com.lehuihome.getInt");
		put(104, "com.lehuihome.getString");
	}
	
	private void put(int cmd, String name) {
		nameToCmd.put(name, cmd);
		cmdToName.put(cmd, name);
	}
	
	
	
	
	@Override
	public int parseCmd(String name) {
		logger.info("parse cmd [{}] to [{}]", name, nameToCmd.get(name));
		return this.nameToCmd.get(name);
	}
	
	@Override
	public String parseName(int cmd) {
		return this.cmdToName.get(cmd);
	}


}
