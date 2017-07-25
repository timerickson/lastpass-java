package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.shims.Ioc;

import java.io.IOException;

public class Blob
{
    final private Ioc ioc;

    public Blob(Ioc ioc, byte[] bytes, int keyIterationCount)
    {
        this.ioc = ioc;
        this.bytes = bytes;
        this.keyIterationCount = keyIterationCount;
    }

    public byte[] makeEncryptionKey(String username, String password) {
        return new FetcherHelper(ioc).makeKey(username, password, keyIterationCount);
    }

    private final byte[] bytes;
    public byte[] getBytes()
    {
        return bytes;
    }

    private final int keyIterationCount;
    public int getKeyIterationCount() {
        return keyIterationCount;
    }
}
