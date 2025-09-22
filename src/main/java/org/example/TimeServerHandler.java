package org.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

public class TimeServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        msg = msg.trim();

        try {
            if (!msg.startsWith("GET_DATE")) {
                ctx.writeAndFlush("Error: Unsupported tag! Only GET_DATE allowed\n");
                return;
            }

            if (!msg.endsWith("#")) {
                ctx.writeAndFlush("Error: Message must end with #\n");
                return;
            }

            msg = msg.substring(0, msg.length() - 1);

            String[] parts = msg.split(";");
            if (parts.length < 4) {
                ctx.writeAndFlush("Error: Invalid message format!\n");
                return;
            }

            ParsedMessage parsed = new ParsedMessage();
            parsed.setTag(parts[0]);
            parsed.setInitialDate(parts[1]);
            parsed.setParamCount(Integer.parseInt(parts[2]));

            Map<String, String> params = new HashMap<>();
            for (int i = 3; i < parts.length; i++) {
                String paramPart = parts[i];
                if (paramPart.isEmpty()) continue;

                String[] kv = paramPart.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
            parsed.setParams(params);

            if (params.size() != parsed.getParamCount()) {
                ctx.writeAndFlush("Error: Parameter count mismatch!\n");
                return;
            }

            System.out.println("Parsed message:");
            System.out.println("Tag: " + parsed.getTag());
            System.out.println("Initial Date: " + parsed.getInitialDate());
            System.out.println("Param Count: " + parsed.getParamCount());
            parsed.getParams().forEach((k, v) -> System.out.println(k + " = " + v));

            if (params.containsKey("c_dt")) {
                ctx.writeAndFlush(params.get("c_dt") + "\n");
            } else {
                ctx.writeAndFlush("Error: c_dt not found!\n");
            }

        } catch (Exception e) {
            ctx.writeAndFlush("Error parsing message!\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
