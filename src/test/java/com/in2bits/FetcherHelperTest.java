package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Ioc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FetcherHelperTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FetcherHelper fetcherHelper;
    private Base64Decoder decoder = new TestBase64Decoder();

    @Before
    public void before() {
        Ioc ioc = new SimpleIoc();
        fetcherHelper = new FetcherHelper(ioc);
    }

    @Test
    public void MakeKey()
    {
        Map<Integer, String> testCases = new HashMap<>();
        testCases.put(1, "C/Bh2SGWxI8JDu54DbbpV8J9wa6pKbesIb9MAXkeF3Y=");
        testCases.put(5, "pE9goazSCRqnWwcixWM4NHJjWMvB5T15dMhe6ug1pZg=");
        testCases.put(10, "n9S0SyJdrMegeBHtkxUx8Lzc7wI6aGl+y3/udGmVey8=");
        testCases.put(50, "GwI8/kNy1NjIfe3Z0VAZfF78938UVuCi6xAL3MJBux0=");
        testCases.put(100, "piGdSULeHMWiBS3QJNM46M5PIYwQXA6cNS10pLB3Xf8=");
        testCases.put(500, "OfOUvVnQzB4v49sNh4+PdwIFb9Fr5+jVfWRTf+E2Ghg=");
        testCases.put(1000, "z7CdwlIkbu0XvcB7oQIpnlqwNGemdrGTBmDKnL9taPg=");

        for (Map.Entry<Integer, String> i : testCases.entrySet())
        {
            byte[] result = fetcherHelper.makeKey(Username, Password, i.getKey());
            assertEquals(Extensions.Strings.decode64(decoder, i.getValue()), result);
        }
    }

    @Test
    public void MakeKey_throws_on_zero_iterationCount()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Iteration count should be positive\r\nParameter name: iterationCount");
        fetcherHelper.makeKey(Username, Password, 0);
    }

    @Test
    public void MakeKey_throws_on_negative_iterationCount()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Iteration count should be positive\r\nParameter name: iterationCount");
        fetcherHelper.makeKey(Username, Password, -1);
    }

    @Test
    public void MakeHash()
    {
        Map<Integer, String> testCases = new HashMap<>();
        testCases.put(1, "a1943cfbb75e37b129bbf78b9baeab4ae6dd08225776397f66b8e0c7a913a055");
        testCases.put(5, "a95849e029a7791cfc4503eed9ec96ab8675c4a7c4e82b00553ddd179b3d8445");
        testCases.put(10, "0da0b44f5e6b7306f14e92de6d629446370d05afeb1dc07cfcbe25f169170c16");
        testCases.put(50, "1d5bc0d636da4ad469cefe56c42c2ff71589facb9c83f08fcf7711a7891cc159");
        testCases.put(100, "82fc12024acb618878ba231a9948c49c6f46e30b5a09c11d87f6d3338babacb5");
        testCases.put(500, "3139861ae962801b59fc41ff7eeb11f84ca56d810ab490f0d8c89d9d9ab07aa6");
        testCases.put(1000, "03161354566c396fcd624a424164160e890e96b4b5fa6d942fc6377ab613513b");

        for (Map.Entry<Integer, String> i : testCases.entrySet())
        {
            String result = fetcherHelper.makeHash(Username, Password, i.getKey());
            assertEquals(i.getValue(), result);
        }
    }

    @Test
    public void MakeHash_throws_on_zero_iterationCount()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Iteration count should be positive\r\nParameter name: iterationCount");
        fetcherHelper.makeHash(Username, Password, 0);
    }

    @Test
    public void MakeHash_throws_on_negative_iterationCount()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Iteration count should be positive\r\nParameter name: iterationCount");
        fetcherHelper.makeHash(Username, Password, -1);
    }

    private static final String Username = "postlass@gmail.com";
    private static final String Password = "pl1234567890";
}
