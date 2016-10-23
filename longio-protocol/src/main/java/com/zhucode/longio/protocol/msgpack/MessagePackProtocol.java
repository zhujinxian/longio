/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol.msgpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.unpacker.BufferUnpacker;

import com.zhucode.longio.Callback;
import com.zhucode.longio.Protocol;
import com.zhucode.longio.Request;
import com.zhucode.longio.Response;
import com.zhucode.longio.annotation.CMD;
import com.zhucode.longio.annotation.Uid;

/**
 * @author zhu jinxian
 * @date  2016年10月22日 下午7:51:11 
 * 
 */
public class MessagePackProtocol implements Protocol {
	
	private TemplateRegistry tr = new TemplateRegistry(null);

	@Override
	public void decodeRequest(Request request, byte[] bytes) throws ProtocolException {
		MessagePack mp = new MessagePack();
		try {
			BufferUnpacker unpacker = mp.createBufferUnpacker(bytes);
			long serial = unpacker.readLong();
			long uid = unpacker.readLong();
			int cmd = unpacker.readInt();
			float version = unpacker.readFloat();
			byte[] data = unpacker.readByteArray();
			request.setSerial(serial);
			request.setUid(uid);
			request.setCmd(cmd);
			request.setVersion(version);
			request.setBody(data);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}	
	}

	@Override
	public void decodeResponse(Response response, byte[] bytes) throws ProtocolException {
		MessagePack mp = new MessagePack();
		try {
			BufferUnpacker unpacker = mp.createBufferUnpacker(bytes);
			long serial = unpacker.readLong();
			int cmd = unpacker.readInt();
			float version = unpacker.readFloat();
			int status = unpacker.readInt();
			String err = unpacker.readString();
			byte[] data = unpacker.readByteArray();
			response.setSerial(serial);
			response.setCmd(cmd);
			response.setVersion(version);
			response.setStatus(status);
			response.setErr(err);
			response.setBody(data);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}	

	}

	@Override
	public byte[] encodeRequest(Request request) throws ProtocolException {
		long serial = request.getSerial();
		long uid = request.getUid();
		int cmd = request.getCmd();
		float version = request.getVersion();
		byte[] data = (byte[])request.getBody();
		if (data == null) {
			data = new byte[0];
		}
		try { 
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(serial);
			packer.write(uid);
			packer.write(cmd);
			packer.write(version);
			packer.write(data);
			return packer.toByteArray();
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public byte[] encodeResponse(Response response) throws ProtocolException {
		long serial = response.getSerial();
		int cmd = response.getCmd();
		float version = response.getVersion();
		int status = response.getStatus();
		String err = response.getErr();
		byte[] data = (byte[])response.getBody();
		if (data == null) {
			data = new byte[0];
		}
		try { 
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(serial);
			packer.write(cmd);
			packer.write(version);
			packer.write(status);
			packer.write(err);
			packer.write(data);
			return packer.toByteArray();
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public Object[] deserializeParameters(Method method, Request request, Response response) throws SerializeException {
		Parameter[] paras = method.getParameters();
		Object[] objs = new Object[paras.length];
		byte[] data = (byte[])request.getBody();
		MessagePack mp = new MessagePack();
		BufferUnpacker unpacker = mp.createBufferUnpacker(data);
		for (int i = 0; i < paras.length; i++) {
			Parameter p = paras[i];
			if (p.getType().isAssignableFrom(Request.class)) {
				objs[i] = request;
				continue;
			}
			if (p.getType().isAssignableFrom(Response.class)) {
				objs[i] = response;
				continue;
			}
			objs[i] = parseObject(p.getType(), p.getParameterizedType(), unpacker);
		}
		return objs;
	}

	private Object parseObject(Class<?> type, Type parameterizedType, BufferUnpacker unpacker) {
		try {
			Type t = parameterizedType == null? type: parameterizedType;
			return unpacker.read(parseTemplate(t));
		} catch (Exception e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public Object serializeParameters(Method method, Object... args) throws SerializeException {
		Parameter[] paras = method.getParameters();
		try {
			MessagePack mp = new MessagePack();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        Packer packer = mp.createPacker(out);
	        for (int i = 0; i < paras.length; i++) {
	        	Parameter pa = paras[i];
	        	Object arg = args[i];
	        	if (arg instanceof Callback) {
					continue;
				}
	    		if (pa.getType().isAnnotationPresent(Uid.class)) {
					continue;
				}
				if (pa.getType().isAnnotationPresent(CMD.class)) {
					continue;
				}
				packer.write(arg);
			}
	        return out.toByteArray();
		} catch (Exception e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public Object serializeReturnValue(Method method, Object ret) throws SerializeException {
		try { 
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(ret);
			return packer.toByteArray();
		} catch (IOException e) {
			throw new ProtocolException(e);
		}

	}

	@Override
	public Object deserializeReturnValue(Method method, Object msg) throws SerializeException {
		Type returnType = method.getGenericReturnType();
		if (returnType == Void.TYPE || msg == null) {
			return null;
		}
	
		byte[] ret = (byte[])msg;
		
		MessagePack mp = new MessagePack();
		try {
			return mp.read(ret, parseTemplate(returnType));
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public byte[] getHeartBeat() {
		try {
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(0);
			packer.write(0);
			packer.write(0.0);
			packer.write(0);
			packer.write("");
			packer.write(new byte[0]);
			return packer.toByteArray();
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}
	
	private Template<?> parseTemplate(Type type) {
		return tr.lookup(type);
	}


}
