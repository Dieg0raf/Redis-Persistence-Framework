package com.ecs160.persistence;

import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Assumption - only support int/long/and string values
public class Session {

    private final Jedis jedisSession;
    private final Map<String,Map<String, String>> objs;

    public Session() {
        jedisSession = new Jedis("localhost", 6379);;
        this.objs = new HashMap<>();
    }

    public void add(Object obj) {
        // Check if object class has Persistable annotation
        if (isNotValidObject(obj)) {
            return;
        }

        // get id field in object class (will act as key for redis)
        String key = findObjectKey(obj);
        if (key.isEmpty()) {
            System.out.println("Can't save class Object! Doesn't have a declared ID field annotation!");
            return;
        }

        // construct Hashmap of class field values
        Map<String, String> fieldMap = processFields(obj);
        if (!fieldMap.isEmpty()) {
            this.objs.put(key, fieldMap);
        }
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


    private boolean isNotValidObject(Object obj) {
        if (obj == null) {
            System.out.println("Object cannot be null");
            return true;
        }
        return !isValidClass(obj.getClass());
    }

    private boolean isValidClass(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Persistable.class)) { // check if class has Persistable annotation
            System.out.println("Class has DOES NOT Persistable annotation! Can't be saved!");
            return false;
        }
        return true;
    }

    private String findObjectKey(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PersistableId.class)) {
                    return field.get(obj).toString(); // returns the id field value as a string
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error accessing ID field: " + e.getMessage());
            }
        }
        return "";
    }

    private Map<String, String> processFields(Object obj) {
        Map<String, String> fieldMap = new HashMap<>(); // key-value pair for field name and value
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PersistableId.class)) {
                    // Skip ID field as it's handled separately
                    continue;
                }

                processField(field, obj, fieldMap);
            } catch (IllegalAccessException | ClassNotFoundException e) {
                System.out.println("Error processing field: " + e.getMessage());
            }
        }
        return fieldMap;
    }

    private void processField(Field field, Object obj, Map<String, String> fieldMap)
            throws IllegalAccessException, ClassNotFoundException {
        String fieldName = field.getName();
        Object fieldVal = field.get(obj);
        if (field.isAnnotationPresent(PersistableField.class)) {
            fieldMap.put(fieldName, fieldVal.toString());
        } else if (field.isAnnotationPresent(PersistableListField.class) && isList(field)) {
            String listIds = processListField(field, fieldVal);
            fieldMap.put(fieldName + "Ids", listIds);
        }
    }

    private String processListField(Field field, Object fieldVal) throws ClassNotFoundException, IllegalAccessException {
        PersistableListField annotation = field.getAnnotation(PersistableListField.class);
        String className = "com.ecs160." + annotation.className();
        String listIds = "";
        Class<?> clazz = Class.forName(className);
        List<?> list = (List<?>) fieldVal;
        for (Object item: list) { // iterate through list of objects
            if (clazz.isInstance(item)) {
                Object castedItemObj = clazz.cast(item);
                if (isNotValidObject(castedItemObj)) {
                    // skip this object if object class does not have Persistable annotation
                    continue;
                }
                Class<?> castedItemClazz = castedItemObj.getClass();
                for (Field castedItemfield: castedItemClazz.getDeclaredFields()) { // iterate through fields of each object in list
                    castedItemfield.setAccessible(true);
                    if (castedItemfield.isAnnotationPresent(PersistableId.class)) {
                        Object castedFieldVal = castedItemfield.get(castedItemObj);
                        String castedKey = castedFieldVal.toString(); // get list object ids
                        listIds = addReplyId(castedKey, listIds);
                    }
                }
            }
        }
        return listIds;
    }

    private static String addReplyId(String newId, String replyIds) {
        if (replyIds.isEmpty()) {
            replyIds = newId;
        } else {
            replyIds += "," + newId;
        }
        return replyIds;
    }

    private static boolean isList(Field field) {
        Class<?> fieldType = field.getType();
        return List.class.isAssignableFrom(fieldType);
    }

}
