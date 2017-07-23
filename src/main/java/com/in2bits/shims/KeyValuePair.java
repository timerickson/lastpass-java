package com.in2bits.shims;

/**
 * Created by Tim on 7/3/17.
 */

public class KeyValuePair<T, T1> {
    final private T key;
    final private T1 value;

    public KeyValuePair(T kind, T1 payload) {
        this.key = kind;
        this.value = payload;
    }

    public T getKey() {
        return key;
    }

    public T1 getValue() {
        return value;
    }
}
