package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.adapters.crypto.AesManaged;
import com.in2bits.adapters.crypto.AesManagedFactory;
import com.in2bits.debug.SimpleRSACryptoServiceProvider;
import com.in2bits.shims.CipherMode;
import com.in2bits.shims.Func;
import com.in2bits.shims.Func2;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.KeyValuePair;
import com.in2bits.adapters.crypto.RSACryptoServiceProvider;
import com.in2bits.shims.RSAParameters;
import com.in2bits.shims.Ref;
import com.in2bits.shims.TAction;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

class ParserHelper
{
    private final Ioc ioc;
    private final Base64Decoder decoder;
    private final Asn1 asn1;

    ParserHelper(Ioc ioc) {
        this.ioc = ioc;
        this.decoder = ioc.get(Base64Decoder.class);
        this.asn1 = new Asn1(this);
    }

    static public class Chunk
    {
        final private String id;
        final private byte[] payload;

        public Chunk(String id, byte[] payload)
        {
            this.id = id;
            this.payload = payload;
        }

        public String getId() {
            return id;
        }

        public byte[] getPayload() {
            return payload;
        }
    }

    // May return null when the chunk does not represent an account.
    // All secure notes are ACCTs but not all of them strore account information.
    //
    // TODO: Add a test for the folder case!
    // TODO: Add a test case that covers secure note account!
    public Account Parse_ACCT(Chunk chunk, final byte[] encryptionKey, final SharedFolder folder)
    {
        assert(chunk.getId().equals("ACCT"));

        return WithBytes(chunk.getPayload(), new Func<DataInputStream, Account>() {
            @Override
            public Account execute(DataInputStream reader) throws IOException {
                String placeholder = "decryption failed";

                String id = Extensions.Bytes.toUtf8(ReadItem(reader));
                String name = DecryptAes256Plain(ReadItem(reader), encryptionKey, placeholder);
                String group = DecryptAes256Plain(ReadItem(reader), encryptionKey, placeholder);
                String url = Extensions.Bytes.toUtf8(Extensions.Strings.decodeHex(Extensions.Bytes.toUtf8(ReadItem(reader))));
                String notes = DecryptAes256Plain(ReadItem(reader), encryptionKey, placeholder);
                SkipItem(reader);
                SkipItem(reader);
                String username = DecryptAes256Plain(ReadItem(reader), encryptionKey, placeholder);
                String password = DecryptAes256Plain(ReadItem(reader), encryptionKey, placeholder);
                SkipItem(reader);
                SkipItem(reader);
                String secureNoteMarker = Extensions.Bytes.toUtf8(ReadItem(reader));

                // Parse secure note
                if (secureNoteMarker.equals("1"))
                {
                    Ref<String> typeRef = new Ref<>("");
                    Ref<String> urlRef = new Ref<>(url);
                    Ref<String> usernameRef = new Ref<>(username);
                    Ref<String> passwordRef = new Ref<>(password);
                    ParseSecureNoteServer(notes, typeRef, urlRef, usernameRef, passwordRef);

                    // Only the some secure notes contain account-like information
                    if (!AllowedSecureNoteTypes.contains(typeRef.getValue()))
                        return null;
                }

                // Override the group name with the shared folder name if any.
                if (folder != null)
                    group = folder.getName();

                return new Account(id, name, username, password, url, group);
            }});
    }

