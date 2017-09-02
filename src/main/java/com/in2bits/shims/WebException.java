package com.in2bits.shims;

/**
 * Created by Tim on 7/4/17.
 */

public class WebException extends RuntimeException {
    public WebException() {
        super();
    }

    public WebException(String message, Throwable t) {
        super(message, t);
    }
}
