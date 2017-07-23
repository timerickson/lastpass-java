package com.in2bits.debug;

import com.in2bits.shims.WebClient;
import com.in2bits.shims.WebClientFactory;

/**
 * Created by Tim on 7/20/17.
 */
public class SimpleWebClientFactory implements WebClientFactory {
    @Override
    public WebClient createWebClient() {
        return new SimpleWebClient();
    }
}
