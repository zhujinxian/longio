/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/package com.zhucode.longio.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.zhucode.longio.client.ClientDispatcher;
import com.zhucode.longio.message.Dispatcher;
import com.zhucode.longio.message.MessageBlock;
import com.zhucode.longio.protocol.JSONArrayProtocolParser;
import com.zhucode.longio.protocol.JSONObjectProtocolParser;
import com.zhucode.longio.protocol.MessagePackProtocolParser;
import com.zhucode.longio.protocol.ProtoBufProtocolParser;
import com.zhucode.longio.protocol.ProtocolParser;
import com.zhucode.longio.transport.Beginpoint;
import com.zhucode.longio.transport.Client;
import com.zhucode.longio.transport.Connector;
import com.zhucode.longio.transport.Endpoint;
import com.zhucode.longio.transport.ProtocolType;
import com.zhucode.longio.transport.TransportType;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class NettyConnector implements Connector {

	private ConcurrentHashMap<Long, ChannelHandlerContext> ctxs = new ConcurrentHashMap<Long, ChannelHandlerContext>();
	
	private AtomicLong sessionId = new AtomicLong(1000000);
	
	protected static AttributeKey<Long> sessionKey = AttributeKey.valueOf("sid");

	private NioEventLoopGroup bossGroup;

	private NioEventLoopGroup workerGroup;
	
	private Executor exe = Executors.newCachedThreadPool();
	
	private List<Endpoint> endpoints = new ArrayList<Endpoint>();

	private ClientDispatcher clientDispatcher = new ClientDispatcher();
	

	public NettyConnector() {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
	}

	public long registHandlerContext(ChannelHandlerContext ctx) {
		long sid = sessionId.addAndGet(1);
		ctx.channel().attr(sessionKey).set(sid);
		ctxs.put(sid, ctx);
		return sid;
	}
	
	public void unregistHandlerContext(ChannelHandlerContext ctx) {
		long sid = ctx.channel().attr(sessionKey).get();
		ctxs.remove(sid);
	}
	
	@Override
	public int getConnectId() {
		return 0;
	}

	@Override
	public void sendMessage(MessageBlock<?> message) {
		this.send(message);
	}
	
	protected ChannelFuture send(MessageBlock<?> message) {
		long sid = message.getSessionId();
		ChannelHandlerContext ctx = ctxs.get(sid);
		AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
		System.out.println("h : " + ctx.attr(handlerKey) == null + " sid = " + sid);
		AbstractNettyHandler handler = ctx.attr(handlerKey).get();
		ChannelFuture future = handler.sendMessage(ctx, message);
		return future;
	}
	
	
	@Override
	public void start(int port, Dispatcher dispatcher, TransportType tt,
			ProtocolType pt, String pkg) throws Exception {
		Endpoint ep = new Endpoint(pkg, port, tt, pt);
		ep.setConnector(this);
		ep.setDispatcher(dispatcher);
		this.endpoints.add(ep);
		exe.execute(new Runnable() {
			
			@Override
			public void run() {
				switch (tt) {
				case HTTP:
					runOneHttpServer(port, dispatcher, pt);
					break;
				case SOCKET:
					runOneRawSocketServer(port, dispatcher, pt);
				default:
					break;
				}
			}
		});
	}
	
	@Override
	public void start(int port, Dispatcher dispatcher, TransportType tt, ProtocolType pt)
			throws Exception {
		start(port, dispatcher, tt, pt, "*");
	}
	
	
	private ProtocolParser<?> getProtocolParser(ProtocolType pt) {
		switch(pt) {
		case JSON:
			return new JSONObjectProtocolParser();
		case JSONARRAY:
			return new JSONArrayProtocolParser();
		case PROTOBUF:
			return new ProtoBufProtocolParser();
		case MESSAGE_PACK:
			return new MessagePackProtocolParser();
		default:
			break;
		}
		return null;
	}


	private void runOneHttpServer(int port, Dispatcher dispatcher, ProtocolType pt) {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);
		
		b.childHandler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new HttpServerCodec());
				ch.pipeline().addLast(new HttpObjectAggregator(65536));
				ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));
				ch.pipeline().addLast(new HttpHandler(
						NettyConnector.this, dispatcher, getProtocolParser(pt)));
			}
			
		});
		b.option(ChannelOption.SO_BACKLOG, 128);
		
		b.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		ChannelFuture f;
		try {
			f = b.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runOneRawSocketServer(int port, Dispatcher dispatcher, ProtocolType pt) {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);
		
		b.childHandler(new ChannelInitializer<SocketChannel>(){

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2));
				ch.pipeline().addLast("encoder", new LengthFieldPrepender(2, false));
				ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));
				ch.pipeline().addLast(new RawSocketHandler(NettyConnector.this, 
						dispatcher, getProtocolParser(pt)));
			}
			
		});
		b.option(ChannelOption.SO_BACKLOG, 128);
		
		b.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		ChannelFuture f;
		try {
			f = b.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<Endpoint> getEndpoints(String pkg) {
		if (pkg.equals("*")) {
			return this.endpoints;
		}
		List<Endpoint> points = new ArrayList<Endpoint>();
		for (Endpoint ep : this.endpoints) {
			if (ep.getPkg().startsWith(pkg) || ep.getPkg().equals("*")) {
				points.add(ep);
			}
		}
		return points;
	}

	@Override
	public Set<Dispatcher> getDispatcheres(String pkg) {
		
		Set<Dispatcher> dispatcher = new HashSet<Dispatcher>();
		
		for (Endpoint ep : this.endpoints) {
			if (ep.getPkg().startsWith(pkg) || ep.getPkg().equals("*") || pkg.equals("*")) {
				dispatcher.add(ep.getDispatcher());
			}
		}
		return dispatcher;
	}

	@Override
	public Client createClient(String host, int port, TransportType tt,
			ProtocolType pt) throws Exception {
		switch (tt) {
		case HTTP:
			return runOneHttpClient(host, port, pt);
		case SOCKET:
			return runOneRawSocketClient(host, port, pt);
		default:
			break;
			
		}
		return null;
	}
	
	private Client runOneHttpClient(String host, int port, ProtocolType pt) {	
		
		return new NettyClient(this, host, port, new AbstarctClientChannelInitializer() {
			

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new HttpClientCodec());
				p.addLast(new HttpObjectAggregator(65536));
				URI uri = new URI("http://" + host + ":" + port);
				p.addLast(new HttpClientHandler(client, NettyConnector.this, getProtocolParser(pt), uri, null));
			}
        });
	}
	
	private Client runOneRawSocketClient(String host, int port, ProtocolType pt) {
		
		return new NettyClient(this, host, port, new AbstarctClientChannelInitializer() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast("decoder", new LengthFieldBasedFrameDecoder(65536, 0,2, 0, 2));
				p.addLast("encoder", new LengthFieldPrepender(2, false));
				p.addLast("handler", new RawSocketClientHandler(client, NettyConnector.this, getProtocolParser(pt)));
			}
		});
	}

	@Override
	public ClientDispatcher getClientDispatcher() {
		return this.clientDispatcher ;
	}


	@Override
	public void sendMessage(Beginpoint bp, MessageBlock<?> message) {
		long sid = message.getSessionId();
		ChannelHandlerContext ctx = ctxs.get(sid);
		AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
		System.out.println("h : " + ctx.attr(handlerKey) == null + " sid = " + sid);
		AbstractNettyHandler handler = ctx.attr(handlerKey).get();
		ChannelFuture f = handler.sendMessage(ctx, message);
	}

	public NioEventLoopGroup getBossGroup() {
		return bossGroup;
	}

	public NioEventLoopGroup getWorkerGroup() {
		return workerGroup;
	}
	
	
	

}
