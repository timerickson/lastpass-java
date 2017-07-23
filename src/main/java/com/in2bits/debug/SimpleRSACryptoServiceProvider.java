package com.in2bits.debug;

import com.in2bits.adapters.crypto.RSACryptoServiceProvider;
import com.in2bits.shims.RSAParameters;

import java.io.IOException;

/**
 * Created by Tim on 7/6/17.
 */

public class SimpleRSACryptoServiceProvider implements RSACryptoServiceProvider {
    @Override
    public void close() throws IOException {

    }

    @Override
    public void importParameters(RSAParameters rsaKey) {
       throw new RuntimeException("Not Implemented: com.in2bits.shims.RSACryptoServiceProvider.importParameters");
    }

    @Override
    public byte[] decrypt(byte[] bytes, boolean b) {
        throw new RuntimeException("Not Implemented: com.in2bits.shims.RSACryptoServiceProvider.decrypt");
    }
}
