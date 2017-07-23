package com.in2bits.debug;

import com.in2bits.adapters.crypto.HmacSHA256;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StubHmacSHA256 implements HmacSHA256 {

    final private Mac mac;
    private byte[] key;

    @Override
    public void close() throws IOException {
    }

    public StubHmacSHA256() {
        try {
            mac = Mac.getInstance("HmacSHA256");
            System.out.println("Mac Length " + mac.getMacLength());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Crypto Error: No Such Algorithm HMAC SHA256", e);
        }
    }

    @Override
    public void setKey(final byte[] keyBytes) {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        try {
            mac.init(key);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid Key", e);
        }
    }

    @Override
    public int getHashSize() {
        return mac.getMacLength() * 8;
    }

    @Override
    public byte[] computeHash(byte[] hashInput) {
        byte[] hash = mac.doFinal(hashInput);
        mac.reset();
        return hash;
    }
}
