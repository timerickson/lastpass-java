package com.in2bits.debug;

import com.in2bits.adapters.crypto.AesManaged;
import com.in2bits.shims.CipherMode;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;

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
//        Cipher cipher = null;
        String plain;
        try {
//            Security.addProvider(new BouncyCastlePQCProvider());
//            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new BouncyCastlePQCProvider());
//            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new IvParameterSpec(iv));
//            plain = new String(cipher.doFinal(encrypted), "UTF-8");
            KeyParameter keyParam = new KeyParameter(encryptionKey);
            CipherParameters params = new ParametersWithIV(keyParam, iv);
            BlockCipherPadding padding = new PKCS7Padding();
            BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                    new CBCBlockCipher(new AESEngine()), padding);
            cipher.reset();
            cipher.init(false, params);
            byte[] buffer = new byte[cipher.getOutputSize(encrypted.length)];
            int len = cipher.processBytes(encrypted, 0, encrypted.length, buffer, 0);
            len += cipher.doFinal(buffer, len);
            byte[] out = Arrays.copyOfRange(buffer, 0, len);
            plain = new String(out, "UTF-8");
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
