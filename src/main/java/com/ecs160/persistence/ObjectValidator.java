package com.ecs160.persistence;

import java.lang.reflect.Field;

public class ObjectValidator {
    // NOT - Reconstruction
    public boolean isNotValidObject(Object obj) {
        if (obj == null) {
            System.out.println("Object cannot be null");
            return true;
        }
        return !isValidClass(obj.getClass());
    }

    // NOT - Reconstruction
    public String findObjectKey(Object obj) {
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

    // Reconstruction
    public String findObjectKeyName(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PersistableId.class)) {
                return field.getName(); // returns the id field name as a string
            }
        }
        return "";
    }

    // NOT - Reconstruction
    private static boolean isValidClass(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Persistable.class)) { // check if class has Persistable annotation
            System.out.println("Class has DOES NOT Persistable annotation! Can't be saved!");
            return false;
        }
        return true;
    }

}
