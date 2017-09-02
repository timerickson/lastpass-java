package com.in2bits;

// Copyright (C) 2013 Dmitry Yakimenko (detunized@gmail.com).
// Licensed under the terms of the MIT license. See LICENCE for details.

import com.in2bits.debug.SimpleIoc;
import com.in2bits.shims.Action1;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.KeyValuePair;
import com.in2bits.shims.Ref;
import com.in2bits.shims.WebClient;
import com.in2bits.shims.WebException;
import com.in2bits.shims.XmlException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.stubbing.OngoingStubbing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetcherTestLogin extends FetcherTest
    {
        //
        // Shared data
        //

        private static final String Username = "username";
        private final String Password = "password";

        private final String IterationsUrl = "https://lastpass.com/iterations.php";
        private final String LoginUrl = "https://lastpass.com/login.php";

        private final String NoMultifactorPassword = null;
        private final String GoogleAuthenticatorCode = "123456";
        private final String YubikeyPassword = "emdbwzemyisymdnevznyqhqnklaqheaxszzvtnxjrmkb";

        private static final ResponseOrException IterationsResponse = new ResponseOrException(String.valueOf(IterationCount));
        private static final ResponseOrException OkResponse = new FetcherTest.ResponseOrException(String.format("<ok sessionid=\"%s\" />",
                                                                                                       SessionId));

        private static final List<KeyValuePair<String, String>> ExpectedIterationsRequestValues = new ArrayList<>();
        static
            {
                ExpectedIterationsRequestValues.add(new KeyValuePair<>("email", Username));
            };

        private static final List<KeyValuePair<String, String>> ExpectedLoginRequestValues = new ArrayList<>();
        static
            {
                ExpectedLoginRequestValues.add(new KeyValuePair<>("method", "mobile"));
                ExpectedLoginRequestValues.add(new KeyValuePair<>("web", "1"));
                ExpectedLoginRequestValues.add(new KeyValuePair<>("xml", "1"));
                ExpectedLoginRequestValues.add(new KeyValuePair<>("username", Username));
                ExpectedLoginRequestValues.add(new KeyValuePair<>("hash", "7880a04588cfab954aa1a2da98fd9c0d2c6eba4c53e36a94510e6dbf30759256"));
                ExpectedLoginRequestValues.add(new KeyValuePair<>("iterations", String.format("%s", IterationCount)));
                ExpectedLoginRequestValues.add(new KeyValuePair<String, String>("imei", "lastPass-Java"));
            };

        private final String IncorrectGoogleAuthenticatorCodeMessage = "Google Authenticator code is missing or incorrect";
        private final String IncorrectYubikeyPasswordMessage = "Yubikey password is missing or incorrect";
        private final String OtherCause = "othercause";
        private final String OtherReasonMessage = "Other reason";

        //
        // Login tests
        //

        @Test
        public void Login_failed_because_of_WebException_in_iterations_request()
        {
            LoginAndVerifyExceptionInIterationsRequest(new ResponseOrException(new WebException()),
                                                                     LoginException.FailureReason.WebException,
                                                                     WebExceptionMessage,
                    WebException.class);
        }

        @Test
        public void Login_failed_because_of_invalid_iteration_count()
        {
            LoginAndVerifyExceptionInIterationsRequest(new ResponseOrException("Not an integer"),
                                                                        LoginException.FailureReason.InvalidResponse,
                                                                        "Iteration count is invalid",
                    NumberFormatException.class);
        }

        @Ignore("Java doesn't throw Overflow Exceptions")
        @Test
        public void Login_failed_because_of_very_large_iteration_count()
        {

            LoginAndVerifyExceptionInIterationsRequest(new ResponseOrException("2147483648"),
                                                                          LoginException.FailureReason.InvalidResponse,
                                                                          "Iteration count is invalid",
                    LoginException.class);
        }

        @Test
        public void Login_failed_because_of_WebException_in_login_request()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException(new WebException()),
                                                                LoginException.FailureReason.WebException,
                                                                WebExceptionMessage,
                    new Action1<Throwable>(){
                        @Override
                        public void execute(Throwable t) {
                            assertTrue(WebException.class.isAssignableFrom(t.getClass()));
                        }
                    });
        }

        @Test
        public void Login_failed_because_of_invalid_xml()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException("Invalid XML!"),
                                                                LoginException.FailureReason.InvalidResponse,
                                                                "Invalid XML in response",
                    XmlException.class);
        }

        @Test
        public void Login_failed_because_of_unknown_email()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("unknownemail", "Unknown email address."),
                                                  LoginException.FailureReason.LastPassInvalidUsername,
                                                  "Invalid username");
        }

        @Test
        public void Login_failed_because_of_invalid_password()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("unknownpassword", "Invalid password!"),
                                                  LoginException.FailureReason.LastPassInvalidPassword,
                                                  "Invalid password");
        }

        @Test
        public void Login_failed_because_of_missing_google_authenticator_code()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("googleauthrequired",
                                                                 "Google Authenticator authentication required! Upgrade your browser extension so you can enter it."),
                                                  LoginException.FailureReason.LastPassIncorrectGoogleAuthenticatorCode,
                                                  IncorrectGoogleAuthenticatorCodeMessage);
        }

        @Test
        public void Login_failed_because_of_incorrect_google_authenticator_code()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("googleauthfailed",
                                                                 "Google Authenticator authentication failed!"),
                                                  LoginException.FailureReason.LastPassIncorrectGoogleAuthenticatorCode,
                                                  IncorrectGoogleAuthenticatorCodeMessage);
        }

        @Test
        public void Login_failed_because_of_missing_yubikey_password()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("yubikeyrestricted",
                                                                 "Your account settings have restricted you from logging in from mobile devices that do not support YubiKey authentication."),
                                                  LoginException.FailureReason.LastPassIncorrectYubikeyPassword,
                                                  IncorrectYubikeyPasswordMessage);
        }

        @Test
        public void Login_failed_because_of_incorrect_yubikey_password()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("yubikeyrestricted",
                                                                 "Your account settings have restricted you from logging in from mobile devices that do not support YubiKey authentication."),
                                                  LoginException.FailureReason.LastPassIncorrectYubikeyPassword,
                                                  IncorrectYubikeyPasswordMessage);
        }

        @Test
        public void Login_failed_because_out_of_band_authentication_required()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("outofbandrequired",
                                                                 "Multifactor authentication required! Upgrade your browser extension so you can enter it."),
                                                  LoginException.FailureReason.LastPassOutOfBandAuthenticationRequired,
                                                  "Out of band authentication required");
        }

        @Test
        public void Login_failed_because_out_of_band_authentication_failed()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse("multifactorresponsefailed",
                                                                 "Multifactor authentication failed!"),
                                                  LoginException.FailureReason.LastPassOutOfBandAuthenticationFailed,
                                                  "Out of band authentication failed");
        }

        @Test
        public void Login_failed_for_other_reason_with_message()
        {
            LoginAndVerifyExceptionInLoginRequest(FormatResponse(OtherCause, OtherReasonMessage),
                                                  LoginException.FailureReason.LastPassOther,
                                                  OtherReasonMessage);
        }

        @Test
        public void Login_failed_for_other_reason_without_message()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException(String.format("<response><error cause=\"%s\"/></response>",
                                                                                        OtherCause)),
                                                  LoginException.FailureReason.LastPassOther,
                                                  OtherCause);
        }

        @Test
        public void Login_failed_with_message_without_cause()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException(String.format("<response><error message=\"%s\"/></response>",
                                                                          OtherReasonMessage)),
                                                  LoginException.FailureReason.LastPassOther,
                                                  OtherReasonMessage);
        }

        @Test
        public void Login_failed_for_unknown_reason_with_error_element()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException("<response><error /></response>"),
                                                  LoginException.FailureReason.LastPassUnknown,
                                                  "Unknown reason");
        }

        @Test
        public void Login_failed_because_of_unknown_xml_schema()
        {
            LoginAndVerifyExceptionInLoginRequest(new ResponseOrException("<response />"),
                                                  LoginException.FailureReason.UnknownResponseSchema,
                                                  "Unknown response schema");
        }

        @Test
        public void Login_makes_iterations_request()
        {
            LoginAndVerifyIterationsRequest(NoMultifactorPassword, ExpectedIterationsRequestValues);
        }

        @Test
        public void Login_makes_iterations_request_with_google_authenticator()
        {
            LoginAndVerifyIterationsRequest(GoogleAuthenticatorCode, ExpectedIterationsRequestValues);
        }

        @Test
        public void Login_makes_iterations_request_with_yubikey()
        {
            LoginAndVerifyIterationsRequest(YubikeyPassword, ExpectedIterationsRequestValues);
        }

        @Test
        public void Login_makes_login_request_without_multifactor_password()
        {
            LoginAndVerifyLoginRequest(NoMultifactorPassword, ExpectedLoginRequestValues);
        }

        @Test
        public void Login_makes_login_request_with_google_authenticator()
        {
            List<KeyValuePair<String, String>> values = new ArrayList<>(ExpectedLoginRequestValues);
            values.add(new KeyValuePair<>("otp", GoogleAuthenticatorCode));
            LoginAndVerifyLoginRequest(GoogleAuthenticatorCode,
                                       values);
        }

        @Test
        public void Login_makes_login_request_with_yubikey()
        {
            List<KeyValuePair<String, String>> values = new ArrayList<>(ExpectedLoginRequestValues);
            values.add(new KeyValuePair<>("otp", YubikeyPassword));
            LoginAndVerifyLoginRequest(YubikeyPassword,
                                       values);
        }

        @Test
        public void Login_returns_session_without_multifactor_password()
        {
            LoginAndVerifySession(NoMultifactorPassword);
        }

        @Test
        public void Login_returns_session_with_google_authenticator()
        {
            LoginAndVerifySession(GoogleAuthenticatorCode);
        }

        @Test
        public void Login_returns_session_with_yubikey_password()
        {
            LoginAndVerifySession(YubikeyPassword);
        }

        //
        // Helpers
        //

        // Formats a valid LastPass response with a cause and a message.
        private static ResponseOrException FormatResponse(String cause, String message)
        {
            return new ResponseOrException(String.format("<response><error message=\"%s\" cause=\"%s\"/></response>",
                                                         message,
                                                         cause));
        }

        // Set up the login process. Response-or-exception parameters provide either
        // response or exception depending on the desired behavior. The login process
        // is two phase: request iteration count, then log in receive the session id.
        // Each of the stages might fail because of the network problems or some other
        // reason.
        private WebClient SetupLogin(ResponseOrException iterationsResponseOrException)
        {
            return SetupLogin(iterationsResponseOrException, null);
        }

        // Set up the login process. Response-or-exception parameters provide either
        // response or exception depending on the desired behavior. The login process
        // is two phase: request iteration count, then log in receive the session id.
        // Each of the stages might fail because of the network problems or some other
        // reason.
        private WebClient SetupLogin(ResponseOrException iterationsResponseOrException,
                                                   ResponseOrException loginResponseOrException)
        {
            WebClient webClient = mock(WebClient.class);
            OngoingStubbing<byte[]> sequence = when(webClient.uploadValues(anyString(), Matchers.<List<KeyValuePair<String, String>>>any()));

            sequence = iterationsResponseOrException.returnOrThrow(sequence);
            if (loginResponseOrException != null)
                sequence = loginResponseOrException.returnOrThrow(sequence);

            return webClient;
        }

        // Immitates the successful login sequence.
        private WebClient SuccessfullyLogin(String multifactorPassword)
        {
            Ref<Session> sessionRef = new Ref<>();
            return SuccessfullyLogin(multifactorPassword, sessionRef);
        }

        // Immitates the successful login sequence, returns the session.
        private WebClient SuccessfullyLogin(String multifactorPassword, Ref<Session> sessionRef)
        {
            WebClient webClient = SetupLogin(IterationsResponse, OkResponse);
            Ioc ioc = new SimpleIoc();
            Fetcher fetcher = new Fetcher(ioc);
            sessionRef.setValue(fetcher.Login(Username, Password, multifactorPassword, webClient));
            return webClient;
        }

        // Try to login and expect an exception, which is later validated by the caller.
        private LoginException LoginAndFailWithException(String multifactorPassword,
                                                                ResponseOrException iterationsResponseOrException)
        {
            return LoginAndFailWithException(multifactorPassword, iterationsResponseOrException, null);
        }

        // Try to login and expect an exception, which is later validated by the caller.
        private LoginException LoginAndFailWithException(String multifactorPassword,
                                                                ResponseOrException iterationsResponseOrException,
                                                                ResponseOrException loginResponseOrException)
        {
            WebClient webClient = SetupLogin(iterationsResponseOrException, loginResponseOrException);
            Ioc ioc = new SimpleIoc();
            Fetcher fetcher = new Fetcher(ioc);
            try {
                fetcher.Login(Username, Password, multifactorPassword, webClient);
            } catch (Exception ex) {
                return (LoginException)ex;
            }
            return null;
        }

        // Fail in iterations request and verify the exception.
        // Response-or-exception argument should either a String
        // with the provided response or an exception to be thrown.
        private void LoginAndVerifyExceptionInIterationsRequest(ResponseOrException iterationsResponseOrException,
                                                                LoginException.FailureReason reason,
                                                                String message,
                                                                final Class<? extends Throwable> innerExceptionClass)
        {
            LoginAndVerifyException(iterationsResponseOrException,
                    null,
                    reason,
                    message,
                    new Action1<Throwable>(){@Override public void execute(Throwable ex) {assertTrue(innerExceptionClass.isAssignableFrom(ex.getClass()));}});
        }

        // See the overload with an action.
        private void LoginAndVerifyExceptionInLoginRequest(ResponseOrException loginResponseOrException,
                                                                 LoginException.FailureReason reason,
                                                                 String message,
                                                           final Class<? extends Throwable> exClass)
        {
            LoginAndVerifyExceptionInLoginRequest(loginResponseOrException, reason, message,
                                                  new Action1<Throwable>(){@Override public void execute(Throwable ex) {assertTrue(exClass.isAssignableFrom(ex.getClass()));}});
        }

        // See the overload with an action.
        private void LoginAndVerifyExceptionInLoginRequest(ResponseOrException loginResponseOrException,
                                                           LoginException.FailureReason reason,
                                                           String message)
        {
            LoginAndVerifyExceptionInLoginRequest(loginResponseOrException, reason, message,
                                                  new Action1<Throwable>(){@Override public void execute(Throwable ex) {assertNull(ex);}});
        }

        // Fail in login request and verify the exception.
        // Response-or-exception argument should either a String
        // with the provided response or an exception to be thrown.
        // The iterations request is not supposed to fail and it's
        // given a valid server response with the proper iteration count.
        private void LoginAndVerifyExceptionInLoginRequest(ResponseOrException loginResponseOrException,
                                                                  LoginException.FailureReason reason,
                                                                  String message,
                                                                  Action1<Throwable> verifyInnerException)
        {
            LoginAndVerifyException(IterationsResponse,
                                    loginResponseOrException,
                                    reason,
                                    message,
                                    verifyInnerException);
        }

        // The most generic version. It expects on the requests to fail with an exception.
        // The exception is verified agains the expectations.
        private void LoginAndVerifyException(ResponseOrException iterationsResponseOrException,
                                                    ResponseOrException loginResponseOrException,
                                                    LoginException.FailureReason reason,
                                                    String message,
                                                    Action1<Throwable> verifyInnerException)
        {
            LoginException exception = LoginAndFailWithException(NoMultifactorPassword,
                                                      iterationsResponseOrException,
                                                      loginResponseOrException);

            assertEquals(reason, exception.getReason());
            assertEquals(message, exception.getMessage());
            verifyInnerException.execute(exception.getCause());
        }

        // Verify the iterations POST request is correct.
        private void LoginAndVerifyIterationsRequest(String multifactorPassword,
                                                            final List<KeyValuePair<String, String>> expectedValues)
        {
            WebClient webClient = SuccessfullyLogin(multifactorPassword);
            verify(webClient).uploadValues(eq(IterationsUrl),
                                                 Matchers.argThat(new BaseMatcher<List<KeyValuePair<String, String>>>(){
                                                     @Override
                                                     public boolean matches(Object o) {
                                                         return AreEqual((List<KeyValuePair<String, String>>)o, expectedValues);
                                                     }
                                                     @Override
                                                     public void describeTo(Description d) {
                                                         throw new RuntimeException("TODO");
                                                     }
                                                 }));
//                             "Did not see iterations POST request with expected form data and/or URL");
        }

        // Verify the login POST request is correct.
        private void LoginAndVerifyLoginRequest(String multifactorPassword,
                                                       final List<KeyValuePair<String, String>> expectedValues)
        {
            WebClient webClient = SuccessfullyLogin(multifactorPassword);
            verify(webClient).uploadValues(eq(LoginUrl),
                                                 Matchers.argThat(new BaseMatcher<List<KeyValuePair<String, String>>>() {
                                                     @Override
                                                             public boolean matches(Object o) {
                                                         return AreEqual((List<KeyValuePair<String, String>>)o, expectedValues);
                                                     }
                                                     @Override
                                                     public void describeTo(Description d) {
                                                         //throw new RuntimeException("TODO");
                                                     }
                                                 }));
//                             "Did not see login POST request with expected form data and/or URL");
        }

        // Verify the session is correct.
        private void LoginAndVerifySession(String multifactorPassword)
        {
            Ref<Session> sessionRef = new Ref<>();
            SuccessfullyLogin(multifactorPassword, sessionRef);

            assertEquals(SessionId, sessionRef.getValue().getId());
            assertEquals(IterationCount, sessionRef.getValue().getKeyIterationCount());
        }

        private static boolean AreEqual(List<KeyValuePair<String, String>> a, List<KeyValuePair<String, String>> b)
        {
            if (a.size() != b.size()) {
                return false;
            }
            Comparator<KeyValuePair<String, String>> comparator = new Comparator<KeyValuePair<String, String>>() {
                @Override
                public int compare(KeyValuePair<String, String> o1, KeyValuePair<String, String> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            };
            Collections.sort(a, comparator);
            Collections.sort(b, comparator);
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).getKey().equals(b.get(i).getKey())) {
                    return false;
                }
            }
            for (KeyValuePair<String, String> pair : a) {
                boolean foundMatch = false;
                for (KeyValuePair<String, String> otherPair : b) {
                    if (pair.getKey().equals(otherPair.getKey())) {
                        String aVal = pair.getValue();
                        String bVal = otherPair.getValue();
                        if ((aVal == null && bVal != null)
                                || (aVal != null && bVal == null)) {
                            return false;
                        }
                        if (aVal == null && bVal == null) {
                            foundMatch = true;
                        }
                        if (!aVal.equals(bVal)) {
                            return false;
                        }
                        foundMatch = true;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }
            return true;
        }
    }
