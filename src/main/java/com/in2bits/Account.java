package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

public class Account
{
    public Account(String id, String name, String username, String password, String url, String group)
    {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.url = url;
        this.group = group;
    }

    final private String id;
    public String getId() {
        return id;
    }

    final private String name;
    public String getName() {
        return name;
    }

    final private String username;
    public String getUsername() {
        return username;
    }

    final private String password;
    public String getPassword() {
        return password;
    }

    final private String url;
    public String getUrl() {
        return url;
    }

    final private String group;
    public String getGroup() {
        return group;
    }
}

