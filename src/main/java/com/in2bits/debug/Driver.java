package com.in2bits.debug;

import com.in2bits.Blob;
import com.in2bits.FetchException;
import com.in2bits.LoginException;
import com.in2bits.ParseException;
import com.in2bits.Vault;
import com.in2bits.adapters.crypto.AesManagedFactory;
import com.in2bits.shims.Ioc;
import com.in2bits.shims.WebClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

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
//            Vault vault = Create(ioc, username, password, otp);
//            downloadBlob(ioc, username, password, otp);
            Blob blob = loadBlob(ioc, 5000);
            Vault vault = Create(ioc, blob, username, password);
            int i = 42;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Blob loadBlob(Ioc ioc, int iterationCount) throws Exception {
        byte[] array = Files.readAllBytes(new File("tim.lp").toPath());
        return new Blob(ioc, array, iterationCount);
    }

    private static void downloadBlob(Ioc ioc, String username, String password, String otp) throws Exception {
        Blob blob = Vault.Download(ioc, username, password, otp);
        try (FileOutputStream fout = new FileOutputStream("tim.lp", false)) {
            fout.write(blob.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
