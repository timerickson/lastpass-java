package com.in2bits.debug;

import com.in2bits.FetchException;
import com.in2bits.LoginException;
import com.in2bits.ParseException;
import com.in2bits.Vault;
import com.in2bits.adapters.crypto.AesManagedFactory;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.WebClientFactory;

import static com.in2bits.Vault.Create;

/**
 * Created by Tim on 7/22/17.
 */

public class Driver {
    public static void main(String[] args) {
        Ioc ioc = new SimpleIoc();
        ioc.register(WebClientFactory.class, new SimpleWebClientFactory());
        ioc.register(AesManagedFactory.class, new SimpleAesManagedFactory());
        String username = "yourlastpassemail@domain.tld";
        String password = "your master password";
        String otp = "123456";
        try {
            Vault vault = Create(ioc, username, password, otp);
            int i = 42;
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (FetchException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
