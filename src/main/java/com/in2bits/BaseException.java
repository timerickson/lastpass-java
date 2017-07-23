package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

public class BaseException extends Exception
{
        public BaseException(String message)
        {
                super(message);
        }

        public BaseException(String message, Exception innerException)
        {
                super(message, innerException);
        }
}

