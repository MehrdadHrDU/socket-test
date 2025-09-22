package org.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        msg = msg.trim();

        try {
            if (!msg.endsWith("#")) {
                String errorMsg = buildErrorMessage("ERROR", "Message must end with #");
                ctx.writeAndFlush(errorMsg + "\n");
                System.out.println(errorMsg);
                return;
            }

            msg = msg.substring(0, msg.length() - 1);

            String[] parts = msg.split(";");
            if (parts.length < 3) {
                String errorMsg = buildErrorMessage("ERROR", "Invalid message format");
                ctx.writeAndFlush(errorMsg + "\n");
                System.out.println(errorMsg);
                return;
            }

            String tag = parts[0];
            int paramCount;

            try {
                paramCount = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                String errorMsg = buildErrorMessage(tag, "paramCount is not a number");
                ctx.writeAndFlush(errorMsg + "\n");
                System.out.println(errorMsg);
                return;
            }

            Map<String, String> params = new HashMap<>();
            for (int i = 3; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }

            String consoleMsg = buildMessage(tag, params);
            System.out.println(consoleMsg);

            ctx.writeAndFlush(consoleMsg + "\n");

        } catch (Exception e) {
            String errorMsg = buildErrorMessage("ERROR", "Error parsing message");
            ctx.writeAndFlush(errorMsg + "\n");
            System.out.println(errorMsg);
        }
    }

    private String buildMessage(String tag, Map<String, String> params) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(";").append(formattedDate).append(";").append(params.size()).append(";");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }

        sb.append("#");
        return sb.toString();
    }

    private String buildErrorMessage(String tag, String errorMessage) {
        Map<String, String> errorParam = new HashMap<>();
        errorParam.put("error", errorMessage);
        return buildMessage(tag, errorParam);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String errorMsg = buildErrorMessage("ERROR", "Exception caught: " + cause.getMessage());
        System.out.println(errorMsg);
        ctx.writeAndFlush(errorMsg + "\n");
        ctx.close();
    }
}
