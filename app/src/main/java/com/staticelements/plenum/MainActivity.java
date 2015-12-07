package com.staticelements.plenum;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 0x01;
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApi;
    private boolean resolvingError;

    @Bind(R.id.current_status)
    TextView statusTextView;

    @Bind(R.id.join_event)
    Button joinButton;

    private static final Message connectedMessage = new Message("Connected".getBytes());
    private static final Message disconnectedMessage = new Message("Connected".getBytes());

    private Handler handler;

    private MessageListener messageListener = new MessageListener() {
        @Override
        public void onLost(Message msg) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    statusTextView.setText(new String(disconnectedMessage.getContent()));
                }
            };

            handler.post(r);
        }

        @Override
        public void onFound(final Message msg) {

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    statusTextView.setText(new
                            String(msg.getContent()
                    ));
                }
            };

            handler.post(r);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        handler = new Handler(getMainLooper());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApi = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApi.isConnected()) {
            googleApi.connect();
        }
    }

    @Override
    protected void onStop() {
        if (googleApi.isConnected()) {
            // Clean up when the user leaves the activity.
            Nearby.Messages.unpublish(googleApi, connectedMessage)
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
                        publishAndSubscribe();
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

    // This is called in response to a button tap in the Nearby permission dialog.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == RESULT_OK) {
                // Permission granted or error resolved successfully then we proceed
                // with publish and subscribe..
                publishAndSubscribe();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Log.i(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
    }

    private void publishAndSubscribe() {
        // We automatically subscribe to messages from nearby devices once
        // GoogleApiClient is connected. If we arrive here more than once during
        // an activity's lifetime, we may end up with multiple calls to
        // subscribe(). Repeated subscriptions using the same MessageListener
        // are ignored.
        Nearby.Messages.publish(googleApi, connectedMessage)
                .setResultCallback(new ErrorCheckingCallback("publish()"));
        Nearby.Messages.subscribe(googleApi, messageListener)
                .setResultCallback(new ErrorCheckingCallback("subscribe()"));
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
                            status.startResolutionForResult(MainActivity.this,
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
}
