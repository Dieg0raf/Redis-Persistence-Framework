package com.ecs160.persistence;
import java.util.HashMap;
import java.util.Map;

public class Session implements ObjectLoader {
    private final Map<String,Map<String, String>> sessionObjs;
    private final RedisDB redisDB;
    private final ObjectReconstructor reconstructor;
    private final ObjectProcessor processor;
    private final ObjectValidator validator;

    public Session() {
        this.redisDB = new RedisDB();
        this.sessionObjs = new HashMap<>();
        this.validator = new ObjectValidator();
        SharedUtils shared = new SharedUtils();
        this.reconstructor = new ObjectReconstructor(this, this.validator, shared);
        this.processor = new ObjectProcessor(this, this.validator, shared);
    }

    @Override
    public void add(Object obj) {
        // Check if object class has Persistable annotation
        if (this.validator.isNotValidObject(obj)) {
            return;
        }

        // get id field in object class (will act as key for redis)
        String key = this.validator.findObjectKey(obj);
        if (key.isEmpty()) {
            System.out.println("Can't save class Object! Doesn't have a declared ID field annotation!");
            return;
        }

        // get Hashmap of class field values and add to session
        Map<String, String> fieldMap = this.processor.processFields(obj);
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

    @Override
    public Object load(Object obj)  {
        // Check if object class has Persistable annotation
        if (this.validator.isNotValidObject(obj)) {
            return null;
        }

        // get id annotated field in object class (to reconstruct object using it as a key)
        String key = this.validator.findObjectKey(obj);
        if (key.isEmpty()) {
            System.out.println("Can't save class Object! Doesn't have a declared ID field annotation!");
            return null;
        }

        // Load Object from db
        Map<String, String> objMap = this.redisDB.loadObject(key);

        return objMap != null ? this.reconstructor.reconstructObject(objMap, obj) : null;
    }

    public int getAmountOfKeys() {
        return this.redisDB.getAmountOfKeys();
    }

    public void clearDB() {
        this.redisDB.clearDB();
    }
}
