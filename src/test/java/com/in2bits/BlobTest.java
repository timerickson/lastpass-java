package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Ioc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

class BlobTest
{
    private static final byte[] Bytes = Extensions.Strings.decode64(new TestBase64Decoder(), "TFBBVgAAAAMxMjJQUkVNAAAACjE0MTQ5");
    private final int IterationCount = 500;
    private final String Username = "postlass@gmail.com";
    private final String Password = "pl1234567890";
    private static final byte[] EncryptionKey = Extensions.Strings.decode64(new TestBase64Decoder(), "OfOUvVnQzB4v49sNh4+PdwIFb9Fr5+jVfWRTf+E2Ghg=");

    private Ioc ioc;

    @Before
    public void before() {
        ioc = new SimpleIoc();
    }

    @Test
    public void Blob_properties_are_set()
    {
        Blob blob = new Blob(ioc, Bytes, IterationCount);
        assertEquals(Bytes, blob.getBytes());
        assertEquals(IterationCount, blob.getKeyIterationCount());
    }

    @Test
    public void Blob_MakeEncryptionKey()
    {
        byte[] key = new Blob(ioc, Bytes, IterationCount).makeEncryptionKey(Username, Password);
        assertEquals(EncryptionKey, key);
    }
}
