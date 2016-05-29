/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;

import com.zhucode.longio.client.reflect.MethodCallback;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.context.parameter.CMD;
import com.zhucode.longio.context.parameter.Uid;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.exception.SerializeException;
import com.zhucode.longio.exception.UnsupportedException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessagePackProtocol implements Protocol {
	
	TemplateRegistry tr = new TemplateRegistry(null);

	@Override
	public MessageBlock decode(byte[] bytes) throws ProtocolException {
		MessagePack mp = new MessagePack();
		try {
			BufferUnpacker unpacker = mp.createBufferUnpacker(bytes);
			long serial = unpacker.readLong();
			long uid = unpacker.readLong();
			int cmd = unpacker.readInt();
			float version = unpacker.readFloat();
			int status = unpacker.readInt();
			String err = unpacker.readString();
			byte[] data = unpacker.readByteArray();
			MessageBlock mb = new MessageBlock(data);
			mb.setCmd(cmd);
			mb.setSerial(serial);
			mb.setUid(uid);
			mb.setVersion(version);
			mb.setStatus(status);
			mb.setErr(err);
			return mb;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProtocolException("MessagePack read io error");
		}	}

	@Override
	public byte[] encode(MessageBlock mb) throws ProtocolException {	
		long serial = mb.getSerial();
		long uid = mb.getUid();
		int cmd = mb.getCmd();
		float version = mb.getVersion();
		int status = mb.getStatus();
		String err = mb.getErr();
		byte[] data = (byte[])mb.getBody();
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
			packer.write(status);
			packer.write(err);
			packer.write(data);
			return packer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProtocolException("MessagePack write io error");
		}
	}

	
	@Override
	public byte[] getHeartBeat() {
		try {
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(0);
			packer.write(0);
			packer.write(0);
			packer.write(0);
			packer.write(0);
			packer.write("");
			packer.write(new byte[0]);
			return packer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object[] unpackMethodInvokeParameters(MessageBlock mb, Parameter[] paras) {
		Object[] objs = new Object[paras.length];
		
		if (objs.length == 0) {
			return objs;
		}
		byte[] body = (byte[])mb.getBody();
		MessagePack mp = new MessagePack();
		ByteArrayInputStream in = new ByteArrayInputStream(body);
	    Unpacker unpacker = mp.createUnpacker(in);
		for (int i = 0; i < paras.length; i++) {
			Parameter p = paras[i];
			if (p.getType() == MessageBlock.class) {
				objs[i] = mb;
				continue;
			}
			if (p.getType() == Connector.class) {
				objs[i] = mb.getConnector();
				continue;
			}
			try {
				Type type = p.getParameterizedType() == null? p.getType() : p.getParameterizedType();
				objs[i] = unpacker.read(parseTemplate(type));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return objs;
	}
	
	private Template<?> parseTemplate(Type type) {
		return tr.lookup(type);
	}


	@Override
	public Object serializeMethodReturnValue(Object ret) throws SerializeException {
		try { 
			MessagePack mp = new MessagePack();
			BufferPacker packer = mp.createBufferPacker();
			packer.write(ret);
			return packer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SerializeException("MessagePack write io error");
		}
	}

	@Override
	public Object packMethodInvokeParameters(MethodInfo mi, Object... args) {
		if (args == null) {
			return null;
		}
		Parameter[] paras = mi.getMethod().getParameters();
		try {

			MessagePack mp = new MessagePack();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        Packer packer = mp.createPacker(out);
	        for (int i = 0; i < paras.length; i++) {
	        	
	        	Parameter pa = paras[i];
	        	Object arg = args[i];
	        	
	        	if (arg instanceof MethodCallback) {
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
			e.printStackTrace();
		}
	
		return null;
	}

	@Override
	public Object deserializeMethodReturnValue(Class<?> returnCls, Type returnType, Object msg) throws UnsupportedException {
		if (returnType == Void.TYPE) {
			return null;
		}
	
		byte[] ret = (byte[])msg;
		
		MessagePack mp = new MessagePack();
		try {
			return mp.read(ret, parseTemplate(returnCls, returnType));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Template<?> parseTemplate(Class<?> returnCls, Type returnType) {
		return tr.lookup(returnType);
	}

}
