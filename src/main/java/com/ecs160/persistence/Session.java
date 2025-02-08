package com.ecs160.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecs160.Post;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;



// Assumption - only support int/long/and string values
public class Session {

    private Jedis jedisSession;
    private List<Post> posts;

    private Session() {
        jedisSession = new Jedis("localhost", 6379);;
    }


    public void add(Object obj) {
        // Adds object to session (doesn't save yet)
    }


    public void persistAll()  {
        // Saves all added objects to Redis
    }


    public Object load(Object object)  {
        // Loads object from Redis using its ID
        return null;
    }

}
