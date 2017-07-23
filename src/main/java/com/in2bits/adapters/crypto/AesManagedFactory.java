package com.in2bits.adapters.crypto;

import com.in2bits.shims.CipherMode;

/**
 * Created by Tim on 7/22/17.
 */

public interface AesManagedFactory {
    AesManaged create(int i, byte[] encryptionKey, CipherMode mode, byte[] iv);
}
