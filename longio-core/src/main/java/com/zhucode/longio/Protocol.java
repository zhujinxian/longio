/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

/**
 * @author zhu jinxian
 * @date  2016年08月13日
 * 
 */
public interface Protocol {
	
	void decodeRequest(Request request, byte[] bytes) throws ProtocolException;
	
	void decodeResponse(Response response, byte[] bytes) throws ProtocolException;
	
	byte[] encodeRequest(Request request) throws ProtocolException;
	
	byte[] encodeResponse(Response response) throws ProtocolException;

	Object[] deserializeParameters(Method method, Request request, Response response) throws SerializeException;
	
	Object serializeParameters(Method method, Object... args) throws SerializeException;

	Object serializeReturnValue(Method method, Object ret) throws SerializeException;
	
	Object deserializeReturnValue(Method method, Object msg) throws SerializeException;
	
	byte[] getHeartBeat();
	
	@SuppressWarnings("serial")
	public static class SerializeException extends RuntimeException {
		public SerializeException(Exception e) {
			super(e);
		}

	}
	
	@SuppressWarnings("serial")
	public static class ProtocolException extends RuntimeException {

		public ProtocolException(Exception e) {
			super(e);
		}
		
	}


}
