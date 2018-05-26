package agh.edu.koscinsa.ask.server.model;

import org.dizitart.no2.objects.Id;

import java.util.List;

public class Session {
    @Id
    private int sessionID;
    private int randomClientNumber;
    private int randomServerNumber;
    private List<String> allowedCryptoAlgorithms;
    private String usedAlgorithm;
    private String clientCertificate;
    private String serverCertificate;
    private String confirmMessage;
    private String secretKey;

    private String clientPublicKey;
    private String serverPublicKey;

    public Session() {
    }

    public Session(int sessionID) {
        this.sessionID = sessionID;
    }

    public Session(int sessionID, int randomClientNumber, int randomServerNumber, List<String> allowedCryptoAlgorithms, String usedAlgorithm, String clientCertificate, String serverCertificate) {
        this.sessionID = sessionID;
        this.randomClientNumber = randomClientNumber;
        this.randomServerNumber = randomServerNumber;
        this.allowedCryptoAlgorithms = allowedCryptoAlgorithms;
        this.usedAlgorithm = usedAlgorithm;
        this.clientCertificate = clientCertificate;
        this.serverCertificate = serverCertificate;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
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

    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public String getServerCertificate() {
        return serverCertificate;
    }

    public void setServerCertificate(String serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public String getClientPublicKey() {
        return clientPublicKey;
    }

    public void setClientPublicKey(String clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        return "Session{" +
                "\nsessionID=" + sessionID +
                ",\n randomClientNumber=" + randomClientNumber +
                ",\n randomServerNumber=" + randomServerNumber +
                ",\n allowedCryptoAlgorithms=" + allowedCryptoAlgorithms +
                ",\n usedAlgorithm='" + usedAlgorithm + '\'' +
                ",\n clientCertificate='" + clientCertificate + '\'' +
                ",\n serverCertificate='" + serverCertificate + '\'' +
                ",\n clientPublicKey='" + clientPublicKey + '\'' +
                ",\n serverPublicKey='" + serverPublicKey + '\'' +
                '}';
    }
}
