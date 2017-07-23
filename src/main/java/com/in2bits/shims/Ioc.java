package com.in2bits.shims;

/**
 * Created by Tim on 7/19/17.
 */

public interface Ioc {
    <T> void register(Class<T> clazz, T instance);
    <T> T get(Class<T> clazz);
}