    public RSAParameters Parse_PRIK(Chunk chunk, byte[] encryptionKey) throws ParseException
    {
        assert(chunk.getId() == "PRIK");

        String decrypted = DecryptAes256(Extensions.Strings.decodeHex(Extensions.Bytes.toUtf8(chunk.getPayload())),
                encryptionKey,
                CipherMode.CBC,
                Arrays.copyOfRange(encryptionKey, 0, 16));

            final String header = "LastPassPrivateKey<";
            final String footer = ">LastPassPrivateKey";
        if (!decrypted.startsWith(header) || !decrypted.endsWith(footer))
            throw new ParseException(ParseException.FailureReason.CorruptedBlob, "Failed to decrypt private key");

        byte[] asn1EncodedKey = Extensions.Strings.decodeHex(decrypted.substring(header.length(),
                decrypted.length() - header.length() - footer.length()));

        KeyValuePair<Asn1.Kind, byte[]> enclosingSequence = asn1.parseItem(asn1EncodedKey);
        KeyValuePair<Asn1.Kind, byte[]> anotherEnclosingSequence = WithBytes(enclosingSequence.getValue(), new Func<DataInputStream, KeyValuePair<Asn1.Kind, byte[]>>() {
            @Override
            public KeyValuePair<Asn1.Kind, byte[]> execute(DataInputStream reader) throws IOException {
                    asn1.ExtractItem(reader);
                    asn1.ExtractItem(reader);
                    return asn1.ExtractItem(reader);
            }
        });
        KeyValuePair<Asn1.Kind, byte[]> yetAnotherEnclosingSequence = asn1.parseItem(anotherEnclosingSequence.getValue());

        return WithBytes(yetAnotherEnclosingSequence.getValue(), new Func<DataInputStream, RSAParameters>(){
            @Override
            public RSAParameters execute(DataInputStream reader) throws IOException {
                asn1.ExtractItem(reader);

                return new RSAParameters.Builder
                        ()
                        .setModulus(readInteger(reader)).
                                setExponent(readInteger(reader)).
                                setD(readInteger(reader)).
                                setP(readInteger(reader)).
                                setQ(readInteger(reader)).
                                setDP(readInteger(reader)).
                                setDQ(readInteger(reader)).
                                setInverseQ(readInteger(reader)).build(
                        );
            }});
    }

    private byte[] readInteger(DataInputStream reader) throws IOException {
        // There are occasional leading zeroes that need to be stripped.
        byte[] item = asn1.ExtractItem(reader).getValue();
        int offset = 0;
        while (item[offset] == 0) {
            offset++;
        }
        return Arrays.copyOfRange(item, offset, item.length - 1);
    }

    // TODO: Write a test for the RSA case!
    public SharedFolder Parse_SHAR(Chunk chunk, final byte[] encryptionKey, final RSAParameters rsaKey) {
        assert(chunk.getId() == "SHAR");

        return WithBytes(chunk.getPayload(), new Func<DataInputStream, SharedFolder>() {
            @Override
            public SharedFolder execute(DataInputStream reader) throws IOException {
                String id = Extensions.Bytes.toUtf8(ReadItem(reader));
                byte[] rsaEncryptedFolderKey = ReadItem(reader);
                byte[] encryptedName = ReadItem(reader);
                SkipItem(reader);
                SkipItem(reader);
                byte[] aesEncryptedFolderKey = ReadItem(reader);

                byte[] key = null;

                // Shared folder encryption key might come already in pre-decrypted form,
                // where it's only AES encrypted with the regular encryption key.
                if (aesEncryptedFolderKey.length > 0)
                {
                    key = Extensions.Strings.decodeHex(DecryptAes256Plain(aesEncryptedFolderKey, encryptionKey));
                }
                else
                {
                    // When the key is blank, then there's an RSA encrypted key, which has to
                    // be decrypted first before use.
                    try (RSACryptoServiceProvider rsa = new SimpleRSACryptoServiceProvider())
                    {
                        rsa.importParameters(rsaKey);
                        key = Extensions.Strings.decodeHex(Extensions.Bytes.toUtf8(rsa.decrypt(
                                Extensions.Strings.decodeHex(Extensions.Bytes.toUtf8(rsaEncryptedFolderKey)), true)));
                    }
                }

                return new SharedFolder(id, DecryptAes256Base64(encryptedName, key), key);
            }});
    }

