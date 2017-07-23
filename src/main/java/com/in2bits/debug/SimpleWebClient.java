package com.in2bits.debug;

import com.in2bits.shims.KeyValuePair;
import com.in2bits.shims.WebClient;
import com.in2bits.shims.WebException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tim on 7/20/17.
 */
public class SimpleWebClient implements WebClient {
    final private Map<String, String> requestHeaders = new HashMap<>();

    private HttpURLConnection conn;

    @Override
    public void addHeader(String name, String value) {
        requestHeaders.put(name, value);
    }

    private void addHeaders() {
        for (String name : requestHeaders.keySet()) {
            conn.setRequestProperty(name, requestHeaders.get(name));
        }
    }

    @Override
    public byte[] downloadData(String uri) throws WebException {
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            addHeaders();
            conn.setRequestMethod("GET");
            return readResponse();
        } catch (MalformedURLException e) {
            throw new WebException("MalformedURL", e);
        } catch (ProtocolException e) {
            throw new WebException("Protocol Error", e);
        } catch (IOException e) {
            throw new WebException("I/O Error", e);
        }
    }

    private byte[] readResponse() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        try (InputStream input = conn.getInputStream()) {
            while ((n = input.read(buffer)) != -1) {
                byteStream.write(buffer, 0, n);
            }
        }
        return byteStream.toByteArray();
    }

    final private String CHARSET = "UTF-8";

    private String urlEncode(List<KeyValuePair<String, String>> data) throws UnsupportedEncodingException {
        StringBuilder encoded = new StringBuilder();
        for (KeyValuePair<String,String> pair : data) {
            if (encoded.length() > 0) {
                encoded.append("&");
            }
//            if (key.equals("username")) {
//                encoded.append(String.format("%s=%s", key, data.get(key)));
//            } else {
                encoded.append(String.format("%s=%s", URLEncoder.encode(pair.getKey(), CHARSET), URLEncoder.encode(pair.getValue(), CHARSET)));
//            }
        }
        return encoded.toString();
    }

    @Override
    public byte[] uploadValues(String uri, List<KeyValuePair<String, String>> values) throws WebException {
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            addHeaders();
            String urlEncodedData = urlEncode(values);
            System.out.println("data: " + urlEncodedData);
            byte[] postBytes = urlEncodedData.getBytes(CHARSET);

//            conn.setRequestProperty("Accept-Encoding", "gzip;q=1.0,deflate;q=0.6,identity;q=0.3");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Charset", CHARSET);
            conn.setFixedLengthStreamingMode(postBytes.length);
//            conn.setRequestProperty("Content-Length", String.valueOf(postBytes));
//            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("User-Agent", "lastpass-java");

            try (OutputStream out = conn.getOutputStream()) {
                out.write(postBytes);
            }
            return readResponse();
        } catch (UnsupportedEncodingException e) {
            throw new WebException("UnsupportedEncoding", e);
        } catch (MalformedURLException e) {
            throw new WebException("MalformedURL", e);
        } catch (IOException e) {
            throw new WebException("I/O Error", e);
        }
    }

    @Override
    public void close() throws IOException {
        // no-op
    }
}
