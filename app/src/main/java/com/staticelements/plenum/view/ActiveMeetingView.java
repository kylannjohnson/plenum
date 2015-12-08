package com.staticelements.plenum.view;

import java.util.List;

public interface ActiveMeetingView {
    void showStatus(String statusMessage);

    void showActiveConnections(List<String> connections);

    void enable();

    void disable();

    void showConnectedScreen();

    void showMainScreen();
}
