package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    public int getAmountOfKeys() {
        return this.redisDB.getAmountOfKeys();
    }

    public void clearDB() {
        this.redisDB.clearDB();
    }

    // TODO: Figure out how to reconstruct Reply Post
    private Object reconstructObject(Map<String, String> objMap, Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            // extract value from objMap (consists of values loaded from db for object)
            if (field.isAnnotationPresent(PersistableId.class)) {
                continue;
            }

            String value = "";
            if (field.isAnnotationPresent(PersistableListField.class)) {
                try {
                    Class<?> clazz = Class.forName(getClassName(field));
                    String listIds = objMap.get(field.getName() + ID_SUFFIX);

                    if (listIds.isEmpty()) {
                        continue;
                    }

                    List<Integer> individualIds = parseReplyIds(listIds);
                    List<Object> newList = new ArrayList<>();

                    for (Integer id: individualIds) {
                        Object newObj = clazz.getDeclaredConstructor().newInstance();
                        String keyName = findObjectKeyName(newObj);
                        invokeSetter(newObj, keyName, id); // sets key field for newly created object
                        newList.add(load(newObj));
                    }

                    field.set(obj, newList); // set list field to the new list
                    continue;
                }
                catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                        IllegalAccessException e) {
                    System.out.println("An error happening inside reconstructObject");
                }
            }

            if (field.isAnnotationPresent(PersistableField.class)) {
                value = objMap.get(field.getName());
            }

            try { // setting value to fields in object class (assuming "Only fields of type Integer and String are persistable.")
                if (!value.isEmpty()) {
                    if (field.getType() == Integer.class) {
                        field.set(obj, Integer.parseInt(value));
                    } else if (field.getType() == String.class) {
                        field.set(obj, value);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException  e) {
                System.out.println("Error: " + e);
            }

        }
        return obj;
    }

    private static void invokeSetter(Object obj, String fieldName, Object value) {
        try {
            // Convert field name to setter method name ("name" -> "setName")
            String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

            // Find the setter method with appropriate parameter type
            Method setter = null;
            for (Method method : obj.getClass().getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                    setter = method;
                    break;
                }
            }

            if (setter != null) {
                setter.invoke(obj, value); // Invoke setter with the provided value
            } else {
                System.out.println("Setter not found for field: " + fieldName);
            }
        } catch (Exception e) {
            System.out.println("Error occurred when invoking setters: " + e);
        }
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

    private String findObjectKeyName(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PersistableId.class)) {
                return field.getName(); // returns the id field name as a string
            }
        }
        return "";
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

    private String getClassName(Field field) {
        PersistableListField annotation = field.getAnnotation(PersistableListField.class);
        return "com.ecs160." + annotation.className();
    }

    private String processListField(Field field, Object fieldVal) throws ClassNotFoundException, IllegalAccessException {
        String listIds = "";
        Class<?> clazz = Class.forName(getClassName(field));
        List<?> list = (List<?>) fieldVal;
        if (!list.isEmpty()) {
            for (Object item: list) { // iterate through list of objects
                if (clazz.isInstance(item)) {
                    Object castedItemObj = clazz.cast(item);
                    Class<?> castedItemClazz = castedItemObj.getClass();
                    add(castedItemObj); // adds list item into persist later HashMap
                    String castedKey = findObjectKey(castedItemObj);
                    listIds = addListId(castedKey, listIds);
                }
            }
        }
        return listIds;
    }

    private static String addListId(String newId, String listIds) {
        if (listIds.isEmpty()) {
            listIds = newId;
        } else {
            listIds += "," + newId;
        }
        return listIds;
    }

    private static List<Integer> parseReplyIds(String listIds) {
        List<Integer> ids = new ArrayList<>();
        if (listIds != null && !listIds.isEmpty()) {
            String[] idArray = listIds.split(",");
            for (String id : idArray) {
                try {
                    ids.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Warning: Invalid integer found in listIds: " + id);
                }
            }
        }
        return ids;
    }

    private static boolean isList(Field field) {
        Class<?> fieldType = field.getType();
        return List.class.isAssignableFrom(fieldType);
    }

}
