package com.ecs160.persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public void invokeSetter(Object obj, String fieldName, Object value) {
        try {
            // Convert field name to setter method name ("name" -> "setName")
            String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method setter = findSetter(obj, methodName);

            if (setter == null) {
                System.out.println("Setter not found for field: " + fieldName);
                return;
            }
            setter.invoke(obj, value); // Invoke setter with the provided value

        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Error occurred when invoking setters: " + e);
        }
    }

    private static Method findSetter(Object obj, String methodName) {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }
}
