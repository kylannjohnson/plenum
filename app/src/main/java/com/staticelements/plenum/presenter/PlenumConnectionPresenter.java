package com.staticelements.plenum.presenter;

import com.staticelements.plenum.nearby.NearbySources;
import com.staticelements.plenum.view.ActiveMeetingView;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlenumConnectionPresenter implements PlenumConnection {

    private final ActiveMeetingView view;
    private NearbySources sources;
    private AtomicBoolean readyForConnection = new AtomicBoolean(false);

    public PlenumConnectionPresenter(ActiveMeetingView view, NearbySources sources) {
        this.view = view;
        this.sources = sources;
    }

    @Override
    public void readyForConnection() {
        readyForConnection.set(true);
        view.enable();
    }

    @Override
    public void lostConnection() {
        readyForConnection.set(false);
        view.disable();
    }

    @Override
    public void searchForConnections() {
        view.showActiveConnections(sources.getAllSources());
    }

    @Override
    public void connect(String identifier) {

    }

    @Override
    public void nearbyConnectionMade() {
        view.showStatus("CONNECTED");
    }

    @Override
    public void nearbyConnectionLost() {
        view.showStatus("--");
    }

}
