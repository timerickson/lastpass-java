package com.in2bits.debug;

import com.in2bits.adapters.crypto.AesManaged;
import com.in2bits.adapters.crypto.AesManagedFactory;
import com.in2bits.shims.CipherMode;

/**
 * Created by Tim on 7/22/17.
 */

public class SimpleAesManagedFactory implements AesManagedFactory {
    @Override
    public AesManaged create(int size, byte[] encryptionKey, CipherMode mode, byte[] iv) {
        return new SimpleAesManaged(size, encryptionKey, mode, iv);
    }
}
