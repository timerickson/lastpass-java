package com.in2bits.adapters.crypto;

import java.io.Closeable;

/**
 * Created by Tim on 7/22/17.
 */

public interface AesManaged extends Closeable {
    String decrypt(byte[] encrypted);
}
