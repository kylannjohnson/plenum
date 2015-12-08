package com.staticelements.plenum.presenter;

import com.staticelements.plenum.nearby.NearbySources;
import com.staticelements.plenum.view.ActiveMeetingView;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlenumConnectionPresenterTest {

    private static final List<String> ACTIVE_CONNECTIONS = Arrays.asList("One", "Two");

    @Mock
    private ActiveMeetingView view;

    @Mock
    private NearbySources sources;

    private PlenumConnectionPresenter plenumPresenter;

    @Before
    public void setupPresenter() throws Exception {

        MockitoAnnotations.initMocks(this);

        plenumPresenter = new PlenumConnectionPresenter(view, sources);
    }

    @Test
    public void testReadyForConnection_enablesView() throws Exception {

        plenumPresenter.readyForConnection();

        verify(view).enable();
    }

    @Test
    public void testLostConnection_disablesView() throws Exception {

        plenumPresenter.lostConnection();

        verify(view).disable();
    }

    @Test
    public void testGetActiveConnections_showsConnectionList() throws Exception {
        when(sources.getAllSources()).thenReturn(ACTIVE_CONNECTIONS);

        plenumPresenter.searchForConnections();

        verify(view).showActiveConnections(ACTIVE_CONNECTIONS);
    }

    public void testConnect() throws Exception {

    }

    public void testConnectionMade() throws Exception {

    }

    public void testConnectionLost() throws Exception {

    }
}