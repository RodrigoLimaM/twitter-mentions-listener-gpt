package br.com.twitter.mentions.listener.gpt.service;

import br.com.twitter.mentions.listener.gpt.config.RedisConnectionSingleton;
import com.lambdaworks.redis.RedisConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisService {

    RedisConnection<String, String> redisConnection = RedisConnectionSingleton.getInstance().getConnection();

    public void set(final String key) {
        redisConnection.set(key, "1");
        log.info("Key has been set key={}", key);
    }

    public String get(final String key) {
        return redisConnection.get(key);
    }
}
