package com.staticelements.plenum.presenter;

public interface PlenumConnection {

    void readyForConnection();

    void lostConnection();

    void searchForConnections();

    void connect(String identifier);

    void nearbyConnectionMade();

    void nearbyConnectionLost();
}
