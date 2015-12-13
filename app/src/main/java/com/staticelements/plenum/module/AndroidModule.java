package com.staticelements.plenum.module;

import android.content.Context;

import com.staticelements.plenum.PlenumApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true)
public class AndroidModule {

    private final PlenumApplication plenumApplication;

    public AndroidModule(PlenumApplication plenumApplication) {
        this.plenumApplication = plenumApplication;
    }

    @Provides
    @Singleton
    @ForApplication
    Context providesApplicationContext() {
        return plenumApplication;
    }
}
