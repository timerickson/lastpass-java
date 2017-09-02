package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.crypto.SHA256Managed;
import com.in2bits.shims.Ioc;

import java.io.IOException;

class FetcherHelper
{
    private final Ioc ioc;

    public FetcherHelper(Ioc ioc) {
        this.ioc = ioc;
    }

    public byte[] makeKey(String username, String password, int iterationCount) {
        if (iterationCount <= 0)
            throw new RuntimeException("FetcherHelper.makeKey: Iteration count should be positive");

        if (iterationCount == 1)
        {
            try (SHA256Managed sha = ioc.get(SHA256Managed.class))
            {
                return sha.computeHash((username + password).getBytes("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException("FetcherHelper.makeKey", e);
            }
        }

        return Pbkdf2.Generate(password, username, iterationCount, 32);
    }

    public String makeHash(String username, String password, int iterationCount) {
        if (iterationCount <= 0)
            throw new RuntimeException("FetcherHelper.makeHash: Iteration count should be positive");

        byte[] key = makeKey(username, password, iterationCount);
        if (iterationCount == 1)
        {
            try (SHA256Managed sha = ioc.get(SHA256Managed.class))
            {
                return Extensions.Bytes.toHex(sha.computeHash((Extensions.Bytes.toHex(key) + password).getBytes()));
            } catch (IOException e) {
                throw new RuntimeException("FetcherHelper.makeHash", e);
            }
        }

        return Extensions.Bytes.toHex(Pbkdf2.Generate(key, password, 1, 32));
    }
}

