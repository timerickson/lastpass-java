package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

public class FetchException extends BaseException
{
    public enum FailureReason
    {
        InvalidResponse,
        WebException
    }

    final private FailureReason reason;

    public FetchException(FailureReason reason, String message)
    {
        super(message);
        this.reason = reason;
    }

    public FetchException(FailureReason reason, String message, Exception innerException)

    {
        super(message, innerException);
        this.reason = reason;
    }

    public FailureReason getReason() {
        return reason;
    }
}
