package com.in2bits;

/**
 * Created by Tim on 7/3/17.
 */

public class Session {

    private final String id;
    private final int keyIterationCount;

    public Session(String id, int keyIterationCount) {
        this.id = id;
        this.keyIterationCount = keyIterationCount;
    }

    public String getId() {
        return id;
    }

    public int getKeyIterationCount() {
        return keyIterationCount;
    }
}
