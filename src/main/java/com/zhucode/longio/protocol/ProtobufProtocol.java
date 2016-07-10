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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
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
public class ProtobufProtocol implements Protocol {

	@Override
	public MessageBlock decode(byte[] bytes) throws ProtocolException {
		CodedInputStream in = CodedInputStream.newInstance(bytes);
		try {
			long serial = in.readInt64();
			long uid = in.readInt64();
			int cmd = in.readInt32();
			float version = in.readFloat();
			int status = in.readInt32();
			String err = in.readString();
			byte[] data = in.readByteArray();			
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
		}
		return null;
	}

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
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CodedOutputStream out = CodedOutputStream.newInstance(bos);
		
		try {
			out.writeInt64NoTag(serial);
			out.writeInt64NoTag(uid);
			out.writeInt32NoTag(cmd);
			out.writeFloatNoTag(version);
			out.writeInt32NoTag(status);
			out.writeStringNoTag(err);
			out.writeByteArrayNoTag(data);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bbs = bos.toByteArray();
		return bbs;
	}

	@Override
	public byte[] getHeartBeat() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CodedOutputStream out = CodedOutputStream.newInstance(bos);
		
		try {
			out.writeInt64NoTag(0);
			out.writeInt64NoTag(0);
			out.writeInt32NoTag(0);
			out.writeFloatNoTag(0);
			out.writeInt32NoTag(0);
			out.writeStringNoTag("");
			out.writeByteArrayNoTag(new byte[0]);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bos.toByteArray();

	}

	@Override
	public Object[] unpackMethodInvokeParameters(MessageBlock mb, Parameter[] paras) {
		Object[] objs = new Object[paras.length];
		if (objs.length == 0) {
			return objs;
		}
		byte[] body = (byte[])mb.getBody();
		if (body == null || body.length == 0) {
			return objs;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(body);
		DataInputStream dis = new DataInputStream(bis);
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
				if (Message.class.isAssignableFrom(p.getType())) {
					int len = dis.readInt();
					if (len == 0) {
						objs[i] = null;
						continue;
					}
					byte[] bs = new byte[len];
					dis.read(bs);
					Message msg = (Message) p.getType().getMethod("parseFrom", byte[].class).invoke(null, bs);
					objs[i] = msg;
				} else {
					throw new ProtocolException("not supported parameter type for protobuf");
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return objs;
	}
	
	
	@Override
	public Object serializeMethodReturnValue(Object ret) throws SerializeException {
		
		if (ret == null) {
			return null;
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CodedOutputStream out = CodedOutputStream.newInstance(bos);

		try {
			if (Message.class.isAssignableFrom(ret.getClass())) {
				ret.getClass().getMethod("writeTo", CodedOutputStream.class).invoke(ret, out);
			} else {
				throw new ProtocolException("not supported return type for protobuf");
			}
			out.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object packMethodInvokeParameters(MethodInfo mi, Object... args) {
		if (args == null) {
			return null;
		}
		Parameter[] paras = mi.getMethod().getParameters();
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
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
				if (arg == null) {
					dos.writeInt(0);
					continue;
				}
				if (Message.class.isAssignableFrom(pa.getType())) {
					byte[] bs = (byte[])pa.getType().getMethod("toByteArray").invoke(arg);
					dos.writeInt(bs.length);
					dos.write(bs);
				} else {
					throw new ProtocolException("not supported parameter type for protobuf");
				}
			}
	        dos.flush();
	        return bos.toByteArray();
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
		if (ret == null || ret.length == 0) {
			return null;
		}
		try {
			if (Message.class.isAssignableFrom(returnCls)){
				return returnCls.getMethod("parseFrom", byte[].class).invoke(null, ret);
			} else {
				throw new ProtocolException("not supported return type for protobuf");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
