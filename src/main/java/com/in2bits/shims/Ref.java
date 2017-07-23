package com.in2bits.shims;

// https://www.codeproject.com/articles/683970/simulating-csharp-ref-parameter-in-java

public class Ref<T> {
    private T value;
    private boolean hasValue;

    public Ref() {
    }

    public Ref(T value) {
        this.setValue(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        this.hasValue = true;
    }

    public boolean hasValue() {
        return hasValue;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
