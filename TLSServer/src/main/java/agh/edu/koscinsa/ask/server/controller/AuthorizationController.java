package agh.edu.koscinsa.ask.server.controller;

import agh.edu.koscinsa.ask.server.component.RequestProcessor;
import agh.edu.koscinsa.ask.server.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthorizationController {

    @Autowired
    private RequestProcessor processor;

    @PostMapping(value = "/authorize", produces = "application/json")
    public ResponseEntity<Session> authorize(@RequestBody Session body, @RequestHeader MultiValueMap<String, String> headers){
        ResponseEntity<Session> responseEntity = processor.processAndCreateResponse(headers.get("tlsheader"), body);

        return responseEntity;
    }
}
