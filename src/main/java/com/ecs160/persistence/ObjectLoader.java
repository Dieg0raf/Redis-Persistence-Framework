package com.ecs160.persistence;

public interface ObjectLoader {
    Object load(Object obj);
    void add(Object obj);
}
