package com.ecs160;

public class IdGenerator {
    private static int idCounter = 0;

    private IdGenerator() {
        // Private constructor to prevent instantiation
    }

    public static synchronized int generateUniqueId() {
        return ++idCounter;
    }
}
