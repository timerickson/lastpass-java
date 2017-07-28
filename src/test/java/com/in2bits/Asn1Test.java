package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.KeyValuePair;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Asn1Test
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Asn1 asn1;

    @Before
    public void before() {
        Ioc ioc = new SimpleIoc();
        ParserHelper parserHelper = new ParserHelper(ioc);
        asn1 = new Asn1(parserHelper);
    }

    @Test
    public void Asn1_ParseItem_returns_integer()
    {
        ParseDeadBeefItem((byte) 2, Asn1.Kind.Integer);
    }

    @Test
    public void Asn1_ParseItem_returns_bytes()
    {
        ParseDeadBeefItem((byte) 4, Asn1.Kind.Bytes);
    }

    @Test
    public void Asn1_ParseItem_returns_null()
    {
        ParseDeadBeefItem((byte) 5, Asn1.Kind.Null);
    }

    @Test
    public void Asn1_ParseItem_returns_squence()
    {
        ParseDeadBeefItem((byte) 16, Asn1.Kind.Sequence);
    }

    @Test
    public void Asn1_ParseItem_throws_on_invalid_tag() throws IllegalArgumentException
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unknown ASN.1 tag 13");
        asn1.parseItem(Extensions.Strings.decodeHex("0D04DEADBEEF"));
    }

    @Test
    public void Asn1_ParseItem_reads_packed_size()
    {
        final int size = 127;
        KeyValuePair<Asn1.Kind, byte[]> item = asn1.parseItem(Extensions.Strings.decodeHex("027F" + Repeat("AB", size)));
        assertEquals(size, item.getValue().length);
    }

    @Test
    public void Asn1_ParseItem_reads_single_byte_size()
    {
        final int size = 128;
        KeyValuePair<Asn1.Kind, byte[]> item = asn1.parseItem(Extensions.Strings.decodeHex("028180" + Repeat("AB", size)));
        assertEquals(size, item.getValue().length);
    }

    @Test
    public void Asn1_ParseItem_reads_multi_byte_size()
    {
        final int size = 260;
        KeyValuePair<Asn1.Kind, byte[]> item = asn1.parseItem(Extensions.Strings.decodeHex("02820104" + Repeat("AB", size)));
        assertEquals(size, item.getValue().length);
    }

    private void ParseDeadBeefItem(byte tag, Asn1.Kind kind)
    {
        KeyValuePair<Asn1.Kind, byte[]> item = asn1.parseItem(Extensions.Strings.decodeHex(String.format("%02X04DEADBEEF", tag)));
        assertEquals(kind, item.getKey());
        assertArrayEquals(new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}, item.getValue());
    }

    private static String Repeat(String s, int times)
    {
        // Inefficient! Who cares?!
        String result = "";
        for (int i = 0; i < times; ++i)
            result += s;

        return result;
    }
}

