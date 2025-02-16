package com.ecs160.persistence;

import java.lang.reflect.Field;

public class SharedUtils {
    public String getClassName(Field field) {
        PersistableListField annotation = field.getAnnotation(PersistableListField.class);
        return "com.ecs160." + annotation.className();
    }
}
