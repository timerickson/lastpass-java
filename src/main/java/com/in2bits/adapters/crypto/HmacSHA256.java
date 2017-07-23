package com.in2bits.adapters.crypto;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Tim on 7/20/17.
 */

public interface HmacSHA256 extends Closeable {
    @Override
    void close() throws IOException;

    void setKey(byte[] key);

    int getHashSize();

    byte[] computeHash(byte[] hashInput);
}
