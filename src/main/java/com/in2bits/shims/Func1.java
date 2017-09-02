package com.in2bits.shims;

import java.io.IOException;

/**
 * Created by Tim on 7/4/17.
 */

public interface Func1<T, T2> {
    T2 execute(T t) throws IOException;
}
