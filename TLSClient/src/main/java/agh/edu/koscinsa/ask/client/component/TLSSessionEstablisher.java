package agh.edu.koscinsa.ask.client.component;

import agh.edu.koscinsa.ask.server.model.Session;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class TLSSessionEstablisher {

    @Value("${tls.server.url}")
    private String url;

    @Value("${tls.algorithmsUsed}")
    private String[] allowedCryptoAlgorithms;

    private Random random = new SecureRandom();

    @Autowired
    private Nitrite nitrite;

    private ObjectRepository<Session> sessionObjectRepository;

    @Bean
    public Session establishSession(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Client Hello", "Client Hello");
        Session response = getRESTResponse(url + "/authorize", headers);

        return response;
    }

    private Session getRESTResponse(String url, MultiValueMap<String, String> params){
        sessionObjectRepository = nitrite.getRepository(Session.class);
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("TLSHeader", "ClientHello");

        Session session = new Session(1, null);
        session.setAllowedCryptoAlgorithms(List.of(allowedCryptoAlgorithms));
        session.setRandomServerNumber(random.nextInt());

        HttpEntity<Session> requestEntity = new HttpEntity<>(session, headers);
        updateOrSaveInRepository(session);

        Session response = null;
        try{
            ResponseEntity<Session> responseEntity = template.exchange(url, HttpMethod.POST, requestEntity,  Session.class);
            response = responseEntity.getBody();
            updateOrSaveInRepository(response);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return response;
    }

    private void updateOrSaveInRepository(Session session){
        sessionObjectRepository.update(session, true);
    }
}
