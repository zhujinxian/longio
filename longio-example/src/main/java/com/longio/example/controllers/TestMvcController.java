/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.longio.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.longio.example.service.HelloService;
import com.zhucode.longio.exception.LongioException;

/**
 * @author zhu jinxian
 * @date  2016年2月25日
 * 
 */

@Controller
@RequestMapping(value="/test")
public class TestMvcController {
	
	@Autowired
	HelloService service;
	
	@ResponseBody
	@RequestMapping(value = "hello", method = RequestMethod.GET)
	public String testVoid() {
		service.testVoid();
		return "========test void=============";
	}
	
	@ResponseBody
	@RequestMapping(value = "int", method = RequestMethod.GET)
	public String testInt() {
		
		return "@========test int=============\n" + service.testInt();
	}
	
	@ResponseBody
	@RequestMapping(value = "string", method = RequestMethod.GET)
	public String testString() {
		
		return "@========test string=============\n" + service.testString();
	}
	
	@ResponseBody
	@RequestMapping(value = "map", method = RequestMethod.GET)
	public String testMap(@RequestParam(value="userId") int userId) {
		return "@========getUser=============\n" + service.getUser(userId);
	}
	
	@ResponseBody
	@RequestMapping(value = "test", method = RequestMethod.GET)
	public String testMap() {
		return "@========test=============\n";
	}
	
	@ResponseBody
	@RequestMapping(value = "exception", method = RequestMethod.GET)
	public String testException() {
		try {
			service.testException();
		} catch (LongioException e) {
			e.printStackTrace();
			return e.toString();
		}
		return "@========test=============\n";
	}
}
