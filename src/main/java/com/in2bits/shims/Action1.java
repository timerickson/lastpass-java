package com.in2bits.shims;

import java.io.IOException;

/**
 * Created by Tim on 8/14/17.
 */

public interface Action1 <T> {
    void execute(T t) throws IOException;
}
