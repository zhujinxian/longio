/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.hello.rpc;

import com.zhucode.longio.annotation.Rpc;
import com.zhucode.longio.annotation.RpcService;
import com.zhucode.longio.protocol.json.JsonProtocol;

/**
 * @author zhu jinxian
 * @date  2016年10月7日 下午8:25:52 
 * 
 */
@RpcService(app = "", ip = "127.0.0.1", port = 8000, path = "com.zhucode", protocolClass = JsonProtocol.class)
public interface HelloRpcService {
	
	@Rpc(cmd = "hello")
	public void hello();
	
	@Rpc(cmd = "int")
	public int getInt(int val);

	@Rpc(cmd = "str")
	public String getString(String val);

}
