package com.in2bits.shims;

/**
 * Created by Tim on 7/19/17.
 */

public interface Ioc {
    <T> void register(Class<T> clazz, T instance);
    <T> void register(Class<T> clazz, Func<T> generator);
    <T> T get(Class<T> clazz);
}
