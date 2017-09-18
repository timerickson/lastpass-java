package com.in2bits;

import com.in2bits.adapters.Base64Decoder;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by Tim on 7/24/17.
 */

public class TestBase64Decoder implements Base64Decoder {
    @Override
    public byte[] decode(String s) {
        return DatatypeConverter.parseBase64Binary(s);
    }
    public String encode(byte[] b) { return DatatypeConverter.printBase64Binary(b); }
}
