package org.example;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisStreamService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String streamKey = "socket:events";

    public RedisStreamService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void pushMessage(String tag, Map<String, String> params) {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        Map<String, Object> data = new HashMap<>(params);
        data.put("tag", tag);
        data.put("timestamp", System.currentTimeMillis());
        streamOps.add(streamKey, data);
    }
}
