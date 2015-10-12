/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.example.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class MsgPackTest {
	
	@Message
	static class Head {
		public String big;
		public String small;
	}
	
	@Message
	static class User {
		public int id;
		public String name;
		public List<Head> heads;
	}
	
	public static void main(String[] args) throws IOException {
		
		User user = new User();
		user.id = 100;
		user.name = "dddd";
		user.heads = new ArrayList<Head>();
		Head head = new Head();
		head.big = "big";
		head.small = "small";
		user.heads.add(head);
		
		MessagePack mp = new MessagePack();
		byte[] bytes = mp.write(user);
		
		User u = mp.read(bytes, User.class);
		
		System.out.println(u.heads.get(0).small);
	}

}
