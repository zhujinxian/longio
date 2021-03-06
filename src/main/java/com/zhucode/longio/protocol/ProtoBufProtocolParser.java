/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.protocol;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.format.Proto;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class ProtoBufProtocolParser {


	public MessageBlock decode(byte[] bytes) throws ProtocolException {
		try {
			Proto.Message data = Proto.Message.parseFrom(bytes);
			int cmd = data.getCmd();
			long serial = data.getSerial();
			int uid = data.getUid();
			int status = data.getStatus();
			String err = data.getErr();
			MessageBlock mb = new MessageBlock(data);
			mb.setCmd(cmd);
			mb.setSerial(serial);
			mb.setUid(uid);
			mb.setStatus(status);
			mb.setErr(err);
			return mb;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			
		}
		return null;
	}

	public byte[] encode(MessageBlock mb) throws ProtocolException {
		int cmd = mb.getCmd();
		long serial = mb.getSerial();
		long uid = mb.getUid();
		int status = mb.getStatus();
		String err = mb.getErr();
		
		if (mb.getBody() instanceof Message) {
			Message m = (Message)mb.getBody();
			Proto.Message data = Proto.Message.newBuilder()
					.setCmd(cmd).setSerial(serial).setUid((int)uid)
					.setStatus(status).setErr(err).setBody(m.toByteString()).build();
			return data.toByteArray();
		} else {
			throw new ProtocolException("the only type must be void or " + Message.class.getCanonicalName());
		}
		
	}

	
	
	public byte[] getHeartBeat() {
		Proto.Message data = Proto.Message.newBuilder()
				.setCmd(0).setSerial(0).setUid(0).setStatus(0).setBody(ByteString.EMPTY).build();
		return data.toByteArray();
	}
	
}
