package agh.edu.koscinsa.ask.server.model;

import org.dizitart.no2.objects.Id;

import java.security.Key;
import java.util.List;

public class Session {
    @Id
    private int sessionID;
    private int randomClientNumber;
    private int randomServerNumber;
    private List<String> allowedCryptoAlgorithms;
    private String usedAlgorithm;
    private Key publicKey;

    public Session() {
    }

    public Session(int sessionID, Key publicKey) {
        this.sessionID = sessionID;
        this.publicKey = publicKey;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    public int getRandomClientNumber() {
        return randomClientNumber;
    }

    public void setRandomClientNumber(int randomClientNumber) {
        this.randomClientNumber = randomClientNumber;
    }

    public int getRandomServerNumber() {
        return randomServerNumber;
    }

    public void setRandomServerNumber(int randomServerNumber) {
        this.randomServerNumber = randomServerNumber;
    }

    public List<String> getAllowedCryptoAlgorithms() {
        return allowedCryptoAlgorithms;
    }

    public void setAllowedCryptoAlgorithms(List<String> allowedCryptoAlgorithms) {
        this.allowedCryptoAlgorithms = allowedCryptoAlgorithms;
    }

    public String getUsedAlgorithm() {
        return usedAlgorithm;
    }

    public void setUsedAlgorithm(String usedAlgorithm) {
        this.usedAlgorithm = usedAlgorithm;
    }
}
