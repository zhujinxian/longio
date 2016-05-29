/**

The MIT License (MIT)

Copyright (c) <2015> <author or authors>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package com.zhucode.longio.example.service;

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tMap;

import java.io.IOException;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;

import com.zhucode.longio.example.message.UserMsg;
import com.zhucode.longio.message.format.MessagePackData;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
public class TestClient {
	
	public static final String HOST = System.getProperty("host", "127.0.0.1");
	public static final int PORT = Integer.parseInt(System.getProperty("port", "9001"));
	public static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

	public static void main(String[] args) throws Exception {
		  // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                 	ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2));
    				ch.pipeline().addLast("encoder", new LengthFieldPrepender(2, false));
                    p.addLast(new EchoClientHandler());
                 }
             });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
	}

}


/**
 * @author zhu jinxian
 * @date  2015年10月12日
 * 
 */
class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private  ByteBuf firstMessage;

   
    public EchoClientHandler() {
//    	try {
//			firstMessage = Unpooled.wrappedBuffer("{data : {user_id: 1000}, cmd : 100}".getBytes("utf-8"));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
    	//------------------------------
//    	User.Data d = User.Data.newBuilder().setUserId(1000).build();
//    	Proto.Message pd = Proto.Message.newBuilder()
//    			.setCmd(101).setSerial(123).setBody(d.toByteString()).build();
//    	
//    	com.zhucode.longio.example.message.User.Data d0;
//    	firstMessage = Unpooled.wrappedBuffer(pd.toByteArray());
    	
    	UserMsg um = new UserMsg();
    	um.user_id = 1000;
    	
    	MessagePackData mpd = new MessagePackData();
    	mpd.cmd = 100;
    	mpd.serial = 13444;
    	
    	try {
    		MessagePack mp = new MessagePack();
        	mpd.data = mp.write(um);
        	
        	mp = new MessagePack();
			firstMessage = Unpooled.wrappedBuffer(mp.write(mpd));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	ByteBuf buf = (ByteBuf)msg;
        //System.out.println(buf.toString(CharsetUtil.UTF_8));
    	
    	
    	byte[] bytes = new byte[buf.readableBytes()];
    	buf.readBytes(bytes);
    	
    	
//    	try {;
//    	Proto.Message pd = Proto.Message.parseFrom(bytes);
//			System.out.println(Res.Data.parseFrom(pd.getBody()).getStatus());
//		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
//		}
    	
    	
    	
    	Template<Map<String, String>> mapTmpl = tMap(TString, TString);
    	
    	try {
        	MessagePack mp = new MessagePack();
        	MessagePackData mpd = mp.read(bytes, MessagePackData.class);
        	
        	mp = new MessagePack();
    		
			Map<String, String> map = mp.read(mpd.data, mapTmpl);
			System.out.println(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
       ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
