package com.ecs160.persistence;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public int getAmountOfKeys() {
        isRedisAvailable();
        return getAllKeys().size();
    }

    public void clearDB() {
        isRedisAvailable();
        if (clear()) {
            System.out.println("Redis database flushed.");
        } else {
            System.out.println("No keys found in the Redis database. Skipping flush.");
        }
    }

    private Set<String> getAllKeys() {
        isRedisAvailable();
        Set<String> keys = new HashSet<>();
        String cursor = "0";
        do {
            ScanResult<String> scanResult = this.jedis.scan(cursor);
            cursor = scanResult.getCursor();
            keys.addAll(scanResult.getResult());
        } while (!cursor.equals("0"));
        return keys;
    }

    private boolean clear() {
        if (this.jedis.dbSize() > 0) {
            jedis.flushDB();
            return true;
        }
        return false;
    }

    private void isRedisAvailable() {
        try {
            this.jedis.ping();  // This actually tests the connection
        } catch (JedisConnectionException e) {
            throw new IllegalStateException("Redis connection lost.", e);
        }
    }
}
