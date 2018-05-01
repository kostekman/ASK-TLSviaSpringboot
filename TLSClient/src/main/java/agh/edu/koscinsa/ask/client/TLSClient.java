package agh.edu.koscinsa.ask.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TLSClient {

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        SpringApplication.run(TLSClient.class);
    }
}
