package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Assumption - only support int/long/and string values
public class Session {
    private final Map<String,Map<String, String>> sessionObjs;
    private final RedisDB redisDB;
    private static final String ID_SUFFIX = "Ids";

    public Session() {
        this.redisDB = new RedisDB();
        this.sessionObjs = new HashMap<>();
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

        // get Hashmap of class field values and add to session
        Map<String, String> fieldMap = processFields(obj);
        if (!fieldMap.isEmpty()) {
            this.sessionObjs.put(key, fieldMap);
        }

    }

    public void persistAll()  {
        // Saves all added objects to db
        for (Map.Entry<String, Map<String, String>> entry: this.sessionObjs.entrySet()) {
            String mapKey = entry.getKey();
            Map<String, String> mapValue = entry.getValue();
            this.redisDB.setObject(mapKey, mapValue);
        }
    }

    public Object load(Object obj)  {
        // Check if object class has Persistable annotation
        if (isNotValidObject(obj)) {
            return null;
        }

        // get id annotated field in object class (to reconstruct object using it as a key)
        String key = findObjectKey(obj);
        if (key.isEmpty()) {
            System.out.println("Can't save class Object! Doesn't have a declared ID field annotation!");
            return null;
        }

        // Load Object from db
        Map<String, String> objMap = this.redisDB.loadObject(key);

        return objMap != null ? reconstructObject(objMap, obj) : null;
    }

    private Object reconstructObject(Map<String, String> objMap, Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            // extract value from objMap (consists of values loaded from db for object)
            if (field.isAnnotationPresent(PersistableId.class)) {
                continue;
            }

            String value = "";
            if (field.isAnnotationPresent(PersistableListField.class)) {
                value = objMap.get(field.getName() + ID_SUFFIX);
            } else if (field.isAnnotationPresent(PersistableField.class)) {
                value = objMap.get(field.getName());
            }
            System.out.println("Field Name & Value: " + field.getName() + " - " + value);
            try {
                if (value != null) {
                    if (field.getType() == int.class) {
                        System.out.println("Int type!");
                        field.set(obj, Integer.parseInt(value));
                    } else if (field.getType() == long.class) {
                        field.set(obj, Long.parseLong(value));
                        System.out.println("long type!");
                    } else if (field.getType() == String.class) {
                        System.out.println("String type!");
                        field.set(obj, value);
                    } else if (field.getType() == List.class) {
                        System.out.println("A list");
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException  e) {
                System.out.println("Error: " + e);
            }

//            System.out.println("Field Type: " + field.getType());
        }

//        System.out.println("ReplyIds: " + objMap.get("repliesIds"));

        return obj;
    }

    private static boolean isNotValidObject(Object obj) {
        if (obj == null) {
            System.out.println("Object cannot be null");
            return true;
        }
        return !isValidClass(obj.getClass());
    }

    private static boolean isValidClass(Class<?> clazz) {
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
        Map<String, String> fieldMap = new HashMap<>(); // key-value pairs for field name and value
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

    private void processField(Field field, Object obj, Map<String, String> fieldMap) throws IllegalAccessException, ClassNotFoundException {
        String fieldName = field.getName();
        Object fieldVal = field.get(obj);
        if (field.isAnnotationPresent(PersistableField.class)) {
            fieldMap.put(fieldName, fieldVal.toString());
        } else if (field.isAnnotationPresent(PersistableListField.class) && isList(field)) {
            String listIds = processListField(field, fieldVal);
            fieldMap.put(fieldName + ID_SUFFIX, listIds);
        }
    }

    private String processListField(Field field, Object fieldVal) throws ClassNotFoundException, IllegalAccessException {
        PersistableListField annotation = field.getAnnotation(PersistableListField.class);
        String className = "com.ecs160." + annotation.className();
        String listIds = "";
        Class<?> clazz = Class.forName(className);
        List<?> list = (List<?>) fieldVal;
        if (!list.isEmpty()) {
            for (Object item: list) { // iterate through list of objects
                if (clazz.isInstance(item)) {
                    Object castedItemObj = clazz.cast(item);
                    Class<?> castedItemClazz = castedItemObj.getClass();
                    add(castedItemObj); // adds list item into persist later HashMap (later be persisted)
                    String castedKey = findObjectKey(castedItemObj);
                    listIds = addReplyId(castedKey, listIds);
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
