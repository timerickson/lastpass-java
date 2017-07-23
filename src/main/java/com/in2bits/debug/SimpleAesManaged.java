package com.in2bits.debug;

import com.in2bits.adapters.crypto.AesManaged;
import com.in2bits.shims.CipherMode;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Tim on 7/22/17.
 */

public class SimpleAesManaged implements AesManaged {
    final private int size;
    final private byte[] encryptionKey;
    final private CipherMode mode;
    final private byte[] iv;
    public SimpleAesManaged(int size, byte[] encryptionKey, CipherMode mode, byte[] iv) {
        this.size = size;
        this.encryptionKey = encryptionKey;
        this.mode = mode;
        this.iv = iv;
    }

    @Override
    public String decrypt(byte[] encrypted) {
        Cipher cipher = null;
        String plain;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new IvParameterSpec(iv));
            plain = new String(cipher.doFinal(encrypted), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("decrypt error in SimpleAesManaged", e);
        }
        return plain;
    }

    @Override
    public void close() throws IOException {
        //no-op
    }
}
