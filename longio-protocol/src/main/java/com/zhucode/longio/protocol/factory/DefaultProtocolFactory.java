package com.zhucode.longio.protocol.factory;
import java.util.Map;

import com.google.common.collect.Maps;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.protocol.json.JsonProtocol;
import com.zhucode.longio.protocol.msgpack.MessagePackProtocol;

/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

/**
 * @author zhu jinxian
 * @date  2017年2月3日 上午1:12:02 
 * 
 */
public class DefaultProtocolFactory {
	
	static Map<Class<? extends Protocol>, Protocol> protocols = Maps.newHashMap();
	
	static {
		protocols.put(JsonProtocol.class, new JsonProtocol());
		protocols.put(MessagePackProtocol.class, new MessagePackProtocol());
	}
	
	public static Protocol getProtocol(Class<? extends Protocol> protocol) {
		return protocols.get(protocol);
	}

}
