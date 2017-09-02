package com.in2bits.debug;

import com.in2bits.adapters.crypto.SHA256Managed;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Tim on 8/13/17.
 */

public class SimpleSha256Managed implements SHA256Managed {
    @Override
    public void close() throws IOException {

    }

    @Override
    public byte[] computeHash(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found", e);
        }
        md.update(bytes);
        byte[] digest = md.digest();
        return digest;
    }
}
