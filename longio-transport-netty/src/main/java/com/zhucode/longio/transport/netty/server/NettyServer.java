/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.transport.netty.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhucode.longio.Protocol;
import com.zhucode.longio.Protocol.ProtocolException;
import com.zhucode.longio.annotation.Rpc;
import com.zhucode.longio.annotation.RpcController;
import com.zhucode.longio.boot.ServerHandler;
import com.zhucode.longio.core.conf.AppLookup;
import com.zhucode.longio.core.conf.CmdLookup;
import com.zhucode.longio.core.server.HandlerInterceptor;
import com.zhucode.longio.core.server.MethodHandler;
import com.zhucode.longio.core.server.RequestWrapper;
import com.zhucode.longio.core.server.ResponseWrapper;
import com.zhucode.longio.core.transport.TransportType;
import com.zhucode.longio.scan.LongioScanner;
import com.zhucode.longio.transport.netty.handler.AbstractNettyHandler;
import com.zhucode.longio.transport.netty.server.handler.HttpHandler;
import com.zhucode.longio.transport.netty.server.handler.RawSocketHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

/**
 * @author zhu jinxian
 * @date 2016年8月14日 下午6:58:07
 * 
 */
public class NettyServer extends ServerHandler {
	
	private Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private AtomicLong channeId = new AtomicLong(1000000);
	
	protected AttributeKey<Long> channelKey = AttributeKey.valueOf("channelId");

	private Executor executor = Executors.newCachedThreadPool();

	private NioEventLoopGroup bossGroup = new NioEventLoopGroup();

	private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

	private ConcurrentHashMap<Long, ChannelHandlerContext> ctxs = new ConcurrentHashMap<Long, ChannelHandlerContext>();

	private AppLookup appLookup;
	private CmdLookup cmdLookup;
	private LongioScanner scanner;
		
	public NettyServer(AppLookup appLookup, CmdLookup cmdLookup, LongioScanner scanner) {
		this.appLookup = appLookup;
		this.cmdLookup = cmdLookup;
		this.scanner = scanner;
	}
	
	@Override
	public Future<Void> write(ResponseWrapper response) {
		byte[] bytes = response.getProtocol().encodeResponse(response);
		ChannelHandlerContext ctx = ctxs.get(response.getChannelId());

		AttributeKey<AbstractNettyHandler> handlerKey = AttributeKey.valueOf("AbstractNettyHandler");
		AbstractNettyHandler handler = ctx.attr(handlerKey).get();

		return handler.write(ctx, bytes);

	}

	@Override
	public void start(String path, String host, int port, TransportType transportType, Protocol protocol) {
		
		List<Object> controllers = this.scanner.scanControllers(path);
		Map<Integer, MethodHandler> routeMap = createRouteMap(controllers);
		List<HandlerInterceptor> interceptors = this.scanner.scanInterceptors(path);
		this.registerInterceptor(interceptors);
		this.registerMethodHandlers(routeMap);
		
		executor.execute(new Runnable() {

			@Override
			public void run() {
				switch (transportType) {
				case HTTP:
					runOneHttpServer(path, host, port, protocol);
					break;
				case SOCKET:
					runOneRawSocketServer(path, host, port, protocol);
				default:
					break;
				}
			}

		});

	}

	private void runOneRawSocketServer(String path, String host, int port, Protocol protocol) {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);

		b.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new HttpServerCodec());
				ch.pipeline().addLast(new HttpObjectAggregator(65536));
				ch.pipeline().addLast(new IdleStateHandler(6000, 3000, 0));
				ch.pipeline().addLast(
						new RawSocketHandler(NettyServer.this, protocol));
			}

		});
		b.option(ChannelOption.SO_BACKLOG, 4096);

		b.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture f;
		try {
			if (host != null) {
				f = b.bind(host, port);
			} else {
				f = b.bind(port);
			}
			this.appLookup.registerAapp(path, host, port, protocol);
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	private void runOneHttpServer(String path, String host, int port, Protocol protocol) {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);

		b.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new HttpServerCodec());
				ch.pipeline().addLast(new HttpObjectAggregator(65536));
				ch.pipeline().addLast(new IdleStateHandler(6000, 3000, 0));
				ch.pipeline().addLast(
						new HttpHandler(NettyServer.this, protocol));
			}

		});
		b.option(ChannelOption.SO_BACKLOG, 4096);

		b.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture f;
		try {
			if (host != null) {
				f = b.bind(host, port);
			} else {
				f = b.bind(port);
			}
			this.appLookup.registerAapp(path, host, port, protocol);
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public long registHandlerContext(ChannelHandlerContext ctx) {
		long cid = channeId.addAndGet(1);
		ctx.channel().attr(channelKey).set(cid);
		ctxs.put(cid, ctx);
		return cid;
	}
	
	public void unregistHandlerContext(ChannelHandlerContext ctx) {
		long cid = ctx.channel().attr(channelKey).get();
		ctxs.remove(cid);
	}

	

	public void service(long channelId, Protocol protocol, ByteBuf buf) throws ProtocolException {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		
		RequestWrapper request = new RequestWrapper();
		request.setProtocol(protocol);
		request.setChannelId(channelId);
		request.setServerHandler(this);
		
		ResponseWrapper response = new ResponseWrapper();
		response.setProtocol(protocol);
		response.setChannelId(channelId);
		response.setServerHandler(this);

		protocol.decodeRequest(request, bytes);
		
		response.setSerial(request.getSerial());
		response.setCmd(request.getCmd());
		response.setVersion(request.getVersion());
		
		this.service(request, response);
	}
	
	
	private Map<Integer, MethodHandler> createRouteMap(List<Object> controllerObjs) {
		Map<Integer, MethodHandler> map = new HashMap<Integer, MethodHandler>();
		for (Object obj : controllerObjs) {
			Class<?> cls = obj.getClass();
			RpcController ls = cls.getAnnotation(RpcController.class);
			if (ls == null) {
				continue;
			}
			for (Method m : cls.getMethods()) {
				Rpc lio = m.getAnnotation(Rpc.class);
				if (lio == null) {
					continue;
				}
				String cmdName = ls.path() + "." + lio.cmd();
				cmdName = cmdName.replaceAll("\\.\\.", ".");
				int cmd = cmdLookup.parseCmd(cmdName);
				boolean asy = lio.asy();
				boolean reply = lio.reply();
				MethodHandler mih = new MethodHandler(cmd, cmdName, obj, m, asy, reply);
				map.put(cmd, mih);
			}
		}
		return map;
	}


}
