package agh.edu.koscinsa.ask.server.controller;

import agh.edu.koscinsa.ask.server.KeyStoreInitializer;
import agh.edu.koscinsa.ask.server.component.DESUtil;
import agh.edu.koscinsa.ask.server.component.JSONConverter;
import agh.edu.koscinsa.ask.server.component.RequestProcessor;
import agh.edu.koscinsa.ask.server.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Base64;

import static java.util.Base64.getDecoder;

@RestController
@Import(KeyStoreInitializer.class)
public class AuthorizationController {
    private Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    private final Nitrite nitrite;
    private final RequestProcessor processor;

    private final KeyStore ownKeyStore;

    @Autowired
    public AuthorizationController(RequestProcessor processor, KeyStore ownKeyStore, Nitrite nitrite) {
        this.processor = processor;
        this.ownKeyStore = ownKeyStore;
        this.nitrite = nitrite;
    }

    @PostMapping(value = "/authorize", produces = "application/json")
    public ResponseEntity<Session> authorize(@RequestBody Session body, @RequestHeader MultiValueMap<String, String> headers) throws Exception {
        logger.info(body.toString());
        ResponseEntity<Session> responseEntity = processor.processAndCreateResponse(headers.get("tlsheader"), body, ownKeyStore);

        return responseEntity;
    }

    @PostMapping(value = "/authorize/encrypted", produces = "application/json")
    public ResponseEntity<Session> authorizeEncrypted(@RequestBody Session body, @RequestHeader MultiValueMap<String, String> headers) throws Exception {
        logger.info(body.toString());

        if(decodeSessionFromStringUsingKey(body.getConfirmMessage(), ownKeyStore.getCertificate("client").getPublicKey())
                .equals(body.getAllowedCryptoAlgorithms().get(0))){
            ResponseEntity<Session> responseEntity = processor.processAndCreateResponse(headers.get("tlsheader"), body, ownKeyStore);

            return responseEntity;
        }

        return null;
    }

    @PostMapping(value = "/message", produces = "application/json")
    public ResponseEntity<String> readAndSendMessage(@RequestBody String body, @RequestHeader MultiValueMap<String, String> headers) throws Exception {


        if(headers.containsKey("session")){
            int sessionId = Integer.parseInt(headers.getFirst("session"));
            Session session = nitrite.getRepository(Session.class).find().toList().stream().filter(s -> s.getSessionID() == sessionId).findFirst().get();

            logger.info(DESUtil.decrypt(body, Base64.getDecoder().decode(session.getSecretKey().getBytes())));

            return ResponseEntity.ok(DESUtil.encrypt("Ping", Base64.getDecoder().decode(session.getSecretKey().getBytes())));
        }


        return null;
    }

    private String decodeSessionFromStringUsingKey(String encoded, Key key) throws Exception{
        //bytes decoded from Base64 string encoded with RSA
        byte[] encodedSession = Base64.getDecoder().decode(encoded);

        Cipher decrypt=Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
        decrypt.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessage = decrypt.doFinal(encodedSession);



        return new String(decryptedMessage);
    }

}
