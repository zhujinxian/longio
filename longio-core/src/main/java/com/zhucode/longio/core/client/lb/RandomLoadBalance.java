/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.core.client.lb;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.zhucode.longio.App;
import com.zhucode.longio.LoadBalance;
import com.zhucode.longio.Request;

/**
 * @author zhu jinxian
 * @date  2017年2月1日 下午1:43:35 
 * 
 */
public class RandomLoadBalance implements LoadBalance {

	@Override
	public App select(Request request, List<App> apps) {
		if (apps.size() == 0) {
			return null;
		}
        int idx = (int) (ThreadLocalRandom.current().nextDouble() * apps.size());
		return apps.get(idx);
	}

}
