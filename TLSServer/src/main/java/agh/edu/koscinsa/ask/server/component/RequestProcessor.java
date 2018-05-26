package agh.edu.koscinsa.ask.server.component;

import agh.edu.koscinsa.ask.server.KeyStoreInitializer;
import agh.edu.koscinsa.ask.server.model.Session;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Component
public class RequestProcessor {
    private final ObjectMapper objectMapper;


    private Random random = new SecureRandom();

    private final Nitrite nitrite;

    private final PublicKey serverPublicKey;
    private final PrivateKey serverPrivateKey;
    private final KeyStore ownKeyStore;

    @Autowired
    public RequestProcessor(Nitrite nitrite, ObjectMapper objectMapper, PublicKey serverPublicKey, PrivateKey serverPrivateKey, KeyStore ownKeyStore) {
        this.nitrite = nitrite;
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        this.objectMapper = objectMapper;
        this.serverPublicKey = serverPublicKey;
        this.serverPrivateKey = serverPrivateKey;
        this.ownKeyStore = ownKeyStore;
    }

    public ResponseEntity<Session> processAndCreateResponse(List<String> header, Session body, KeyStore ownKeyStore) throws Exception {
        ObjectRepository<Session> objectRepository = nitrite.getRepository(Session.class);
        if(header.contains("ClientHello")){
            body.setUsedAlgorithm(
                    body.getAllowedCryptoAlgorithms().get(
                            Math.abs(random.nextInt()) % body.getAllowedCryptoAlgorithms().size()));

            body.setRandomServerNumber(random.nextInt());

            body.setServerCertificate(Base64.getEncoder().encodeToString(ownKeyStore.getCertificate("server").getEncoded()));
            body.setServerPublicKey(Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("TLSHeader", "ServerHello");
            headers.add("TLSHeader", "Certificate");
            headers.add("TLSHeader", "ServerKeyExchange");
            headers.add("TLSHeader", "ServerHelloDone");
            headers.add("TLSHeader", "CertificateRequest");

            objectRepository.update(body, true);

            return ResponseEntity.ok().headers(headers).body(body);
        } else if(header.contains("Certificate")) {
            ownKeyStore.setCertificateEntry("client", KeyStoreInitializer.getCertificateFromEncodedString(body.getClientCertificate()));
            objectRepository.update(body, true);
            return ResponseEntity.ok(body);
        } else if(header.contains("ClientKeyExchange")) {
            String secretKey = body.getSecretKey();
            byte[] secretBytesKey = Base64.getDecoder().decode(secretKey.getBytes());

            Cipher decrypt=Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            decrypt.init(Cipher.DECRYPT_MODE, serverPrivateKey);
            secretBytesKey = decrypt.doFinal(secretBytesKey);

            body.setSecretKey(Base64.getEncoder().encodeToString(secretBytesKey));
            objectRepository.update(body, true);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("TLSHeader", "ChangeCipherSpec");
            headers.add("TLSHeader", "Finished");

            return ResponseEntity.ok().headers(headers).body(body);
        }

        return null;
    }
}
