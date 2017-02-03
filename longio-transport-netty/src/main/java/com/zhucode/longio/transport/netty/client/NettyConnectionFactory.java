/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.client;

import java.net.URI;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.transport.netty.client.handler.HttpClientHandler;
import com.zhucode.longio.transport.netty.client.handler.RawSocketClientHandler;
import com.zhucode.longio.transport.netty.server.NettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * @author zhu jinxian
 * @date  2016年9月15日 下午5:23:03 
 * 
 */
public class NettyConnectionFactory {
	
	private NioEventLoopGroup eventLoop;
	
	public NettyConnectionFactory(NioEventLoopGroup eventLoop) {
		this.eventLoop = eventLoop;
	}
	
	public NettyConnection runOneHttpClient(NettyClient client, String host, int port, Protocol protocol) {	
		
		return new NettyConnection(host, port, protocol, eventLoop, new ChannelInitializer<SocketChannel>() {
			
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new HttpClientCodec());
				p.addLast(new HttpObjectAggregator(65536));
				URI uri = new URI("http://" + host + ":" +port);
				p.addLast(new HttpClientHandler(client, protocol, uri));
			}
        });
	}
	
	public NettyConnection runOneRawSocketClient(NettyClient client, String host, int port, Protocol protocol) {
		
		return new NettyConnection(host, port, protocol, eventLoop, new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast("decoder", new LengthFieldBasedFrameDecoder(65536, 0,2, 0, 2));
				p.addLast("encoder", new LengthFieldPrepender(2, false));
				p.addLast("handler", new RawSocketClientHandler(client, protocol));
			}
		});
	}


}
