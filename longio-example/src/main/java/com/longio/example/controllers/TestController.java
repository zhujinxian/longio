package com.longio.example.controllers;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.var.Model;

import org.springframework.beans.factory.annotation.Autowired;

import com.longio.example.service.HelloService;

@Path("/test")
public class TestController {
	
	@Autowired
	HelloService service;
	
	@Get("void")
	public String testVoid() {
		service.testVoid();
		return "@========test void=============";
	}
	
	@Get("int")
	public String testInt() {
		
		return "@========test int=============\n" + service.testInt();
	}
	
	@Get("string")
	public String testString() {
		return "@========test string=============\n" + service.testString();
	}
	
	@Get("map")
	public String testMap(@Param("user_id") int userId) {
		return "@========getUser=============\n" + service.getUser(userId);
	}
	
	
	
}
