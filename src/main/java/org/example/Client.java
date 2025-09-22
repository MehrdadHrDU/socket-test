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

public class Client {

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            p.addLast(new ClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect(host, port).sync().channel();

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter messages in format <tag>;<formattedDate>;<paramCount>;<key=value>;...#");
            System.out.println("Type 'quit' to exit or 'help' for instructions.");

            String line;
            while ((line = consoleReader.readLine()) != null) {
                line = line.trim();
                if ("quit".equalsIgnoreCase(line)) {
                    channel.close().sync();
                    break;
                } else if ("help".equalsIgnoreCase(line)) {
                    printHelp();
                    continue;
                }

                channel.writeAndFlush(line + "\n");
            }

        } finally {
            group.shutdownGracefully();
        }
    }

    private void printHelp() {
        System.out.println("Message format:");
        System.out.println("<tag>;<formattedDate>;<paramCount>;<key1>=<value1>;<key2>=<value2>;...#");
        System.out.println("Examples:");
        System.out.println("GET_DATE;2025-09-22 12:00:00;2;name=Mehrdad;city=Baku;#");
        System.out.println("Type 'quit' to exit.");
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            System.out.println("[Server Response] " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("Exception: " + cause.getMessage());
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new Client("localhost", 8080).start();
    }
}
