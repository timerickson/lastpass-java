package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.shims.Func1;
import com.in2bits.shims.KeyValuePair;

import java.io.DataInputStream;
import java.io.IOException;

// Very-very basic ASN.1 parser. Just enough to extract the RSA key
// parameters stored in a vault. Supports only sequences, octet strings
// and numbers. Error handling is minimal too.
class Asn1
{
    public enum Kind
    {
        Integer,
        Bytes,
        Null,
        Sequence,
    }

    private final ParserHelper parserHelper;

    public Asn1(ParserHelper parserHelper) {
        this.parserHelper = parserHelper;
    }

    public KeyValuePair<Kind, byte[]> parseItem(byte[] bytes) {
        return parserHelper.WithBytes(bytes, new Func1<DataInputStream, KeyValuePair<Kind, byte[]>>() {
            @Override
            public KeyValuePair<Kind, byte[]> execute(DataInputStream reader) {
                try {
                    return ExtractItem(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                };
                return null;
            }
        });
    }

    public KeyValuePair<Kind, byte[]> ExtractItem(DataInputStream reader) throws IOException {
        byte id = reader.readByte();
        int tag = id & 0x1F;

        Kind kind;
        switch (tag)
        {
            case 2:
                kind = Kind.Integer;
                break;
            case 4:
                kind = Kind.Bytes;
                break;
            case 5:
                kind = Kind.Null;
                break;
            case 16:
                kind = Kind.Sequence;
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown ASN.1 tag %s", tag));
        }

        int size = reader.readByte();
        if ((size & 0x80) != 0)
        {
            int sizeLength = size & 0x7F;
            size = 0;
            for (int i = 0; i < sizeLength; ++i)
            {
                int oneByte = reader.readByte();
                if (oneByte < 0) {
                    oneByte *= -1;
                }
                size = size * 256 + oneByte;
            }
        }

        byte[] payload = new byte[size];
        reader.read(payload);

        return new KeyValuePair<Kind, byte[]>(kind, payload);
    }
}

