package com.staticelements.plenum;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

public class PlenumActivity extends PlenumBaseActivity  {
    private static final String TAG = "PlenumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    protected Fragment getFragment() {
        return new SearchForConnectionsFragment();
    }

}
