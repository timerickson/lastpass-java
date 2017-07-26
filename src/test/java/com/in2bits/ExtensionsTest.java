package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.shims.Action;
import com.in2bits.shims.Ref;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class ExtensionsTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void Reverse()
    {
        assertEquals(0L, Extensions.Uint.reverse(0));
        assertEquals(0xffL, Extensions.Uint.reverse(0xff000000));
        assertEquals(0xff000000L, Extensions.Uint.reverse(0xff));
        assertEquals(0xff00ff00L, Extensions.Uint.reverse(0xff00ff));
        assertEquals(0x78563412L, Extensions.Uint.reverse(0x12345678));
        assertEquals(0xdeadbeefL, Extensions.Uint.reverse(0xefbeadde));
    }

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes, 0, Long.SIZE);
        return buffer.getLong();
    }

    @Test
    public void FromBigEndian()
    {
        assertEquals(0L, Extensions.Uint.fromBigEndian(bytesToLong(new byte[] {0x00, 0x00, 0x00, 0x00})));
        assertEquals(0x12345678L, Extensions.Uint.fromBigEndian(bytesToLong(new byte[] {0x12, 0x34, 0x56, 0x78})));
        assertEquals(0xdeadbeefL, Extensions.Uint.fromBigEndian(bytesToLong(new byte[] {(byte) 0xDE, (byte) 0xad, (byte) 0xbe, (byte) 0xef})));
    }

    @Test
    public void ToUtf8()
    {
        assertEquals("", Extensions.Bytes.toUtf8(new byte[] {}));
        assertEquals(_helloUtf8, Extensions.Bytes.toUtf8(_helloUtf8Bytes));
    }

    @Test
    public void ToHex()
    {
        for (Map.Entry<String, byte[]> i : _hexToBytes.entrySet()) {
            assertEquals(i.getKey(), Extensions.Bytes.toHex(i.getValue()));
        }
    }

    @Test
    public void ToBytes() throws Exception
    {
        assertEquals(new byte[] {}, Extensions.Strings.toBytes(""));
        assertEquals(_helloUtf8Bytes, Extensions.Strings.toBytes(_helloUtf8));
    }

    @Test
    public void DecodeHex()
    {
        for (Map.Entry<String, byte[]> i : _hexToBytes.entrySet())
        {
            assertEquals(i.getValue(), Extensions.Strings.decodeHex(i.getKey()));
            assertEquals(i.getValue(), Extensions.Strings.decodeHex(i.getKey().toUpperCase()));
        }
    }

    @Test
    public void DecodeHex_throws_on_odd_length()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input length must be multple of 2");
        Extensions.Strings.decodeHex("0");
    }

    @Test
    public void DecodeHex_throws_on_non_hex_characters()
    {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input contains invalid characters");
        Extensions.Strings.decodeHex("xz");
    }

    @Test
    public void Decode64()
    {
        Base64Decoder decoder = new TestBase64Decoder();
        assertEquals(new byte[] {}, Extensions.Strings.decode64(decoder, ""));
        assertEquals(new byte[] {0x61}, Extensions.Strings.decode64(decoder, "YQ=="));
        assertEquals(new byte[] {0x61, 0x62}, Extensions.Strings.decode64(decoder, "YWI="));
        assertEquals(new byte[] {0x61, 0x62, 0x63}, Extensions.Strings.decode64(decoder, "YWJj"));
        assertEquals(new byte[] {0x61, 0x62, 0x63, 0x64}, Extensions.Strings.decode64(decoder, "YWJjZA=="));
    }

    @Test
    public void Times()
    {
        int[] times = new int[] {0, 1, 2, 5, 10};
        for (Integer i : times)
        {
            final Ref<Integer> calledRef = new Ref<>();
            calledRef.setValue(0);
            Extensions.Integers.times(i, new Action(){
                @Override
                public void execute() {
            calledRef.setValue(calledRef.getValue()+1);}});
            assertEquals(i, calledRef.getValue());
        }
    }

    private final String _helloUtf8 = "Hello, UTF-8!";
    private final byte[] _helloUtf8Bytes = new byte[] {
        0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x55, 0x54, 0x46, 0x2D, 0x38, 0x21
};

    private final static Map<String, byte[]> _hexToBytes = new HashMap<String, byte[]>();
    static {
        _hexToBytes.put("",
                new byte[] {});

        _hexToBytes.put("00",
                new byte[] {0});

        _hexToBytes.put("00ff",
                new byte[] {0, (byte)255});

        _hexToBytes.put("00010203040506070809",
                new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        _hexToBytes.put("000102030405060708090a0b0c0d0e0f",
                new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

        _hexToBytes.put("8af633933e96a3c3550c2734bd814195",
                new byte[] {(byte) 0x8A, (byte) 0xF6, 0x33, (byte) 0x93, 0x3E, (byte) 0x96, (byte) 0xA3, (byte) 0xC3, 0x55, 0x0C, 0x27, 0x34, (byte) 0xBD, (byte) 0x81, 0x41, (byte) 0x95});
    }
}
