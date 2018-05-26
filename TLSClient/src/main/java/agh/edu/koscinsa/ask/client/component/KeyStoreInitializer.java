package agh.edu.koscinsa.ask.client.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class KeyStoreInitializer {

    @Bean(name = "caPublicKey")
    public static PublicKey getCaPublicKey() throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSClient/src/main/resources/keystore/ca.public.key.der"));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }


    @Bean
    public static PrivateKey getPrivateKey()
            throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSClient/src/main/resources/keystore/client.key.der"));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    @Bean(name = "clientPublicKey")
    public static PublicKey getPublicKey()
            throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSClient/src/main/resources/keystore/client.public.key.der"));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }


    @Bean
    public KeyStore produceKeyStore() {
        KeyStore keyStore = null;

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)


            // Create a KeyStore containing our trusted CAs
            Certificate ca = getCertificateFromPath("./TLSClient/src/main/resources/keystore/ca.crt");
            Certificate ownCertificate = getCertificateFromPath("./TLSClient/src/main/resources/keystore/client.crt");

            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            keyStore.setCertificateEntry("client", ownCertificate);

            // Create a TrustManager that trusts the CAs in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SHA1WithRSASignature");
            tmf.init(keyStore);

        } catch (Exception e) {

        }

        return keyStore;
    }

    public static boolean validateCertificate(String certificate, KeyStore keyStore) {
        Certificate serverCertificate = null;
        try {
            serverCertificate = getCertificateFromEncodedString(certificate);
            Certificate caCertificate = keyStore.getCertificate("ca");

            /*Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(serverCertificate.getPublicKey());

            sig.verify(caCertificate.getEncoded());

            serverCertificate.verify(caCertificate.getPublicKey());*/

        } catch (Exception e) {
            e.printStackTrace();
        }



        return true;
    }

    private Certificate getCertificateFromPath(String path) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new BufferedInputStream(new FileInputStream(path));
        InputStream caInput = new BufferedInputStream(is);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }
        return ca;
    }

    public static Certificate getCertificateFromEncodedString(String certificate) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(certificate);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(decodedBytes));
        InputStream caInput = new BufferedInputStream(is);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
        } finally {
            caInput.close();
        }
        return ca;
    }
}