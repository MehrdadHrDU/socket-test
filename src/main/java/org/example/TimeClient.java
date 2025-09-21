package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TimeClient {
    private final String host;
    private final int port;

    public TimeClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            p.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    System.out.println("Client received: " + msg);
                                }
                            });
                        }
                    });

            Channel ch = b.connect(host, port).sync().channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            System.out.println("Enter GET_DATE for or 'quit' to exit:");
            while ((line = in.readLine()) != null) {
                if ("quit".equalsIgnoreCase(line.trim())) {
                    ch.close().sync();
                    break;
                }
                ch.writeAndFlush(line + "\n");
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new TimeClient("localhost", 8080).start();
    }
}
