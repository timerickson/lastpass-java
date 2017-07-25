package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.


import com.in2bits.shims.Ioc;
import com.in2bits.shims.RSAParameters;
import com.in2bits.shims.Ref;
import com.in2bits.shims.TAction;

import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vault
{
    public static Vault Create(Ioc ioc, String username, String password) throws LoginException, FetchException, ParseException {
        return Create(ioc, username, password, null);
    }

    public static Vault Create(Ioc ioc, String username, String password, String multifactorPassword) throws LoginException, FetchException, ParseException
    {
        return Create(ioc, Download(ioc, username, password, multifactorPassword), username, password);
    }

    // TODO: Make a test for this!
    public static Vault Create(Ioc ioc, Blob blob, String username, String password) throws ParseException {
        return new Vault(ioc, blob, blob.MakeEncryptionKey(username, password));
    }

    public static Blob Download(Ioc ioc, String username, String password) throws FetchException, LoginException {
        return Download(ioc, username, password, null);
    }

    public static Blob Download(Ioc ioc, String username, String password, String multifactorPassword) throws FetchException, LoginException {
        Fetcher fetcher = new Fetcher(ioc);
        return fetcher.Fetch(fetcher.Login(username, password, multifactorPassword));
    }

    final private Ioc ioc;

    // TODO: Make a test for this!
    // TODO: Extract some of the code and put it some place else.
    private Vault(Ioc ioc, Blob blob, final byte[] encryptionKey) throws ParseException
    {
        this.ioc = ioc;
        final Ref<ParseException> exRef = new Ref<>();
        final Ref<Account[]> accountsRef = new Ref<>();
        final ParserHelper parserHelper = new ParserHelper(ioc);
        parserHelper.WithBytes(blob.getBytes(), new TAction<DataInputStream>(){
            @Override
            public void execute(DataInputStream reader) {
                List<ParserHelper.Chunk> chunks = parserHelper.ExtractChunks(reader);
                if (!IsComplete(chunks)) {
                    exRef.setValue(new ParseException(ParseException.FailureReason.CorruptedBlob, "Blob is truncated"));
                    return;
                }

                try {
                    accountsRef.setValue(ParseAccounts(chunks, encryptionKey));
                } catch (ParseException e) {
                    exRef.setValue(e);
                    return;
                }
            }});
        if (exRef.hasValue()) {
            throw exRef.getValue();
        }
        accounts = accountsRef.getValue();
    }

    private boolean IsComplete(List<ParserHelper.Chunk> chunks)
    {
        int count = chunks.size();
        ParserHelper.Chunk last = count > 0 ? chunks.get(count - 1) : null;
        try {
            return count > 0 && last.getId().equals("ENDM") && Arrays.equals(last.getPayload(), "OK".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            return false;
        }
    }

    private Account[] ParseAccounts(List<ParserHelper.Chunk> chunks, byte[] encryptionKey) throws ParseException {
        List<Account> accounts = new ArrayList<>();
//        chunks.forEach(i -> { if (i.getId().equals("ACCT")) accounts.add(i)});
        SharedFolder folder = null;
        RSAParameters rsaKey = new RSAParameters();

        ParserHelper parserHelper = new ParserHelper(ioc);
        for (ParserHelper.Chunk i : chunks)
        {
            switch (i.getId())
            {
                case "ACCT":
                    Account account = parserHelper.Parse_ACCT(i,
                            folder == null ? encryptionKey : folder.getEncryptionKey(),
                            folder);
                    if (account != null)
                        accounts.add(account);
                    break;
                case "PRIK":
                    rsaKey = parserHelper.Parse_PRIK(i, encryptionKey);
                    break;
                case "SHAR":
                    folder = parserHelper.Parse_SHAR(i, encryptionKey, rsaKey);
                    break;
            }
        }

        return accounts.toArray(new Account[0]);
    }

    private Account[] accounts;
    public Account[] getAccounts() {
        Account[] result = new Account[this.accounts.length];
        System.arraycopy(this.accounts, 0, result, 0, result.length);
        return result;
    }

}
