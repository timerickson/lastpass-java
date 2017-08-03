package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.shims.Action;

import java.io.UnsupportedEncodingException;

abstract class Extensions
{
    public static class Bytes {
        public static String toUtf8(byte[] x)
        {
            try {
                return new String(x, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Extensions.Bytes.toUtf8", e);
            }
        }

        private final static char[] hexArray = "0123456789abcdef".toCharArray();
        public static String toHex(byte[] x)
        {
            char[] hexChars = new char[x.length * 2];
            for ( int j = 0; j < x.length; j++ ) {
                int v = x[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }

    public static class Strings {
        public static byte[] toBytes(String s) throws UnsupportedEncodingException
        {
            return s.getBytes("UTF-8");
        }

        public static byte[] decodeHex(String s)
        {
            if (s.length() % 2 != 0)
                throw new IllegalArgumentException("Input length must be multple of 2");

            byte[] bytes = new byte[s.length() / 2];
            for (int i = 0; i < s.length() / 2; ++i)
            {
                byte b = 0;
                for (int j = 0; j < 2; ++j)
                {
                    b <<= 4;
                    char c = Character.toLowerCase(s.charAt(i * 2 + j));
                    if (c >= '0' && c <= '9')
                        b |= c - '0';
                    else if (c >= 'a' && c <= 'f')
                        b |= c - 'a' + 10;
                    else
                        throw new IllegalArgumentException("Input contains invalid characters");
                }

                bytes[i] = (byte)b;
            }

            return bytes;
        }

        public static byte[] decode64(Base64Decoder decoder, String s)
        {
            return decoder.decode(s);
        }
    }

    public static class Integers {
        public static void times(int times, Action action)
        {
            for (int i = 0; i < times; ++i)
                action.execute();
        }
    }
}

