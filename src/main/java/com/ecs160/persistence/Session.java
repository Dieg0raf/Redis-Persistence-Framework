package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Assumption - only support int/long/and string values
public class Session {

//    private Jedis jedisSession;
    private List<Map<String, String>> objs;

    public Session() {
//        jedisSession = new Jedis("localhost", 6379);;
        this.objs = new ArrayList<>();
    }

    public void savePost(Object obj) {
//        isRedisAvailable();
//        Class<?> currentClazz = obj.getClass();
//        Map<String, String> postMap = new HashMap<>();
//
//        while (currentClazz != null) { // Search through current class and super class fields using reflection
//            for (Field field : currentClazz.getDeclaredFields()) {
//                field.setAccessible(true);
//                try {
//                    String fieldName = field.getName();
//                    Object fieldVal = field.get(obj);
//                    System.out.println("FieldName: " + fieldName);
//                    System.out.println("FieldVal: " + fieldVal);
//                    if (isList(field)) {
//                        List<?> replyObList = (List<?>) fieldVal;
//                        String replyIds = "";
//                        for (Object replyObj: replyObList) {
//                            Class<?> replyClazz = replyObj.getClass();
//                            while (replyClazz != null) {
////                                System.out.println("replyObj: " + replyObj.getClass());
//                                for (Field replyField: replyClazz.getDeclaredFields()) {
//                                    replyField.setAccessible(true);
//                                    String replyFieldName = replyField.getName();
//                                    Object replyFieldVal = replyField.get(replyObj);
//                                    if (replyFieldName.equals("postId")) {
//                                        addReplyId((String) replyFieldVal , replyIds);
//                                    }
//                                    System.out.println("replyieldName: " + replyFieldName);
//                                    System.out.println("replyFieldVal: " + replyFieldVal);
//                                }
//
//                                replyClazz = replyClazz.getSuperclass();
//                            }
//                        }
//                        System.out.println("Reply ids: " + replyIds);
//                        // left off figuring out how to iterate through list of replies
//                        // after figuring out it's a list
//                    }
//                    postMap.put(fieldName, fieldVal.toString());
//                } catch (IllegalAccessException e) {
//                    System.err.println("Error accessing field: " + e.getMessage());
//                }
//            }
//            currentClazz = currentClazz.getSuperclass();
//        }
//
//        this.objs.add(postMap);
//        this.jedis.hset(generatePostId(), postMap); // save object into db

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

    public void addReplyId(String newId, String replyIds) {
        if (replyIds.isEmpty()) {
            replyIds = newId;
        } else {
            replyIds += "," + newId;
        }
    }

    public static boolean isList(Field field) {
        Class<?> fieldType = field.getType();
        return List.class.isAssignableFrom(fieldType);
    }

}
