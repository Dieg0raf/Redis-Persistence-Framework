package com.ecs160.persistence;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

public class RedisDB {
    private final Jedis jedis;

    public RedisDB() {
        this.jedis = new Jedis("localhost", 6379);;
    }

    public void setObject(String key, Map<String, String> objMap) {
        isRedisAvailable();
        this.jedis.hset(key, objMap);
    }

    public Map<String,String> loadObject(String key) {
        isRedisAvailable();
        return this.jedis.hgetAll(key);
    }

    private void isRedisAvailable() {
        try {
            this.jedis.ping();  // This actually tests the connection
        } catch (JedisConnectionException e) {
            throw new IllegalStateException("Redis connection lost.", e);
        }
    }
}
