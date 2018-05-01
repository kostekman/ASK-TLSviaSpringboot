package agh.edu.koscinsa.ask.client.controller;

import org.dizitart.no2.Nitrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

    @Autowired
    private Nitrite db;
}
