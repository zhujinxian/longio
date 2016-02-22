/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.conf;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author zhu jinxian
 * @date  2016年2月18日
 * 
 */
public class RedisAppLookup implements AppLookup {
	
	private JedisPool pool;
	
	public RedisAppLookup(String host, int port, String pwd) {
		getPool(host, port, pwd);
	}
	
	private JedisPool getPool(String ip, int port, String pwd) {
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			config.setMaxTotal(500);  
			// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			config.setMaxIdle(5);
			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
			config.setMaxWaitMillis(1000 * 100);
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			config.setTestOnBorrow(true);
			pool = new JedisPool(config, ip, port, 1000, pwd);
		}
		return pool;
	}
	

	@Override
	public String[] parseHosts(String app) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			String val = jedis.hget("longio.app.hosts", app);
			if (val == null) {
				return null;
			}
			return val.split("\\s+");
		} finally {
		  if (jedis != null) {
		    jedis.close();
		  }
		}
	}

	@Override
	public String parseAppName(int appId) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.hget("longio.app.id_name", "longio.app."+appId + "");
		} finally {
		  if (jedis != null) {
		    jedis.close();
		  }
		}
	}
	
	public static void main(String... args) {
		RedisAppLookup look = new RedisAppLookup("127.0.0.1", 6379, "123456");
		System.out.println(look.parseAppName(2000));
	}
	

}
