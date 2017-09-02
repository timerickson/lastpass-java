package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.shims.WebClient;

import org.mockito.stubbing.OngoingStubbing;

import java.io.UnsupportedEncodingException;

class FetcherTest
{
    //
    // Data shared between Login and Fetcher tests
    //

    static final int IterationCount = 5000;
    static final String SessionId = "53ru,Hb713QnEVM5zWZ16jMvxS0";

    static final String WebExceptionMessage = "WebException occured";

    static class ResponseOrException
    {
        public ResponseOrException(String response)
        {
            this(response, null);
        }

        public ResponseOrException(Exception exception)
        {
            this((byte[])null, exception);
        }

        public ResponseOrException(String response, Exception exception) {
            try {
                _response = response.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding error", e);
            }
            _exception = exception;
        }

        public ResponseOrException(byte[] response, Exception exception) {
            _response = response;
            _exception = exception;
        }

        public OngoingStubbing<byte[]> returnOrThrow(OngoingStubbing<byte[]> setup)
        {
            if (_exception != null)
                return setup.thenThrow(_exception);
            else
                return setup.thenReturn(_response);
        }

//        public void returnOrThrow(ISetupSequentialResult<byte[]> setup)
//        {
//            if (_exception != null)
//                setup.Throws(_exception);
//            else
//                setup.Returns(_response);
//        }

        private final byte[] _response;
        private final Exception _exception;
    }
}
