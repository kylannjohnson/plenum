package com.staticelements.plenum.module;

import android.content.Context;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.staticelements.plenum.PlenumActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = false,
        injects = {GoogleApiClient.Builder.class,
                PlenumActivity.class},
        complete = false)
public class ApplicationModule {

    @Provides
    @Singleton
    GoogleApiClient.Builder googleApiClient(@ForApplication Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Nearby.MESSAGES_API)
                .addApi(AppIndex.API);
    }
}
