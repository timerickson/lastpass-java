package com.in2bits.adapters.crypto;

import com.in2bits.shims.RSAParameters;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Tim on 7/20/17.
 */

public interface RSACryptoServiceProvider extends Closeable {
    @Override
    void close() throws IOException;

    void importParameters(RSAParameters rsaKey);

    byte[] decrypt(byte[] bytes, boolean b);
}
