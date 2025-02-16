package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectReconstructor {
    private final ObjectLoader loader;
    private final SharedUtils shared;
    private final ObjectValidator validator;
    private final ReflectionUtils refUtils;

    public ObjectReconstructor(ObjectLoader loader, ObjectValidator validator, SharedUtils shared) {
        this.loader = loader;
        this.shared = shared;
        this.validator = validator;
        this.refUtils = new ReflectionUtils();
    }

    // Reconstruction
    public Object reconstructObject(Map<String, String> objMap, Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            // Check type of Persistable field annotation (if applicable)
            if (field.isAnnotationPresent(PersistableId.class)) {
                continue;
            } else if (field.isAnnotationPresent(PersistableListField.class)) {
                reconstructListField(objMap, field, obj);
            } else if (field.isAnnotationPresent(PersistableField.class)) {
                reconstructField(objMap, field, obj);
            }
        }
        return obj;
    }

    private void reconstructListField(Map<String, String> objMap, Field field, Object obj) {
        try {
            Class<?> clazz = Class.forName(this.shared.getClassName(field));
            String listIds = objMap.get(field.getName() + Constants.getIdSuffix());

            if (listIds.isEmpty()) {
                return;
            }

            List<Integer> individualIds = parseReplyIds(listIds);
            List<Object> newList = new ArrayList<>();
            for (Integer id: individualIds) {
                Object newObj = clazz.getDeclaredConstructor().newInstance();
                String keyName = this.validator.findObjectKeyName(newObj);
                this.refUtils.invokeSetter(newObj, keyName, id); // sets key field for newly created object
                newList.add(this.loader.load(newObj));
            }
            field.set(obj, newList); // set list field to the new list
        }
        catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
               IllegalAccessException e) {
            System.out.println("An error happening inside reconstructObject");
        }
    }

    private void reconstructField(Map<String, String> objMap,  Field field, Object obj) {
        // setting value to fields in object class (assuming "Only fields of type Integer and String are persistable."
        try {
            String value = objMap.get(field.getName());
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

    // Reconstruction
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
}
