package agh.edu.koscinsa.ask.client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class TLSClient {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        System.setProperty("javax.net.ssl.keyStorePassword", "test");
        System.out.println(System.getProperty("user.dir"));
        SpringApplication.run(TLSClient.class);
    }
}
