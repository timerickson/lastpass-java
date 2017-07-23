package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

public class ParseException extends BaseException
{
    public enum FailureReason
    {
        CorruptedBlob
    }

    public ParseException(FailureReason reason, String message)
    {
        this(reason, message, null);
    }

    public ParseException(FailureReason reason, String message, Exception innerException)
    {
        super(message, innerException);
        this.reason = reason;
    }

    private final FailureReason reason;
    public FailureReason getReason() {
        return reason;
    }
}

