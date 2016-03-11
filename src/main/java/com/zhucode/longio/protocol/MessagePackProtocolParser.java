/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/package com.zhucode.longio.protocol;

import java.io.IOException;

import org.msgpack.MessagePack;

import com.zhucode.longio.exception.ProtocolException;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.message.format.MessagePackData;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MessagePackProtocolParser implements ProtocolParser<byte[]> {

	@Override
	public MessageBlock<byte[]> decode(byte[] bytes)
			throws ProtocolException {
		
		MessagePack mp = new MessagePack();
		try {
			MessagePackData mpd = mp.read(bytes, MessagePackData.class);
			MessageBlock<byte[]> mb = new MessageBlock<byte[]>(mpd.data);
			mb.setCmd(mpd.cmd);
			mb.setSerial(mpd.serial);
			mb.setUid(mpd.uid);
			mb.setStatus(mpd.status);
			mb.setErr(mpd.err);
			return mb;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProtocolException("MessagePack read io error");
		}
	}

	@Override
	public byte[] encode(MessageBlock<?> mb) throws ProtocolException {
		
//		MessagePackData mpd = (MessagePackData)mb.getBody();
//		mpd.cmd = mb.getCmd();
//		mpd.serial = mb.getSerial();
//		MessagePack mp = new MessagePack();
//		try {
//			mpd.data = mp.write(mpd);
//			mp = new MessagePack();
//			return mp.write(mpd);
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new ProtocolException("MessagePack write io error");
//		}
		
		MessagePackData mpd = new MessagePackData();
		mpd.cmd = mb.getCmd();
		mpd.serial = mb.getSerial();
		mpd.uid = mb.getUid();
		mpd.status = mb.getStatus();
		mpd.err = mb.getErr();
		
		Object ret = mb.getBody();
		MessagePack mp = new MessagePack();
		try {
			if (ret instanceof byte[]) {
				mpd.data = (byte[])ret;
			} else {
				mpd.data = mp.write(ret);
			}
			mp = new MessagePack();
			return mp.write(mpd);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProtocolException("MessagePack write io error");
		}
	}

	
	@Override
	public byte[] getHeartBeat() {
		MessagePackData mpd = new MessagePackData();
		mpd.cmd = 0;
		mpd.serial = 0;
		mpd.uid = 0;
		mpd.status = 0;
		mpd.data = new byte[0];
		MessagePack mp = new MessagePack();
		try {
			return mp.write(mpd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
