package com.in2bits.debug;

import com.in2bits.shims.Func;
import com.in2bits.shims.Func1;
import com.in2bits.shims.Ioc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tim on 7/20/17.
 */
public class SimpleIoc implements Ioc {
    Map<Class<?>, Object> instanceMap = new HashMap<>();
    Map<Class<?>, Func<?>> generatorMap = new HashMap<>();

    @Override
    public <T> void register(Class<T> clazz, T instance) {
        instanceMap.put(clazz, instance);
    }

    @Override
    public <T> void register(Class<T> clazz, Func<T> generator) {
        generatorMap.put(clazz, generator);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        T instance = (T) instanceMap.get(clazz);
        if (instance == null) {
            Func<T> generator = (Func<T>) generatorMap.get(clazz);
            if (generator == null) {
                return null;
            }
            instance = generator.execute();
        }
        return instance;
    }
}