    public void ParseSecureNoteServer(String notes,
                                             Ref<String> type,
                                             Ref<String> url,
                                             Ref<String> username,
                                             Ref<String> password)
    {
        for (String i : notes.split("\n"))
        {
            String[] keyValue = i.split(":", 2);
            if (keyValue.length < 2)
                continue;

            switch (keyValue[0])
            {
                case "NoteType":
                    type.setValue(keyValue[1]);
                    break;
                case "Hostname":
                    url.setValue(keyValue[1]);
                    break;
                case "Username":
                    username.setValue(keyValue[1]);
                    break;
                case "Password":
                    password.setValue(keyValue[1]);
                    break;
            }
        }
    }

    public List<Chunk> ExtractChunks(DataInputStream reader) {
        List<Chunk> chunks = new ArrayList<>();
        try
        {
            boolean stopped = false;
            while (!stopped) {
                chunks.add(ReadChunk(reader));
            }
        }
        catch (Exception e)
        {
            // In case the stream is truncated we just ignore the incomplete chunk.
            if (!(e.getCause() instanceof IOException)) {
                throw e;
            }
        }

        return chunks;
    }

    public Chunk ReadChunk(DataInputStream reader)
    {
        // LastPass blob chunk is made up of 4-byte ID, big endian 4-byte size and payload of that size
        // Example:
        //   0000: 'IDID'
        //   0004: 4
        //   0008: 0xDE 0xAD 0xBE 0xEF
        //   000C: --- Next chunk ---

        return new Chunk(ReadId(reader),
                ReadPayload(reader, ReadSize(reader)));
    }

    public byte[] ReadItem(DataInputStream reader) throws IOException
    {
        // An item in an itemized chunk is made up of the big endian size and the payload of that size
        // Example:
        //   0000: 4
        //   0004: 0xDE 0xAD 0xBE 0xEF
        //   0008: --- Next item ---

        return ReadPayload(reader, ReadSize(reader));
    }

    public void SkipItem(DataInputStream reader) throws IOException
    {
        // See ReadItem for item description.
//        reader.BaseStream.Seek(ReadSize(reader), SeekOrigin.Current);
        reader.read(new byte[ReadSize(reader)]);
    }

    public String ReadId(DataInputStream reader)
    {
        byte[] buffer = new byte[4];
        try {
            reader.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("ParserHelper.ReadId", e);
        }
    }

    public int ReadSize(DataInputStream reader)
    {
        try {
            return reader.readInt();
        } catch (IOException e) {
            throw new RuntimeException("ParserHelper.ReadSize", e);
        }
    }

    public byte[] ReadPayload(DataInputStream reader, int size)
    {
        byte[] buffer = new byte[size];
        try {
            reader.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException("ParserHelper.ReadPayload", e);
        }
        return buffer;
    }

    public String DecryptAes256Plain(byte[] data, byte[] encryptionKey, final String defaultValue)
    {
        return DecryptAes256WithDefaultValue(data, encryptionKey, defaultValue, new Func2<byte[], byte[], String>(){
            @Override
            public String execute(byte[] data, byte[] encryptionKey) {
                return DecryptAes256Plain(data, encryptionKey);
            }
        });
    }

