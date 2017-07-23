package com.in2bits;

/**
 * Created by Tim on 7/3/17.
 */

public class LoginException extends BaseException {
    public enum FailureReason {
        // LastPass returned errors
        LastPassInvalidUsername,
        LastPassInvalidPassword,
        LastPassIncorrectGoogleAuthenticatorCode,
        LastPassIncorrectYubikeyPassword,
        LastPassOutOfBandAuthenticationRequired,
        LastPassOutOfBandAuthenticationFailed,
        LastPassOther, // Message property contains the message given by the LastPass server
        LastPassUnknown,

        // Other
        WebException,
        UnknownResponseSchema,
        InvalidResponse
    }

    public LoginException(FailureReason reason, String message) {
        this(reason, message, null);
    }

    public LoginException(FailureReason reason, String message, Exception innerException) {
        super(message, innerException);
        this.reason = reason;
    }

    private final FailureReason reason;
    public FailureReason getReason() {
        return reason;
    }
}
