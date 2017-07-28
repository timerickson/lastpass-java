package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ExceptionsTest
{
    private final String _message = "message";
    private final Exception _innerException = new Exception();
    private final FetchException.FailureReason _fetchReason = FetchException.FailureReason.InvalidResponse;
    private final LoginException.FailureReason _loginReason = LoginException.FailureReason.InvalidResponse;
    private final ParseException.FailureReason _parseReason = ParseException.FailureReason.CorruptedBlob;

    @Test
    public void BaseException_with_message()
    {
        BaseException e = new BaseException(_message);
        assertEquals(_message, e.getMessage());
        assertNotNull(e.getCause());
    }

    @Test
    public void BaseException_with_message_and_inner_exception()
    {
        BaseException e = new BaseException(_message, _innerException);
        assertEquals(_message, e.getMessage());
        assertSame(_innerException, e.getCause());
    }

    @Test
    public void FetchException_with_message()
    {
        FetchException e = new FetchException(_fetchReason, _message);
        assertEquals(_message, e.getMessage());
        assertNull(e.getCause());
        assertEquals(_fetchReason, e.getReason());
    }

    @Test
    public void FetchException_with_message_and_inner_exception()
    {
        FetchException e = new FetchException(_fetchReason, _message, _innerException);
        assertEquals(_message, e.getMessage());
        assertSame(_innerException, e.getCause());
        assertEquals(_fetchReason, e.getReason());
    }

    @Test
    public void LoginException_with_message()
    {
        LoginException e = new LoginException(_loginReason, _message);
        assertEquals(_message, e.getMessage());
        assertNull(e.getCause());
        assertEquals(_loginReason, e.getReason());
    }

    @Test
    public void LoginException_with_message_and_inner_exception()
    {
        LoginException e = new LoginException(_loginReason, _message, _innerException);
        assertEquals(_message, e.getMessage());
        assertSame(_innerException, e.getCause());
        assertEquals(_loginReason, e.getReason());
    }

    @Test
    public void ParseException_with_message()
    {
        ParseException e = new ParseException(_parseReason, _message);
        assertEquals(_message, e.getMessage());
        assertNull(e.getCause());
        assertEquals(_parseReason, e.getReason());
    }

    @Test
    public void ParseException_with_message_and_inner_exception()
    {
        ParseException e = new ParseException(_parseReason, _message, _innerException);
        assertEquals(_message, e.getMessage());
        assertSame(_innerException, e.getCause());
        assertEquals(_parseReason, e.getReason());
    }
}
