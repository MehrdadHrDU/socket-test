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

            ParsedMessage parsed = new ParsedMessage();
            parsed.setTag(parts[0]);

            int paramCount;
            try {
                paramCount = Integer.parseInt(parts[2]);
                parsed.setParamCount(paramCount);
            } catch (NumberFormatException e) {
                sendAndLog(ctx, buildErrorMessage(parsed.getTag(), "paramCount is not a number"));
                return;
            }

            Map<String, String> params = new HashMap<>();
            for (int i = 3; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
            parsed.setParams(params);

            if (parsed.getParamCount() != parsed.getParams().size()) {
                sendAndLog(ctx, buildErrorMessage(parsed.getTag(),
                        "Parameter count mismatch! Expected=" + parsed.getParamCount() + ", Actual=" + parsed.getParams().size()));
                return;
            }

            sendAndLog(ctx, buildMessage(parsed));

        } catch (Exception e) {
            sendAndLog(ctx, buildErrorMessage("ERROR", "Error parsing message"));
        }
    }

    private void sendAndLog(ChannelHandlerContext ctx, String message) {
        System.out.println(message);
        ctx.writeAndFlush(message + "\n");
    }

    private String buildMessage(ParsedMessage parsed) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder(parsed.getTag())
                .append(";")
                .append(formattedDate)
                .append(";")
                .append(parsed.getParamCount())
                .append(";");
        parsed.getParams().forEach((k, v) -> sb.append(k).append("=").append(v).append(";"));
        sb.append("#");
        return sb.toString();
    }

    private String buildErrorMessage(String tag, String error) {
        ParsedMessage errorMsg = new ParsedMessage();
        errorMsg.setTag(tag);
        errorMsg.setParamCount(1);
        Map<String, String> params = new HashMap<>();
        params.put("error", error);
        errorMsg.setParams(params);
        return buildMessage(errorMsg);
    }
}
