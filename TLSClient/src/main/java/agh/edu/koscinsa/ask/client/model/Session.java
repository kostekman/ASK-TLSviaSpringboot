package agh.edu.koscinsa.ask.client.model;

import java.security.Key;

public class Session {
    private int sessionID;
    private Key publicKey;


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
}
