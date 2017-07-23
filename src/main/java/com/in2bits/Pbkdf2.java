package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.crypto.HmacSHA256;
import com.in2bits.debug.StubHmacSHA256;

import java.io.IOException;

abstract class Pbkdf2
{
    public static byte[] Generate(String password, String salt, int iterationCount, int byteCount) {
        return Generate(password.getBytes(), salt.getBytes(), iterationCount, byteCount);
    }

    public static byte[] Generate(String password, byte[] salt, int iterationCount, int byteCount) {
        return Generate(password.getBytes(), salt, iterationCount, byteCount);
    }

    public static byte[] Generate(byte[] password, String salt, int iterationCount, int byteCount) {
        return Generate(password, salt.getBytes(), iterationCount, byteCount);
    }

    public static byte[] Generate(byte[] password, byte[] salt, int iterationCount, int byteCount) {
        if (iterationCount <= 0)
            throw new IllegalArgumentException("Iteration count should be positive");

        if (byteCount < 0)
            throw new IllegalArgumentException("Byte count should be nonnegative");

        try (HmacSHA256 hmac = new StubHmacSHA256())
        {
            hmac.setKey(password);

            // Prepare hash input (salt + block index)
            int hashInputSize = salt.length + 4;
            byte[] hashInput = new byte[hashInputSize];
            System.arraycopy(salt, 0, hashInput, 0, salt.length);
            hashInput[hashInputSize - 4] = 0;
            hashInput[hashInputSize - 3] = 0;
            hashInput[hashInputSize - 2] = 0;
            hashInput[hashInputSize - 1] = 0;

            byte[] bytes = new byte[byteCount];
            int hashSize = hmac.getHashSize() / 8;
            int blockCount = (byteCount + hashSize - 1) / hashSize;

            for (int i = 0; i < blockCount; ++i)
            {
                // Increase 32-bit big-endian block index at the end of the hash input buffer
                if (++hashInput[hashInputSize - 1] == 0)
                    if (++hashInput[hashInputSize - 2] == 0)
                        if (++hashInput[hashInputSize - 3] == 0)
                            ++hashInput[hashInputSize - 4];

                byte[] hashed = hmac.computeHash(hashInput);
                byte[] block = hashed;
                for (int j = 1; j < iterationCount; ++j)
                {
                    hashed = hmac.computeHash(hashed);
                    for (int k = 0; k < hashed.length; ++k)
                    {
                        block[k] ^= hashed[k];
                    }
                }

                int offset = i * hashSize;
                int size = Math.min(hashSize, byteCount - offset);
                System.arraycopy(block, 0, bytes, offset, size);
            }

            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Pbkdf2.Generate(byte[], byte[], int, int", e);
        }
    }
}

