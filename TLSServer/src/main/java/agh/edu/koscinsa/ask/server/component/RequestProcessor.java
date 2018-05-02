package agh.edu.koscinsa.ask.server.component;



import agh.edu.koscinsa.ask.server.model.NitriteUtil;
import agh.edu.koscinsa.ask.server.model.Session;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class RequestProcessor {

    private Random random = new SecureRandom();

    @Autowired
    Nitrite nitrite;

    public ResponseEntity<Session> processAndCreateResponse(List<String> header, Session body) {
        ObjectRepository<Session> objectRepository = nitrite.getRepository(Session.class);
        if(header.contains("ClientHello")){
            body.setUsedAlgorithm(
                    body.getAllowedCryptoAlgorithms().get(
                            Math.abs(random.nextInt()) % body.getAllowedCryptoAlgorithms().size()));

            body.setRandomClientNumber(random.nextInt());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("TLSHeader", "ServerHello");
            headers.add("TLSHeader", "Certificate");
            headers.add("TLSHeader", "ServerKeyExchange");
            headers.add("TLSHeader", "ServerHelloDone");

            return ResponseEntity.ok().headers(headers).body(body);
        }

        return null;
    }
}
