package com.staticelements.plenum;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.staticelements.plenum.nearby.NearbySources;
import com.staticelements.plenum.presenter.PlenumConnectionPresenter;
import com.staticelements.plenum.view.ActiveMeetingView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchForConnectionsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActiveMeetingView {

    private static final String TAG = "SearchFragment";
    private static final int REQUEST_RESOLVE_ERROR = 0x01;
    private boolean resolvingError;

    @Bind(R.id.current_status)
    TextView statusTextView;

    @Bind(R.id.join_event)
    Button joinEvent;

    @Bind(R.id.start_event)
    Button startEvent;

    @Inject
    GoogleApiClient.Builder googleApiBuilder;
    private GoogleApiClient googleApi;

    private static final Message connectedMessage = new Message("Connected".getBytes());
    private static final Message disconnectedMessage = new Message("Disconnected".getBytes());

    private PlenumConnectionPresenter plenumConnection;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApi = googleApiBuilder
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        plenumConnection = new PlenumConnectionPresenter(this, new NearbySources());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);

        ButterKnife.bind(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!googleApi.isConnected()) {
            googleApi.connect();
        }
    }

    @Override
    public void onStop() {
        if (googleApi.isConnected()) {
            // Clean up when the user leaves the activity.
            Nearby.Messages.unpublish(googleApi, disconnectedMessage)
                    .setResultCallback(new ErrorCheckingCallback("unpublish()"));
            Nearby.Messages.unsubscribe(googleApi, messageListener)
                    .setResultCallback(new ErrorCheckingCallback("unsubscribe()"));
        }
        googleApi.disconnect();
        super.onStop();
    }

    // GoogleApiClient connection callback.
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected: " + connectionHint);
        Nearby.Messages.getPermissionStatus(googleApi).setResultCallback(
                new ErrorCheckingCallback("getPermissionStatus", new Runnable() {
                    @Override
                    public void run() {
                        plenumConnection.readyForConnection();
                    }
                })
        );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @OnClick(R.id.join_event)
    public void joinEvent() {
        Nearby.Messages.subscribe(googleApi, messageListener)
                .setResultCallback(new ErrorCheckingCallback("subscribe()"));
    }

    @OnClick(R.id.start_event)
    public void startEvent() {
        Nearby.Messages.publish(googleApi, connectedMessage)
                .setResultCallback(new ErrorCheckingCallback("publish()"));
    }

    @Override
    public void showStatus(final String statusMessage) {
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
                statusTextView.setText(statusMessage);
//            }
//        };
//
//        handler.post(r);
    }

    @Override
    public void showActiveConnections(List<String> connections) {

    }

    @Override
    public void enable() {
        joinEvent.setEnabled(true);
        startEvent.setEnabled(true);
    }

    @Override
    public void disable() {
        joinEvent.setEnabled(false);
        startEvent.setEnabled(false);
    }

    @Override
    public void showConnectedScreen() {

    }

    @Override
    public void showMainScreen() {

    }

    /**
     * A simple ResultCallback that logs when errors occur.
     * It also displays the Nearby opt-in dialog when necessary.
     */
    private class ErrorCheckingCallback implements ResultCallback<Status> {
        private final String method;
        private final Runnable runOnSuccess;

        private ErrorCheckingCallback(String method) {
            this(method, null);
        }

        private ErrorCheckingCallback(String method, @Nullable Runnable runOnSuccess) {
            this.method = method;
            this.runOnSuccess = runOnSuccess;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, method + " succeeded.");
                if (runOnSuccess != null) {
                    runOnSuccess.run();
                }
            } else {
                // Currently, the only resolvable error is that the device is not opted
                // in to Nearby. Starting the resolution displays an opt-in dialog.
                if (status.hasResolution()) {
                    if (!resolvingError) {
                        try {
                            status.startResolutionForResult(getActivity(),
                                    REQUEST_RESOLVE_ERROR);
                            resolvingError = true;
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, method + " failed with exception: " + e);
                        }
                    } else {
                        // This will be encountered on initial startup because we do
                        // both publish and subscribe together.
                        Log.i(TAG, method + " failed with status: " + status
                                + " while resolving error.");
                    }
                } else {
                    Log.e(TAG, method + " failed with : " + status
                            + " resolving error: " + resolvingError);
                }
            }
        }
    }

    // This is called in response to a button tap in the Nearby permission dialog.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                // Permission granted or error resolved successfully then we proceed
                // with publish and subscribe..
                plenumConnection.readyForConnection();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Log.i(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
    }

    private final MessageListener messageListener = new MessageListener() {
        @Override
        public void onLost(Message msg) {
            plenumConnection.nearbyConnectionLost();
        }

        @Override
        public void onFound(final Message msg) {
            plenumConnection.nearbyConnectionMade();
        }
    };

}
