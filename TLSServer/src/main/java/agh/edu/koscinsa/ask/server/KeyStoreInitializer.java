package agh.edu.koscinsa.ask.server;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class KeyStoreInitializer {

    @Bean(name = "caPublicKey")
    public static PublicKey getCaPublicKey() throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSServer/src/main/resources/keystore/ca.public.key.der"));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    @Bean
    public static PrivateKey getPrivateKey()
            throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSServer/src/main/resources/keystore/server.key.der"));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    @Bean(name = "serverPublicKey")
    public static PublicKey getPublicKey()
            throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get("./TLSServer/src/main/resources/keystore/server.public.key.der"));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    @Bean
    public static KeyStore ownKeyStore() {
        KeyStore keyStore = null;

        try {
            Certificate ca = getCertificateFromPath("./TLSServer/src/main/resources/keystore/ca.crt");
            Certificate ownCertificate = getCertificateFromPath("./TLSServer/src/main/resources/keystore/server.crt");

            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            keyStore.setCertificateEntry("server", ownCertificate);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

        } catch (Exception e) {

        }

        return keyStore;
    }

    private static Certificate getCertificateFromPath(String path) throws Exception {
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
