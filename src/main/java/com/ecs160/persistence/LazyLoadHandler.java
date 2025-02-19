package com.ecs160.persistence;

import javassist.util.proxy.MethodHandler;
import java.lang.reflect.Method;

public class LazyLoadHandler implements MethodHandler {
    private final ObjectLoader loader;
    private final Object target;
    private Object loadedObj = null;
    private boolean loading = false;

    public LazyLoadHandler(ObjectLoader loader, Object target) {
        this.loader = loader;
        this.target = target;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        String methodName = thisMethod.getName();

        // If we're already loading or the method is not a getter, use the proceed
        // method
        if (loading || !methodName.startsWith("get")) {
            return proceed.invoke(self, args);
        }

        // Load the object if not already loaded
        if (loadedObj == null) {
            try {
                loading = true;
                loadedObj = loader.load(target);
                if (loadedObj == null) {
                    return null;
                }
            } finally {
                loading = false;
            }
        }

        // Invoke the method on the loaded object
        return thisMethod.invoke(loadedObj, args);
    }
}
