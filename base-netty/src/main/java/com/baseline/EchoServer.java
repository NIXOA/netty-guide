package com.baseline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Author Godzilla
 * @Date 2019/9/9 23:26
 * @Version 1.0
 */
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port ;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length!=1){
            System.out.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
        }

        int port=Integer.parseInt(args[0]);

        new EchoServer(port).start();


    }

    public void start() throws InterruptedException {
        final EchoServerHandle serverHandle=new EchoServerHandle();
        EventLoopGroup group=new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(group)
                    //指定所使用NIO传输的Channel
                    .channel(NioServerSocketChannel.class)
                    //指定接口的套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //添加自定义的handle
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(serverHandle);
                        }
                    });
            //异步绑定服务器，调用sync()方法阻塞直到绑定完成
            ChannelFuture future=bootstrap.bind().sync();
            //获取Channel的CloseFuture,并且阻塞当前线程直它完成
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            //关闭EventLoopGroup,释放所有的资源
            group.shutdownGracefully().sync();
        }
    }

}
