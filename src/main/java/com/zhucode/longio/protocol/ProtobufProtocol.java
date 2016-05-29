/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import javax.swing.text.FlowView.FlowStrategy;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.zhucode.longio.client.reflect.MethodInfo;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.exception.SerializeException;
import com.zhucode.longio.exception.UnsupportedException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.format.Proto;
import com.zhucode.longio.transport.Connector;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProtobufProtocol implements Protocol {

	@Override
	public MessageBlock decode(byte[] bytes) throws ProtocolException {
		CodedInputStream in = ByteString.copyFrom(bytes).newCodedInput();
		try {
			long serial = in.readInt64();
			long uid = in.readFixed64();
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
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CodedOutputStream out = CodedOutputStream.newInstance(bos);
		
		try {
			int i = 1;
			out.writeInt64(i++, serial);
			out.writeInt64(i++, uid);
			out.writeInt32(i++, cmd);
			out.writeFloat(i++, version);
			out.writeInt32(i++, status);
			out.writeString(i++, err);
			out.writeByteArray(i++, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bos.toByteArray();
	}

	@Override
	public byte[] getHeartBeat() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CodedOutputStream out = CodedOutputStream.newInstance(bos);
		
		try {
			int i = 1;
			out.writeInt64(i++, 0);
			out.writeInt64(i++, 0);
			out.writeInt32(i++, 0);
			out.writeFloat(i++, 0);
			out.writeInt32(i++, 0);
			out.writeString(i++, "");
			out.writeByteArray(i++, new byte[0]);
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
		CodedInputStream in = ByteString.copyFrom(body).newCodedInput();
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
				if (p.getType().isAssignableFrom(Message.class)){
					byte[] bts = in.readByteArray();
					Message msg = (Message) p.getType().getMethod("parseFrom").invoke(null, bts);
					objs[i] = msg;
				} else {
					objs[i] = parsePrimitive(p.getType(), in);
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return objs;
	}
	
	

	private Object parsePrimitive(Class<?> type, CodedInputStream in) throws IOException {
		if (type == Byte.class) {
			return (byte)in.readInt32();
		}
		if (type == Short.class) {
			return (short)in.readInt32();
		}
		if (type == Integer.class) {
			return (short)in.readInt32();
		}
		
		if (type == Long.class) {
			return in.readInt64();
		}
		
		if (type == Float.class) {
			return in.readFloat();
		}
		
		if (type == Double.class) {
			return in.readDouble();
		}

		if (type == String.class) {
			return in.readString();
		}
		
		return null;
	}

	@Override
	public Object serializeMethodReturnValue(Object ret) throws SerializeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object packMethodInvokeParameters(MethodInfo mi, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object deserializeMethodReturnValue(Class<?> returnCls, Type type, Object msg) throws UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
