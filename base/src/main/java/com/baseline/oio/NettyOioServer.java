package com.baseline.oio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * 使用netty的阻塞网络处理
 *
 * @Author Godzilla
 * @Date 2019/9/17 23:30
 * @Version 1.0
 */
public class NettyOioServer {


    /**
     * 阻塞的网络处理
     *
     * @param port
     * @throws InterruptedException
     */
    public void server(int port) throws InterruptedException {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled
                .copiedBuffer("Hi !\r\n", Charset.forName("UTF-8")));
        //使用OioEventLoop以允许阻塞模式
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(OioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    //指定ChannelInitializer,每个已接受的连接都调用它
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ChannelHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    //添加ChannelFutureListener，消息一写完就关闭
                                    ctx.writeAndFlush(buf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }

                    });

            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }


    }
}
