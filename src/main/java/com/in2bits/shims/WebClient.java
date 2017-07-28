package com.in2bits.shims;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Created by Tim on 7/3/17.
 */

public interface WebClient extends Closeable {
    void addHeader(String name, String value);
    Map<String, String> getHeaders();
    byte[] downloadData(String uri) throws WebException;
    byte[] uploadValues(String uri, List<KeyValuePair<String, String>> values) throws WebException;
}
