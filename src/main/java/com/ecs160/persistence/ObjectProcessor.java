package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectProcessor {
    private final ObjectLoader loader;
    private final ObjectValidator validator;
    private final SharedUtils shared;

    public ObjectProcessor(ObjectLoader loader, ObjectValidator validator, SharedUtils shared) {
        this.loader = loader;
        this.validator = validator;
        this.shared = shared;
    }

    // Processing
    public Map<String, String> processFields(Object obj) {
        Map<String, String> fieldMap = new HashMap<>(); // key-value pairs for field name and value
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                // Skip ID field as it's handled separately
                if (field.isAnnotationPresent(PersistableId.class)) {
                    continue;
                }
                processField(field, obj, fieldMap);
            } catch (Exception e) { // Catch all exceptions to prevent failures
                System.err.println("Error processing field '" + field.getName() + "': " + e.getMessage());
            }
        }
        return fieldMap;
    }

    // Processing
    private void processField(Field field, Object obj, Map<String, String> fieldMap) {
        try {
            String fieldName = field.getName();
            Object fieldVal = field.get(obj);
            if (field.isAnnotationPresent(PersistableField.class)) {
                fieldMap.put(fieldName, fieldVal.toString());
            } else if (field.isAnnotationPresent(PersistableListField.class) && isList(field)) {
                String listIds = processListField(field, fieldVal);
                fieldMap.put(fieldName + Constants.getIdSuffix(), listIds);
            }
        } catch (IllegalAccessException e) {
            System.err.println("Illegal access to field '" + field.getName() + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error processing field '" + field.getName() + "': " + e.getMessage());
        }
    }

    // Processing
    private String processListField(Field field, Object fieldVal) {
        try {
            String listIds = "";
            Class<?> clazz = Class.forName(this.shared.getClassName(field));
            List<?> list = (List<?>) fieldVal;

            if (!list.isEmpty()) {
                for (Object item : list) { // iterate through list of objects
                    if (clazz.isInstance(item)) {
                        Object castedItemObj = clazz.cast(item);
                        this.loader.add(castedItemObj); // adds list item into persist later HashMap
                        String castedKey = this.validator.findObjectKey(castedItemObj);
                        listIds = addListId(castedKey, listIds);
                    }
                }
            }
            return listIds;
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found for field '" + field.getName() + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing list field '" + field.getName() + "': " + e.getMessage());
        }
        return "";
    }

    // Processing
    private static String addListId(String newId, String listIds) {
        return listIds.isEmpty() ? newId : listIds + "," + newId;
    }

    // Processing
    private static boolean isList(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }
}
