package br.com.twitter.mentions.listener.gpt.service;

import br.com.twitter.mentions.listener.gpt.config.RedisConnectionSingleton;
import com.lambdaworks.redis.RedisConnection;

public class RedisService {

    RedisConnection<String, String> redisConnection = RedisConnectionSingleton.getInstance().getConnection();

    public void set(final String key) {
        redisConnection.set(key, "1");
        System.out.printf("Key has been set key=%s", key);
        System.out.println();
    }

    public String get(final String key) {
        return redisConnection.get(key);
    }
}
