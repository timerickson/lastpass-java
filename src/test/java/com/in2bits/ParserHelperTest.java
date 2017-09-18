package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Action1;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.RSAParameters;
import com.in2bits.shims.Ref;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.in;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParserHelperTest
{
    private ParserHelper parserHelper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        Ioc ioc = new SimpleIoc();
        parserHelper = new ParserHelper(ioc);
    }

    @Test
    public void ParseAccount_returns_account()
    {
        WithBlob(new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                List<ParserHelper.Chunk> chunks = parserHelper.ExtractChunks(reader);
                for (int i = 0; i < chunks.size(); i++) {
                    ParserHelper.Chunk chunk = chunks.get(i);
                    if (chunk.getId().equals("ACCT")) {
                        Account account = parserHelper.Parse_ACCT(chunks.get(i), TestData.EncryptionKey);
                        assertTrue(account.getUrl().startsWith(TestData.Accounts[i].Url));
                    }
                }
            }
        });
    }

    @Test
    public void Parse_PRIK_returns_private_key()
    {
        ParserHelper.Chunk chunk = new ParserHelper.Chunk("PRIK", TestData.Chunk_PRIK);
        RSAParameters rsa = parserHelper.Parse_PRIK(chunk, TestData.EncryptionKey);

        assertEquals(TestData.RsaD, rsa.getD());
        assertEquals(TestData.RsaDP, rsa.getDp());
        assertEquals(TestData.RsaDQ, rsa.getDq());
        assertEquals(TestData.RsaExponent, rsa.getExponent());
        assertEquals(TestData.RsaInverseQ, rsa.getInverseQ());
        assertEquals(TestData.RsaModulus, rsa.getModulus());
        assertEquals(TestData.RsaP, rsa.getP());
        assertEquals(TestData.RsaQ, rsa.getQ());
    }

    @Test
    public void Parse_SHAR_returns_folder_key_when_aes_encrypted()
    {
        String id = "id";
        String name = "name";
        byte[] key = "0123456789012345".getBytes();

        byte[][] items = new byte[][] {
            MakeItem(id),
            MakeItem("rsa"),
            MakeItem(Encode64(EncryptAes256(name, key))),
            MakeItem("skipped"),
            MakeItem("skipped"),
            MakeItem(EncryptAes256(Extensions.Bytes.toHex(key), TestData.EncryptionKey)),
        };

        SharedFolder folder = parserHelper.Parse_SHAR(MakeChunk("SHAR", items),
                                             TestData.EncryptionKey,
                                             new RSAParameters());

        assertEquals(id, folder.getId());
        assertEquals(name, folder.getName());
        assertEquals(key, folder.getEncryptionKey());
    }

    @Test
    public void Parse_PRIK_throws_on_invalid_chunk()
    {
        ParserHelper.Chunk chunk = new ParserHelper.Chunk("PRIK", "".getBytes());
        thrown.expect(ParseException.class);
        thrown.expect(new BaseMatcher<ParseException>(){
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                ParseException parseException = (ParseException)item;
                return ParseException.FailureReason.CorruptedBlob.equals(parseException.getReason());
            }
        });
        thrown.expectMessage("Failed to decrypt private key");
        parserHelper.Parse_PRIK(chunk, TestData.EncryptionKey);
    }

    @Test
    public void ParseSecureNoteServer_parses_all_parameters()
    {
        Ref<String> typeRef = new Ref<>("");
        Ref<String> urlRef = new Ref<>("");
        Ref<String> usernameRef = new Ref<>("");
        Ref<String> passwordRef = new Ref<>("");
        parserHelper.ParseSecureNoteServer("NoteType:type\nHostname:url\nUsername:username\nPassword:password",
                                           typeRef,
                                           urlRef,
                                           usernameRef,
                                           passwordRef);

        assertEquals("type", typeRef.getValue());
        assertEquals("url", urlRef.getValue());
        assertEquals("username", usernameRef.getValue());
        assertEquals("password", passwordRef.getValue());
    }

    @Test
    public void ParseSecureNoteServer_handles_extra_colons()
    {
        Ref<String> typeRef = new Ref<>("");
        Ref<String> urlRef = new Ref<>("");
        Ref<String> usernameRef = new Ref<>("");
        Ref<String> passwordRef = new Ref<>("");
        parserHelper.ParseSecureNoteServer("NoteType:type:type\nHostname:url:url\nUsername:username:username\nPassword:password:password",
                typeRef,
                urlRef,
                usernameRef,
                passwordRef);

        assertEquals("type:type", typeRef.getValue());
        assertEquals("url:url", urlRef.getValue());
        assertEquals("username:username", usernameRef.getValue());
        assertEquals("password:password", passwordRef.getValue());
    }

    @Test
    public void ParseSecureNoteServer_skips_invalid_lines()
    {
        Ref<String> typeRef = new Ref<>("");
        Ref<String> urlRef = new Ref<>("");
        Ref<String> usernameRef = new Ref<>("");
        Ref<String> passwordRef = new Ref<>("");
        parserHelper.ParseSecureNoteServer("Something:Else\nHostname\nUsername:\n:\n::\n\n",
                typeRef,
                urlRef,
                usernameRef,
                passwordRef);

        assertEquals("", typeRef.getValue());
        assertEquals("", urlRef.getValue());
        assertEquals("", usernameRef.getValue());
        assertEquals("", passwordRef.getValue());
    }

    @Test
    public void ParseSecureNoteServer_does_not_modify_missing_parameters()
    {
        Ref<String> typeRef = new Ref<>("type");
        Ref<String> urlRef = new Ref<>("url");
        Ref<String> usernameRef = new Ref<>("username");
        Ref<String> passwordRef = new Ref<>("password");
        parserHelper.ParseSecureNoteServer("", typeRef, urlRef, usernameRef, passwordRef);

        assertEquals("type", typeRef.getValue());
        assertEquals("url", urlRef.getValue());
        assertEquals("username", usernameRef.getValue());
        assertEquals("password", passwordRef.getValue());
    }

    @Test
    public void ReadChunk_returns_first_chunk()
    {
        WithBlob(new Action1<DataInputStream>(){
            @Override
            public void execute(DataInputStream reader) throws IOException {
                ParserHelper.Chunk chunk = parserHelper.ReadChunk(reader);
                assertEquals("LPAV", chunk.getId());
                assertEquals(3, chunk.getPayload().length);
                assertEquals(11, TestData.Blob.length - reader.available());
            }
        });
    }

    @Test
    public void ReadChunk_reads_all_chunks()
    {
        WithBlob(new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                while (reader.available() > 0)
                    parserHelper.ReadChunk(reader);

                assertEquals(0, reader.available());
            }
        });
    }

    @Test
    public void ExtractChunks_returns_all_chunks()
    {
        WithBlob(new Action1<DataInputStream>() {
            @Override
                    public void execute(DataInputStream reader)
            {
                List<ParserHelper.Chunk> chunks = parserHelper.ExtractChunks(reader);
                List<String> ids = new ArrayList<>();
                for (ParserHelper.Chunk chunk : chunks) {
                    String id = chunk.getId();
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
                assertArrayEquals(TestData.ChunkIds, ids.toArray(new String[0]));
            }
        });
    }

    @Test
    public void ReadItem_returns_first_item()
    {
        WithBlob(new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) {
                List<ParserHelper.Chunk> chunks = parserHelper.ExtractChunks(reader);

                ParserHelper.Chunk account = null;
                for (ParserHelper.Chunk chunk : chunks) {
                    if (chunk.getId().equals("ACCT")) {
                        account = chunk;
                        break;
                    }
                }
                assertNotNull(account);

                parserHelper.WithBytes(account.getPayload(), new Action1<DataInputStream>() {
                    @Override
                    public void execute(DataInputStream chunkReader) throws IOException {
                        byte[] item = parserHelper.ReadItem(chunkReader);
                        assertNotNull(item);
                    }
                });
            }
        });
    }

    @Test
    public void SkipItem_skips_empty_item()
    {
        WithHex("00000000", new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                parserHelper.SkipItem(reader);
                assertEquals(TestData.Blob.length - 4, reader.available());
            }
        });
    }

    @Test
    public void SkipItem_skips_non_empty_item()
    {
        WithHex("00000004DEADBEEF", new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                parserHelper.SkipItem(reader);
                assertEquals(TestData.Blob.length - 8, reader.available());
            }
        });
    }

    @Test
    public void ReadId_returns_id()
    {
        final String expectedId = "ABCD";
        parserHelper.WithBytes(expectedId.getBytes(), new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                String id = parserHelper.ReadId(reader);
                assertEquals(expectedId, id);
                assertEquals(TestData.Blob.length - 4, reader.available());
            }
        });
    }

    @Test
    public void ReadSize_returns_size()
    {
        WithHex("DEADBEEF", new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                int size = parserHelper.ReadSize(reader);
                assertEquals(0xDEADBEEF, size);
                assertEquals(TestData.Blob.length - 4, reader.available());
            }
        });
    }

    @Test
    public void ReadPayload_returns_payload()
    {
        final byte[] expectedPayload = Extensions.Strings.decodeHex("FEEDDEADBEEF");
        final int size = expectedPayload.length;
        parserHelper.WithBytes(expectedPayload, new Action1<DataInputStream>() {
            @Override
            public void execute(DataInputStream reader) throws IOException {
                byte[] payload = parserHelper.ReadPayload(reader, size);
                assertEquals(expectedPayload, payload);
                assertEquals(size, TestData.Blob.length - reader.available());
            }
        });
    }

    @Test
    public void DecryptAes256Plain_with_default_value()
    {
        String defVal = "ohai!";
        String plaintext = parserHelper.DecryptAes256Plain("not a valid ciphertext".getBytes(),
                                                        _encryptionKey,
                                                        defVal);
        assertEquals(defVal, plaintext);
    }

    @Test
    public void DecryptAes256Base64_with_default_value()
    {
        String defVal = "ohai!";
        String plaintext = parserHelper.DecryptAes256Base64("bm90IGEgdmFsaWQgY2lwaGVydGV4dA==".getBytes(),
                                                         _encryptionKey,
                                                         defVal);
        assertEquals(defVal, plaintext);
    }

    @Test
    public void DecryptAes256Plain()
    {
        String[][] tests = new String[][] {
            {"", ""},
            {"All your base are belong to us", "BNhd3Q3ZVODxk9c0C788NUPTIfYnZuxXfkghtMJ8jVM="},
            {"All your base are belong to us", "IcokDWmjOkKtLpZehWKL6666Uj6fNXPpX6lLWlou+1Lrwb+D3ymP6BAwd6C0TB3hSA=="}
        };

        Base64Decoder decoder = new TestBase64Decoder();
        for (int i = 0; i < tests.length; ++i)
            assertEquals(tests[i][0], parserHelper.DecryptAes256Plain(Extensions.Strings.decode64(decoder, tests[i][1]), _encryptionKey));
    }

    @Test
    public void DecryptAes256Base64() throws UnsupportedEncodingException {
        String[][] tests = new String[][] {
            {"", ""},
            {"All your base are belong to us", "BNhd3Q3ZVODxk9c0C788NUPTIfYnZuxXfkghtMJ8jVM="},
            {"All your base are belong to us", "!YFuiAVZgOD2K+s6y8yaMOw==|TZ1+if9ofqRKTatyUaOnfudletslMJ/RZyUwJuR/+aI="}
        };

        for (int i = 0; i < tests.length; ++i)
            assertEquals(tests[i][0], parserHelper.DecryptAes256Base64(tests[i][1].getBytes(), _encryptionKey));
    }

    @Test
    public void DecryptAes256EcbPlain()
    {
        Map<String, String> tests = new HashMap<>();
        tests.put("", "");
        tests.put("0123456789", "8mHxIA8rul6eq72a/Gq2iw==");
        tests.put("All your base are belong to us", "BNhd3Q3ZVODxk9c0C788NUPTIfYnZuxXfkghtMJ8jVM=");

        Base64Decoder decoder = new TestBase64Decoder();
        for (Map.Entry<String, String> i : tests.entrySet())
            assertEquals(i.getKey(), parserHelper.DecryptAes256EcbPlain(Extensions.Strings.decode64(decoder, i.getValue()), _encryptionKey));
    }

    @Test
    public void DecryptAes256EcbBase64() throws UnsupportedEncodingException {
        Map<String, String> tests = new HashMap<>();
        tests.put("", "");
        tests.put("0123456789", "8mHxIA8rul6eq72a/Gq2iw==");
        tests.put("All your base are belong to us", "BNhd3Q3ZVODxk9c0C788NUPTIfYnZuxXfkghtMJ8jVM=");

        for (Map.Entry<String, String> i : tests.entrySet())
            assertEquals(i.getKey(), parserHelper.DecryptAes256EcbBase64(i.getValue().getBytes(), _encryptionKey));
    }

    @Test
    public void DecryptAes256CbcPlain()
    {
        Map<String, String> tests = new HashMap<String, String>();
        tests.put("", "");
        tests.put("0123456789", "IQ+hiIy0vGG4srsHmXChe3ehWc/rYPnfiyqOG8h78DdX");
        tests.put("All your base are belong to us", "IcokDWmjOkKtLpZehWKL6666Uj6fNXPpX6lLWlou+1Lrwb+D3ymP6BAwd6C0TB3hSA==");

        Base64Decoder decoder = new TestBase64Decoder();
        for (Map.Entry<String, String> i : tests.entrySet())
            assertEquals(i.getKey(), parserHelper.DecryptAes256CbcPlain(Extensions.Strings.decode64(decoder, i.getValue()), _encryptionKey));
    }

    @Test
    public void DecryptAes256CbcBase64() throws UnsupportedEncodingException {
        Map<String, String> tests = new HashMap<>();
        tests.put("", "");
        tests.put("0123456789", "!6TZb9bbrqpocMaNgFjrhjw==|f7RcJ7UowesqGk+um+P5ug==");
        tests.put("All your base are belong to us", "!YFuiAVZgOD2K+s6y8yaMOw==|TZ1+if9ofqRKTatyUaOnfudletslMJ/RZyUwJuR/+aI=");

        for (Map.Entry<String, String> i : tests.entrySet())
            assertEquals(i.getKey(), parserHelper.DecryptAes256CbcBase64(i.getValue().getBytes(), _encryptionKey));
    }

    //
    // Helpers
    //

    private void WithBlob(Action1<DataInputStream> action)
    {
        parserHelper.WithBytes(TestData.Blob, action);
    }

    private void WithHex(String hex, Action1<DataInputStream> action)
    {
        parserHelper.WithBytes(Extensions.Strings.decodeHex(hex), action);
    }

    private static byte[] MakeItem(String payload)
                                              {
                                                 return MakeItem(payload.getBytes());
                                                                                    }

    private static byte[] MakeItem(byte[] payload)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(payload.length);
        byte[] sizeBits = byteBuffer.array();
        return Arrays.concatenate(sizeBits, payload);
    }

    private static ParserHelper.Chunk MakeChunk(String id, byte[][] items)
    {
        byte[] chained = items[0];
        for (int i = 1; i < items.length; i++) {
            chained = Arrays.concatenate(chained, items[i]);
        }
        return new ParserHelper.Chunk(id, chained);
    }

    private static String Encode64(byte[] data)
                                           {
                                              return new TestBase64Decoder().encode(data);
                                                                                  }

    private static byte[] EncryptAes256(String data, byte[] encryptionKey)
    {
        return EncryptAes256(data.getBytes(), encryptionKey);
    }

    private static byte[] EncryptAes256(byte[] data, byte[] encryptionKey)
    {
        try {
            KeyParameter keyParam = new KeyParameter(encryptionKey);
            BlockCipherPadding padding = new PKCS7Padding();
            BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                    new CBCBlockCipher(new AESEngine()), padding);
            cipher.reset();
            cipher.init(true, keyParam);
            byte[] buffer = new byte[cipher.getOutputSize(data.length)];
            int len = cipher.processBytes(data, 0, data.length, buffer, 0);
            len += cipher.doFinal(buffer, len);
            return Arrays.copyOfRange(buffer, 0, len);
        } catch (Exception e) {
            throw new RuntimeException("decrypt error in SimpleAesManaged", e);
        }
    }

    private static final byte[] _encryptionKey;
    static {
        Base64Decoder decoder = new TestBase64Decoder();
        _encryptionKey = Extensions.Strings.decode64(decoder, "OfOUvVnQzB4v49sNh4+PdwIFb9Fr5+jVfWRTf+E2Ghg=");
    }
}
