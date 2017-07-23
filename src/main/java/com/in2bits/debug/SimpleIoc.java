package com.in2bits.debug;

import com.in2bits.shims.Ioc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tim on 7/20/17.
 */
public class SimpleIoc implements Ioc {
    Map<Class<?>, Object> map = new HashMap<>();

    @Override
    public <T> void register(Class<T> clazz, T instance) {
        map.put(clazz, instance);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        return (T) map.get(clazz);
    }
}
