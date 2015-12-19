package com.staticelements.plenum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public abstract class PlenumBaseActivity extends AppCompatActivity {

    private static final String CURRENT_FRAGMENT = "PlenumBaseActivity.CURRENT_FRAGMENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((PlenumApplication) getApplication()).inject(this);

        attachFragment(getFragment());

    }

    private void attachFragment(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    protected abstract Fragment getFragment();
}