    public String DecryptAes256Base64(byte[] data, byte[] encryptionKey, String defaultValue)
    {
        return DecryptAes256WithDefaultValue(data, encryptionKey, defaultValue, new Func2<byte[], byte[], String>(){
            @Override
            public String execute(byte[] data, byte[] encryptionKey) {
                try {
                    return DecryptAes256Base64(data, encryptionKey);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public String DecryptAes256Plain(byte[] data, byte[] encryptionKey)
    {
        int length = data.length;

        if (length == 0)
            return "";
        else if (data[0] == '!' && length % 16 == 1 && length > 32)
            return DecryptAes256CbcPlain(data, encryptionKey);
        else
            return DecryptAes256EcbPlain(data, encryptionKey);
    }

    public String DecryptAes256Base64(byte[] data, byte[] encryptionKey) throws UnsupportedEncodingException {
        int length = data.length;

        if (length == 0)
            return "";
        else if (data[0] == '!')
            return DecryptAes256CbcBase64(data, encryptionKey);
        else
            return DecryptAes256EcbBase64(data, encryptionKey);
    }

    public String DecryptAes256EcbPlain(byte[] data, byte[] encryptionKey)
    {
        return DecryptAes256(data, encryptionKey, CipherMode.ECB);
    }

    public String DecryptAes256EcbBase64(byte[] data, byte[] encryptionKey) throws UnsupportedEncodingException {
        return DecryptAes256(Extensions.Strings.decode64(decoder, Extensions.Bytes.toUtf8(data)), encryptionKey, CipherMode.ECB);
    }

    public String DecryptAes256CbcPlain(byte[] data, byte[] encryptionKey)
    {
        return DecryptAes256(Arrays.copyOfRange(data, 17, data.length),
                encryptionKey,
                CipherMode.CBC,
                Arrays.copyOfRange(data, 1, 17));
    }

    public String DecryptAes256CbcBase64(byte[] data, byte[] encryptionKey) throws UnsupportedEncodingException {
        return DecryptAes256(Extensions.Strings.decode64(decoder, Extensions.Bytes.toUtf8(Arrays.copyOfRange(data, 26, data.length - 26))),
                encryptionKey,
                CipherMode.CBC,
                Extensions.Strings.decode64(decoder, Extensions.Bytes.toUtf8(Arrays.copyOfRange(data, 1, 1 + 24))));
    }

    public String DecryptAes256(byte[] data, byte[] encryptionKey, CipherMode mode)
    {
        return DecryptAes256(data, encryptionKey, mode, new byte[16]);
    }

    public String DecryptAes256(byte[] data, byte[] encryptionKey, CipherMode mode, byte[] iv) {
        if (data.length == 0)
            return "";

        try (AesManaged aes = ioc.get(AesManagedFactory.class).create(256, encryptionKey, mode, iv);
             /*AesDecryptor decryptor = aes.createDecryptor();
             ByteArrayInputStream stream = new ByteArrayInputStream(data);
             cryptoStream = new CryptoStream(stream, decryptor, CryptoStreamMode.Read);
             Reader reader = new StreamReader(cryptoStream)*/)
        {
            // TODO: StreamReader is a text reader. This might fail with arbitrary binary encrypted
            //       data. Luckily we have only text encrypted. Pay attention when refactoring!
//            return reader.ReadToEnd();
            return aes.decrypt(data);
        } catch (IOException e) {
            throw new RuntimeException("I/O Error in DecryptAes256", e);
        }
    }

    public void WithBytes(byte[] bytes, final TAction<DataInputStream> action) {
        WithBytes(bytes, new Func<DataInputStream, Integer>(){
            @Override
            public Integer execute(DataInputStream reader) {
                action.execute(reader);
                return 0;
            };
        });
    }

    public <TResult> TResult WithBytes(byte[] bytes, Func<DataInputStream, TResult> action) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
             DataInputStream reader = new DataInputStream(stream)) {
            return action.execute(reader);
        } catch (IOException e) {
            throw new RuntimeException("ParserHelper.WithBytes", e);
        }
    }

    private String DecryptAes256WithDefaultValue(byte[] data,
                                                        byte[] encryptionKey,
                                                        String defaultValue,
                                                        Func2<byte[], byte[], String> decrypt)
    {
        try
        {
            return decrypt.execute(data, encryptionKey);
        }
        catch (Exception ex)
        {
            return defaultValue;
        }
    }

    private static final HashSet<String> AllowedSecureNoteTypes = new HashSet<String>();
    static
    {
        AllowedSecureNoteTypes.add("Server");
        AllowedSecureNoteTypes.add("Email Account");
        AllowedSecureNoteTypes.add("Database");
        AllowedSecureNoteTypes.add("Instant Messenger");
    }
}

