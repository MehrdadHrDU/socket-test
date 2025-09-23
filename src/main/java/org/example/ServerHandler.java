package org.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private final RedisStreamService redisStreamService;

    public ServerHandler(RedisStreamService redisStreamService) {
        this.redisStreamService = redisStreamService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        msg = msg.trim();
        try {
            ParsedMessage parsedMessage = parseAndLog(msg);

            redisStreamService.pushMessage(parsedMessage.getTag(), parsedMessage.getParams());

            switch (parsedMessage.getTag()) {
                case "GET_DATE":
                    ParsedMessage response = new ParsedMessage();
                    response.setTag("SET_DATE");
                    response.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    Map<String,String> map = new HashMap<>();
                    map.put("date", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                    response.setParams(map);
                    ctx.writeAndFlush(buildMessage(response));
                    break;

                default:
                    ctx.writeAndFlush(buildErrorMessage(parsedMessage.getTag(),
                            "Unknown tag: " + parsedMessage.getTag()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.writeAndFlush(buildErrorMessage("ERROR", "Error parsing message"));
        }
    }

    private ParsedMessage parseAndLog(String msg) {
        if (!msg.endsWith("#")) throw new RuntimeException("Message must end with #");

        msg = msg.substring(0, msg.length() - 1);
        String[] parts = msg.split(";");
        if (parts.length < 3) throw new RuntimeException("Invalid message format");

        ParsedMessage parsed = new ParsedMessage();
        parsed.setTag(parts[0]);
        parsed.setDate(parts[1]);

        Map<String, String> params = new HashMap<>();
        for (int i = 3; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        parsed.setParams(params);

        return parsed;
    }

    private String buildMessage(ParsedMessage parsed) {
        StringBuilder sb = new StringBuilder(parsed.getTag())
                .append(";")
                .append(parsed.getDate())
                .append(";")
                .append(parsed.getParams().size())
                .append(";");
        parsed.getParams().forEach((k,v) -> sb.append(k).append("=").append(v).append(";"));
        sb.append("#");
        return sb.toString();
    }

    private String buildErrorMessage(String tag, String error) {
        ParsedMessage errorMsg = new ParsedMessage();
        errorMsg.setTag(tag);
        Map<String,String> map = new HashMap<>();
        map.put("error", error);
        errorMsg.setParams(map);
        return buildMessage(errorMsg);
    }
}

