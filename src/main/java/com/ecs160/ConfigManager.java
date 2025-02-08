package com.ecs160;

public class ConfigManager {
    public String getFilePathFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("file=")) {
                return arg.substring(5);
            }
        }
        return "input.json";  // default
    }

    public String getWeightFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("weighted=")) {
                return arg.substring(9);
            }
        }
        return "false";
    }
}
