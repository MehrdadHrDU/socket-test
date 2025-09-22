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
                sendAndLog(ctx, buildErrorMessage("ERROR", "Message must end with #"));
                return;
            }

            msg = msg.substring(0, msg.length() - 1);
            String[] parts = msg.split(";");
            if (parts.length < 3) {
                sendAndLog(ctx, buildErrorMessage("ERROR", "Invalid message format"));
                return;
            }

            String tag = parts[0];
            int paramCount;
            try {
                paramCount = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                sendAndLog(ctx, buildErrorMessage(tag, "paramCount is not a number"));
                return;
            }

            Map<String, String> params = new HashMap<>();
            for (int i = 3; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length == 2) params.put(kv[0], kv[1]);
            }

            if (paramCount != params.size()) {
                sendAndLog(ctx, buildErrorMessage(tag,
                        "Parameter count mismatch! Expected=" + paramCount + ", Actual=" + params.size()));
                return;
            }

            sendAndLog(ctx, buildMessage(tag, params));

        } catch (Exception e) {
            sendAndLog(ctx, buildErrorMessage("ERROR", "Error parsing message"));
        }
    }

    private void sendAndLog(ChannelHandlerContext ctx, String message) {
        System.out.println(message);
        ctx.writeAndFlush(message + "\n");
    }

    private String buildMessage(String tag, Map<String, String> params) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder(tag)
                .append(";").append(formattedDate)
                .append(";").append(params.size()).append(";");
        params.forEach((k, v) -> sb.append(k).append("=").append(v).append(";"));
        sb.append("#");
        return sb.toString();
    }

    private String buildErrorMessage(String tag, String error) {
        Map<String, String> params = new HashMap<>();
        params.put("error", error);
        return buildMessage(tag, params);
    }
}
