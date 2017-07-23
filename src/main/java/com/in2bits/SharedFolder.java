package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

public class SharedFolder
{
    public SharedFolder(String id, String name, byte[] encryptionKey)
    {
        this.id = id;
        this.name = name;
        this.encryptionKey = encryptionKey;
    }

    final private String id;
    public String getId() {
        return id;
    }

    final private String name;
    public String getName() {
        return name;
    }

    final private byte[] encryptionKey;
    public byte[] getEncryptionKey() {
        return encryptionKey;
    }
}

