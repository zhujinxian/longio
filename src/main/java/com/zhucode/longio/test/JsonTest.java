/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class JsonTest {
	
	
	public static List<Item> getItems() {return null;}
	
	public static Item getItem() {return null;}

	

	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		
		User u = new User();
		u.id = 122;
		u.name = "ddfffgg";
		
		Item it = new Item();
		it.des = "ffkllkkepopeoe";
		
		List<Item> items = new ArrayList<Item>();
		items.add(it);
		items.add(it);
		
		u.items = items;
		
		String js = JSON.toJSONString(u);
		
		JSONObject jso = JSONObject.parseObject(js);
		
		Method m = JsonTest.class.getMethod("getItems");
		
		System.out.println(m.getReturnType());
		System.out.println(m.getGenericReturnType());
		
		
		
		Date d = new Date();
		JSON.parseObject("10", Integer.class);
		
	}

}
