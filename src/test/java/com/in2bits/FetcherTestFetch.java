package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.Ref;
import com.in2bits.shims.WebClient;
import com.in2bits.shims.WebException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetcherTestFetch extends FetcherTest
{
    //
    // Shared data
    //

    private final static String AccountDownloadUrl = "https://lastpass.com/getaccts.php?mobile=1&b64=1&hash=0.0&hasplugin=3.0.23&requestsrc=android";
    private static final Session Session = new Session(SessionId, IterationCount);
    private final static String FetchResponse = "VGVzdCBibG9i";
    private static final byte[] Blob = "Test blob".getBytes();

    private Fetcher fetcher;

    @Before
    public void before() {
        Ioc ioc = new SimpleIoc();
        fetcher = new Fetcher(ioc);
    }

    //
    // Fetch tests
    //

        @Test
    public void Fetch_sets_session_id_cookie() throws UnsupportedEncodingException,
                WebException, FetchException {
        Map<String, String> headers = new HashMap<>();
        SuccessfullyFetch(headers);

        assertEquals(String.format("PHPSESSID=%s", URLEncoder.encode(SessionId, "UTF-8")), headers.get("Cookie"));
    }

        @Test
    public void Fetch_requests_accounts_from_correct_url() throws WebException, UnsupportedEncodingException, FetchException {
        WebClient webClient = SuccessfullyFetch();
        verify(webClient).downloadData(eq(AccountDownloadUrl));
    }

        @Test
    public void Fetch_returns_blob() throws WebException, UnsupportedEncodingException, FetchException {
        Ref<Blob> blobRef = new Ref<>();
        SuccessfullyFetch(blobRef);

        assertEquals(Blob, blobRef.getValue().getBytes());
        assertEquals(IterationCount, blobRef.getValue().getKeyIterationCount());
    }

        @Test
    public void Fetch_throws_on_WebException() throws WebException {
        FetchAndVerifyException(new ResponseOrException(new WebException(null, null)),
            WebException.class, FetchException.FailureReason.WebException,
            WebExceptionMessage);
    }

        @Test
    public void Fetch_throws_on_invalid_response() throws WebException {
        FetchAndVerifyException(new ResponseOrException("Invalid base64 String!"),
            IllegalArgumentException.class, FetchException.FailureReason.InvalidResponse,
            "Invalid base64 in response");
    }

    //
    // Helpers
    //

    private WebClient SetupFetch(ResponseOrException responseOrException) throws WebException {
        return SetupFetch(responseOrException, null);
    }

    private WebClient SetupFetch(ResponseOrException responseOrException,
                                               Map<String, String> headers) throws WebException {
        WebClient webClient = mock(WebClient.class);

        when(webClient.getHeaders()).thenReturn(headers == null ?
            new HashMap<String, String>() :
            headers);

        responseOrException.returnOrThrow(when(webClient.downloadData(anyString())));

        return webClient;
    }

    private WebClient SuccessfullyFetch()
            throws WebException, UnsupportedEncodingException, FetchException {
        return SuccessfullyFetch((Map<String, String>)null);
    }

    private WebClient SuccessfullyFetch(Map<String, String> headers)
            throws WebException, UnsupportedEncodingException, FetchException {
        Ref<Blob> blobRef = new Ref<>();
        return SuccessfullyFetch(blobRef, headers);
    }

    private WebClient SuccessfullyFetch(Ref<Blob> blobRef)
            throws WebException, UnsupportedEncodingException, FetchException {
        return SuccessfullyFetch(blobRef, null);
    }

    private WebClient SuccessfullyFetch(Ref<Blob> blobRef, Map<String, String> headers)
            throws WebException, UnsupportedEncodingException, FetchException {
        WebClient webClient = SetupFetch(new ResponseOrException(FetchResponse), headers);
        blobRef.setValue(fetcher.Fetch(Session, webClient));
        return webClient;
    }

    private void FetchAndVerifyException(ResponseOrException responseOrException,
    Class<?> exceptionType, FetchException.FailureReason reason,
    String message) throws WebException
    {
        WebClient webClient = SetupFetch(responseOrException);
        Exception exception = null;
        try {
            fetcher.Fetch(Session, webClient);
        } catch (Exception ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertEquals(FetchException.class, exception.getClass());

        FetchException fetchException = (FetchException)exception;

        assertEquals(reason, fetchException.getReason());
        assertEquals(message, fetchException.getMessage());
        assertEquals(exceptionType, fetchException.getCause().getClass());
    }
}
