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

            ParsedMessage parsedMessage = parseAndLog(msg);
            switch (parsedMessage.getTag()) {
                case "GET_DATE":
                    ParsedMessage parsedDateMessage = new ParsedMessage();
                    parsedDateMessage.setDate(new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    parsedDateMessage.setTag("SET_DATE");
                    Map<String,String> map = new HashMap<>();
                    map.put("date",new  SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                    parsedDateMessage.setParams(map);
                    parsedDateMessage.getParams().size();
                    ctx.writeAndFlush(buildMessage(parsedDateMessage));
                    break;
                default:
                    ctx.writeAndFlush(buildErrorMessage(parsedMessage.getTag(),
                            "Parameter count mismatch! Expected=" + parsedMessage.getParams().size() +
                                    ", Actual=" + parsedMessage.getParams().size()));

            }

        } catch (Exception e) {
            ctx.writeAndFlush(buildErrorMessage("ERROR", "Error parsing message"));
        }
    }

    private ParsedMessage parseAndLog(String msg) {
        if (!msg.endsWith("#")) {
            throw new RuntimeException();
        }

        msg = msg.substring(0, msg.length() - 1);
        String[] parts = msg.split(";");
        if (parts.length < 3) {
            throw new RuntimeException();
        }

        ParsedMessage parsed = new ParsedMessage();
        parsed.setTag(parts[0]);
        parsed.setDate(parts[1]);

        Map<String, String> params = new HashMap<>();
        for (int i = 3; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        parsed.setParams(params);

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (parsed.getTag().contains("GET_DATE")) {
            parsed.getParams().put("c_dt", formattedDate);
        }

        return parsed;
    }


    private String buildMessage(ParsedMessage parsed) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder(parsed.getTag())
                .append(";")
                .append(parsed.getDate())
                .append(";")
                .append(parsed.getParams().size())
                .append(";");
        parsed.getParams().forEach((k, v) -> sb.append(k).append("=").append(v).append(";"));
        sb.append("#");
        return sb.toString();
    }

    private String buildErrorMessage(String tag, String error) {
        ParsedMessage errorMsg = new ParsedMessage();
        errorMsg.setTag(tag);
        errorMsg.getParams().size();
        Map<String, String> params = new HashMap<>();
        params.put("error", error);
        errorMsg.setParams(params);
        return buildMessage(errorMsg);
    }
}
