package br.com.twitter.mentions.listener.gpt.config;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisConnectionSingleton {
    private static RedisConnectionSingleton instance;
    private final RedisClient redisClient;
    private final RedisConnection<String, String> connection;

    private RedisConnectionSingleton() {
        redisClient = new RedisClient(RedisURI.create(System.getProperty("REDIS_CONNECTION")));
        connection = redisClient.connect();
        log.info("Connected to Redis");
    }

    public static RedisConnectionSingleton getInstance() {
        if (instance == null) {
            instance = new RedisConnectionSingleton();
        }
        return instance;
    }

    public RedisConnection<String, String> getConnection() {
        return connection;
    }

    public void closeConnection() {
        connection.close();
        redisClient.shutdown();
    }
}
