package com.ecs160.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javassist.util.proxy.ProxyFactory;

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
        // Check if this object should be lazy loaded
        boolean hasLazyLoad = false;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(LazyLoad.class)) {
                hasLazyLoad = true;
                break;
            }
        }

        if (hasLazyLoad) {
            // For lazy loaded objects, only set the ID field
            try {
                String idFieldName = this.validator.findObjectKeyName(obj);
                Field idField = obj.getClass().getDeclaredField(idFieldName);
                idField.setAccessible(true);
                String idValue = objMap.get(idFieldName);
                if (idValue != null) {
                    idField.set(obj, Integer.parseInt(idValue));
                }
                return createLazyProxy(obj);
            } catch (Exception e) {
                System.out.println("Error setting ID field for lazy loading: " + e.getMessage());
                return obj;
            }
        }

        // Original reconstruction code for non-lazy objects...
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
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
            for (Integer id : individualIds) {
                Object newObj = clazz.getDeclaredConstructor().newInstance();
                String keyName = this.validator.findObjectKeyName(newObj);
                this.refUtils.invokeSetter(newObj, keyName, id); // sets key field for newly created object
                newList.add(this.loader.load(newObj));
            }
            field.set(obj, newList); // set list field to the new list
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            System.out.println("An error happening inside reconstructObject");
        }
    }

    private void reconstructField(Map<String, String> objMap, Field field, Object obj) {
        // setting value to fields in object class (assuming "Only fields of type
        // Integer and String are persistable."
        try {
            String value = objMap.get(field.getName());
            if (!value.isEmpty()) {
                if (field.getType() == Integer.class) {
                    field.set(obj, Integer.parseInt(value));
                } else if (field.getType() == String.class) {
                    field.set(obj, value);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
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

    private Object createLazyProxy(Object obj) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(obj.getClass());

            Class<?> proxyClass = factory.createClass();
            Object proxy = proxyClass.getDeclaredConstructor().newInstance();

            // Copy the ID field from original object to proxy
            String idFieldName = this.validator.findObjectKeyName(obj);
            Field idField = obj.getClass().getDeclaredField(idFieldName);
            idField.setAccessible(true);
            Object idValue = idField.get(obj);
            idField.set(proxy, idValue);

            ((javassist.util.proxy.Proxy) proxy).setHandler(
                    new LazyLoadHandler(this.loader, obj));

            return proxy;
        } catch (Exception e) {
            System.out.println("Error creating lazy proxy: " + e.getMessage());
            return obj;
        }
    }
}
