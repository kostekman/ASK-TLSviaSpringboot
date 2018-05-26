package agh.edu.koscinsa.ask.client.component;

import agh.edu.koscinsa.ask.server.component.DESUtil;
import agh.edu.koscinsa.ask.server.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import okhttp3.TlsVersion;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Component
public class TLSSessionEstablisher {
    Logger logger = LoggerFactory.getLogger(TLSSessionEstablisher.class);
    @Value("${tls.server.url}")
    private String url;

    @Value("${tls.algorithmsUsed}")
    private String[] allowedCryptoAlgorithms;

    private Random random = new SecureRandom();

    private final Nitrite nitrite;
    private final KeyStore keyStore;
    private final PublicKey clientPublicKey;
    private final PrivateKey clientPrivateKey;
    private final PublicKey caPublicKey;

    private ObjectRepository<Session> sessionObjectRepository;

    private final ObjectMapper objectMapper;

    private DESKeySpec desKeySpec;

    @Autowired
    public TLSSessionEstablisher(Nitrite nitrite, KeyStore ownKeyStore, PublicKey clientPublicKey, PrivateKey clientPrivateKey, ObjectMapper objectMapper, PublicKey caPublicKey) {
        this.nitrite = nitrite;
        this.keyStore = ownKeyStore;
        this.clientPublicKey = clientPublicKey;
        this.clientPrivateKey = clientPrivateKey;
        this.objectMapper = objectMapper;
        this.caPublicKey = caPublicKey;
    }

    @Bean
    public Session establishSession(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Client Hello", "Client Hello");
        Session response = getRESTResponse(url + "/authorize", headers);
        logger.info(response.toString());

        return response;
    }

    private Session getRESTResponse(String url, MultiValueMap<String, String> params){
        sessionObjectRepository = nitrite.getRepository(Session.class);
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("TLSHeader", "ClientHello");

        Session session = new Session(1);
        session.setAllowedCryptoAlgorithms(List.of(allowedCryptoAlgorithms));
        session.setRandomClientNumber(random.nextInt());

        HttpEntity<Session> requestEntity = new HttpEntity<>(session, headers);
        updateOrSaveInRepository(session);

        Session response = null;

        try{
            ResponseEntity<Session> responseEntity = template.exchange(url, HttpMethod.POST, requestEntity,  Session.class);
            response = responseEntity.getBody();
            updateOrSaveInRepository(response);

            Certificate serverCertificate = KeyStoreInitializer.getCertificateFromEncodedString(response.getServerCertificate());

            //serverCertificate.verify(caPublicKey);

            headers.clear();
            headers.add("TLSHeader", "Certificate");
            headers.add("TLSHeader", "ClientKeyExchange");
            headers.add("TLSHeader", "ChangeCipherSpec");
            headers.add("TLSHeader", "Finished");
            response.setClientCertificate(Base64.getEncoder().encodeToString(keyStore.getCertificate("client").getEncoded()));
            response.setClientPublicKey(Base64.getEncoder().encodeToString(clientPublicKey.getEncoded()));
            requestEntity = new HttpEntity<>(response, headers);
            responseEntity = template.exchange(url, HttpMethod.POST, requestEntity, Session.class);
            response = responseEntity.getBody();

            headers.clear();
            headers.set("TLSHeader", "CertificateVerify");

            Cipher encrypt=Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            encrypt.init(Cipher.ENCRYPT_MODE, clientPrivateKey);
            byte[] encryptedSession = encrypt.doFinal(response.getAllowedCryptoAlgorithms().get(0).getBytes());
            String baseEncodedSession = Base64.getEncoder().encodeToString(encryptedSession);
            response.setConfirmMessage(baseEncodedSession);
            requestEntity = new HttpEntity<>(response, headers);
            responseEntity = template.exchange(url + "/encrypted", HttpMethod.POST, requestEntity, Session.class);

            if(responseEntity.getStatusCode().equals(HttpStatus.OK)){
                headers.clear();
                headers.add("TLSHeader", "ClientKeyExchange");
                headers.add("TLSHeader", "ChangeCipherSpec");
                headers.add("TLSHeader", "Finished");

                Long secretLongKey = random.nextLong();
                ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
                byteBuffer.putLong(secretLongKey);
                byte[] secretByteKey = byteBuffer.array();

                response.setSecretKey(Base64.getEncoder().encodeToString(secretByteKey));
                nitrite.getRepository(Session.class).update(response, true);

                encrypt=Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
                encrypt.init(Cipher.ENCRYPT_MODE, serverCertificate.getPublicKey());

                secretByteKey = encrypt.doFinal(secretByteKey);
                response.setSecretKey(Base64.getEncoder().encodeToString(secretByteKey));

                requestEntity = new HttpEntity<>(response, headers);
                template.exchange(url, HttpMethod.POST, requestEntity, Session.class);

                headers.clear();
                headers.add("session", Integer.valueOf(response.getSessionID()).toString());

                Session finalResponse = response;
                response = nitrite.getRepository(Session.class).find().toList().stream().filter(s -> s.getSessionID() == finalResponse.getSessionID()).findFirst().get();

                ResponseEntity<String> messageEntity = template.exchange(url.replace("/authorize", "/message"), HttpMethod.POST, new HttpEntity<>(DESUtil.encrypt("Ping", Base64.getDecoder().decode(response.getSecretKey().getBytes())), headers), String.class);

                logger.info(DESUtil.decrypt(messageEntity.getBody(), Base64.getDecoder().decode(response.getSecretKey().getBytes())));
            }

        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }


        return response;
    }

    private void sendCertificate(HttpHeaders headers, Session response, ResponseEntity<Session> responseEntity, HttpEntity<Session> requestEntity, RestTemplate template) throws Exception{
        headers.clear();
        headers.add("TLSHeader", "Certificate");
        headers.add("TLSHeader", "ClientKeyExchange");
        headers.add("TLSHeader", "ChangeCipherSpec");
        headers.add("TLSHeader", "Finished");
        response.setClientCertificate(Base64.getEncoder().encodeToString(keyStore.getCertificate("client").getEncoded()));
        response.setClientPublicKey(Base64.getEncoder().encodeToString(clientPublicKey.getEncoded()));
        requestEntity = new HttpEntity<>(response, headers);
        responseEntity = template.exchange(url, HttpMethod.POST, requestEntity, Session.class);
        response = responseEntity.getBody();
    }

    private void updateOrSaveInRepository(Session session){
        sessionObjectRepository.update(session, true);
    }
}
