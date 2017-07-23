package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.adapters.Base64Decoder;
import com.in2bits.shims.KeyValuePair;
import com.in2bits.shims.WebClient;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.WebClientFactory;
import com.in2bits.shims.WebException;
import com.in2bits.shims.XAttribute;
import com.in2bits.shims.XDocument;
import com.in2bits.shims.XElement;
import com.in2bits.shims.XmlException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class Fetcher
{
    final private Ioc ioc;

    Fetcher(Ioc ioc) {
        this.ioc = ioc;
    }

    public Session Login(String username, String password, String multifactorPassword) throws LoginException
    {
        try (WebClient webClient = ioc.get(WebClientFactory.class).createWebClient()) {
            return Login(username, password, multifactorPassword, webClient);
        } catch (IOException e) {
            throw new RuntimeException("Fetcher.Login", e);
        }
    }

    public Session Login(String username, String password, String multifactorPassword, WebClient webClient) throws LoginException {
        // First we need to request PBKDF2 key interation count
        int keyIterationCount = RequestIterationCount(username, webClient);

        // Knowing the iterations count we can hash the password and log in
        Document doc = Login(username, password, multifactorPassword, keyIterationCount, webClient);

        // Parse the response
        Node ok = doc.getElementsByTagName("ok").item(0);
        if (ok != null)
        {
            Attr sessionId = (Attr) ok.getAttributes().getNamedItem("sessionid");
            if (sessionId != null)
            {
                return new Session(sessionId.getValue(), keyIterationCount);
            }
        }

        Element response = (Element) doc.getElementsByTagName("response").item(0);
        Element error = (Element) response.getElementsByTagName("error").item(0);

        throw CreateLoginException(error);
    }

    public Blob Fetch(Session session) throws FetchException {
        try (WebClient webClient = ioc.get(WebClientFactory.class).createWebClient()) {
            return Fetch(session, webClient);
        } catch (IOException e) {
            throw new RuntimeException("Fetcher.Fetch", e);
        }
    }

    public Blob Fetch(Session session, WebClient webClient) throws UnsupportedEncodingException, FetchException {
        webClient.addHeader("Cookie", String.format("PHPSESSID=%s", URLEncoder.encode(session.getId(), "UTF-8")));

        byte[] response;
        try
        {
            response = webClient.downloadData("https://lastpass.com/getaccts.php?mobile=1&hash=0.0&hasplugin=3.0.23&requestsrc=android");
        }
        catch (WebException e)
        {
            throw new FetchException(FetchException.FailureReason.WebException, "WebException occured", e);
        }

        try
        {
            return new Blob(ioc, response, session.getKeyIterationCount());
        }
        catch (Exception e)
        {
            throw new FetchException(FetchException.FailureReason.InvalidResponse, "Invalid base64 in response", e);
        }
    }

    private int RequestIterationCount(String username, WebClient webClient) throws LoginException {
        try
        {
            List<KeyValuePair<String, String>> values = new ArrayList<>();
            values.add(new KeyValuePair<>("email", username));
            // LastPass server is supposed to return paint text int, nothing fancy.
            return Integer.parseInt(Extensions.Bytes.toUtf8(webClient.uploadValues("https://lastpass.com/iterations.php",
                    values)));
        }
        catch (WebException e)
        {
            throw new LoginException(LoginException.FailureReason.WebException, "WebException occured", e);
        }
        catch (NumberFormatException e)
        {
            throw new LoginException(LoginException.FailureReason.InvalidResponse,
                "Iteration count is invalid",
                e);
        }
    }

    private Document Login(String username,
                           String password,
                           String multifactorPassword,
                           int keyIterationCount,
                           WebClient webClient) throws LoginException {
        try
        {
            List<KeyValuePair<String, String>> parameters = new ArrayList<>();
            parameters.add(new KeyValuePair<>("method", "mobile"));
            parameters.add(new KeyValuePair<>("web", "1"));
            parameters.add(new KeyValuePair<>("xml", "1"));
            parameters.add(new KeyValuePair<>("username", username));
            String hash = new FetcherHelper(ioc).makeHash(username, password, keyIterationCount);
            System.out.println("hash: " + hash);
            parameters.add(new KeyValuePair<>("hash", hash));
            parameters.add(new KeyValuePair<>("iterations", String.format("%s", keyIterationCount)));

            if (multifactorPassword != null)
                parameters.add(new KeyValuePair<>("otp", multifactorPassword));
            parameters.add(new KeyValuePair<>("imei", "lastPass-Java"));

            String xml = Extensions.Bytes.toUtf8(webClient.uploadValues("https://lastpass.com/login.php", parameters));

            Document doc;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(new StringReader(xml)));
            } catch (SAXException e) {
                throw new XmlException("Xml Reading Error", e);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException("Parser Configuration Error", e);
            } catch (IOException e) {
                throw new RuntimeException("I/O Error", e);
            }
            return doc;
        }
        catch (WebException e)
        {
            throw new LoginException(LoginException.FailureReason.WebException,
                    "WebException occured", e);
        }
        catch (XmlException e)
        {
            throw new LoginException(LoginException.FailureReason.InvalidResponse,
                    "Invalid XML in response", e);
        }
    }

    private LoginException CreateLoginException(Element error)
    {
        // XML is valid but there's nothing in it we can understand
        if (error == null)
        {
            return new LoginException(LoginException.FailureReason.UnknownResponseSchema, "Unknown response schema");
        }

        // Both of these are optional
        Attr cause = (Attr) error.getAttributes().getNamedItem("cause");
        Attr message = (Attr) error.getAttributes().getNamedItem("message");

        // We have a cause element, see if it's one of ones we know
        if (cause != null)
        {
            String causeValue = cause.getValue();
            switch (causeValue)
            {
                case "unknownemail":
                    return new LoginException(LoginException.FailureReason.LastPassInvalidUsername,
                            "Invalid username");
                case "unknownpassword":
                    return new LoginException(LoginException.FailureReason.LastPassInvalidPassword,
                            "Invalid password");
                case "googleauthrequired":
                case "googleauthfailed":
                    return new LoginException(LoginException.FailureReason.LastPassIncorrectGoogleAuthenticatorCode,
                            "Google Authenticator code is missing or incorrect");
                case "yubikeyrestricted":
                    return new LoginException(LoginException.FailureReason.LastPassIncorrectYubikeyPassword,
                            "Yubikey password is missing or incorrect");
                case "outofbandrequired":
                    return new LoginException(LoginException.FailureReason.LastPassOutOfBandAuthenticationRequired,
                            "Out of band authentication required");
                case "multifactorresponsefailed":
                    return new LoginException(LoginException.FailureReason.LastPassOutOfBandAuthenticationFailed,
                            "Out of band authentication failed");
                case "mobilerestricted":
                    return new LoginException(LoginException.FailureReason.LastPassOther,
                            "Mobile device not authorized");
                default:
                    return new LoginException(LoginException.FailureReason.LastPassOther,
                            message != null ? message.getValue() : causeValue);
            }
        }

        // No cause, maybe at least a message
        if (message != null)
        {
            return new LoginException(LoginException.FailureReason.LastPassOther, message.getValue());
        }

        // Nothing we know, just the error element
        return new LoginException(LoginException.FailureReason.LastPassUnknown, "Unknown reason");
    }
}
