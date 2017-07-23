package com.in2bits.shims;

/**
 * Created by Tim on 7/4/17.
 */

public class WebException extends Exception {
    public WebException(String message, Throwable t) {
        super(message, t);
    }
}
