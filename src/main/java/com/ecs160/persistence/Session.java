package com.ecs160.persistence;

import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Assumption - only support int/long/and string values
public class Session {

    private final Jedis jedisSession;
    private Map<String,Map<String, String>> objs;

    public Session() {
        jedisSession = new Jedis("localhost", 6379);;
        this.objs = new HashMap<>();
    }

    public void savePost(Object obj) {
//        TODO: Add checks for redis availability
//        isRedisAvailable();
        Class<?> currentClazz = obj.getClass();

        if (!currentClazz.isAnnotationPresent(Persistable.class)) { // check if class has Persistable annotation
            System.out.println("Class has DOES NOT Persistable annotation! Can't be saved!");
            return;
        }

        Map<String, Map<String, String>> outerMap = new HashMap<>();
        Map<String, String> objMap = new HashMap<>();

        String key = null;
        String replyIds = "";
        // Search through current class fields using reflection
        for (Field field : currentClazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String fieldName = field.getName();
                Object fieldVal = field.get(obj);

                if (field.isAnnotationPresent(PersistableId.class)) { // grab key with annotation
                    key = fieldVal.toString(); // saves key
                } else if (field.isAnnotationPresent(PersistableField.class)) { // persist fields with annotation
//                    System.out.println("FieldName: " + fieldName);
//                    System.out.println("FieldVal: " + fieldVal);
                    objMap.put(fieldName, fieldVal.toString());
                } else if (field.isAnnotationPresent(PersistableListField.class) && isList(field)) { // persist lists with annotation
                    PersistableListField annotation = field.getAnnotation(PersistableListField.class);
                    String className = "com.ecs160." + annotation.className();
                    Class<?> clazz = Class.forName(className);
                    List<?> list = (List<?>) fieldVal;
                    for (Object item: list) { // iterate through list
                        if (clazz.isInstance(item)) { // check the types of each element
//                            System.out.println("Reply is of type: " + className);
                            Object castedItemObj = clazz.cast(item);
                            Class<?> castedItemClazz = castedItemObj.getClass();
                            for (Field castedItemfield: castedItemClazz.getDeclaredFields()) {
                                castedItemfield.setAccessible(true);
                                String castedFieldName = castedItemfield.getName();
                                Object castedFieldVal = castedItemfield.get(castedItemObj);
                                if (!castedItemClazz.isAnnotationPresent(Persistable.class)) { // check if class has Persistable annotation
                                    System.out.println("Class has DOES NOT Persistable annotation! Can't be saved!");
                                    continue;
                                }

                                if (castedItemfield.isAnnotationPresent(PersistableId.class)) {
                                    String castedKey = castedFieldVal.toString(); // saves key
                                    replyIds = addReplyId(castedKey, replyIds);
                                }
                            }
                        }
                    }
                    objMap.put(fieldName + "Ids", replyIds); // add string for ids in list
                }

            } catch (IllegalAccessException e) {
                System.out.println("Error accessing field: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        if (key == null) {
            System.out.println("Can't save class Object! Doesn't have a declared ID field annotation!");
            return;
        } else {
            if (!objMap.isEmpty()) {
                this.objs.put(key, objMap); // save key with objMap for later persisting (not persisted yet)
            }
        }
    }

    public void add(Object obj) {
        // Adds object to session (doesn't save yet)
    }


    public void persistAll()  {
        // Saves all added objects to Redis
        for (Map.Entry<String, Map<String, String>> entry: this.objs.entrySet()) {
            String mapKey = entry.getKey();
            Map<String, String> mapValue = entry.getValue();
            this.jedisSession.hset(mapKey, mapValue);
        }

    }


    public Object load(Object object)  {
        // Loads object from Redis using its ID
        return null;
    }

    public String addReplyId(String newId, String replyIds) {
        if (replyIds.isEmpty()) {
            replyIds = newId;
        } else {
            replyIds += "," + newId;
        }

        return replyIds;
    }

    public static boolean isList(Field field) {
        Class<?> fieldType = field.getType();
        return List.class.isAssignableFrom(fieldType);
    }

}
