package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountTest
{
    @Test
    public void EncryptedAccount_properties_are_set()
    {
        String id = "1234567890";
        String name = "name";
        String username = "username";
        String password = "password";
        String url = "url";
        String group = "group";

        Account account = new Account(id, name, username, password, url, group);
        assertEquals(id, account.getId());
        assertEquals(name, account.getName());
        assertEquals(username, account.getUsername());
        assertEquals(password, account.getPassword());
        assertEquals(url, account.getUrl());
        assertEquals(group, account.getGroup());
    }
}
