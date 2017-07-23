package com.in2bits.adapters.crypto;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Tim on 7/4/17.
 */

public interface SHA256Managed extends Closeable {
    @Override
    void close() throws IOException;

    byte[] computeHash(byte[] bytes);
}
