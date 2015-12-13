package com.staticelements.plenum;

import android.app.Application;

import com.staticelements.plenum.module.AndroidModule;
import com.staticelements.plenum.module.ApplicationModule;

import dagger.ObjectGraph;
import java.util.Arrays;
import java.util.List;

public class PlenumApplication extends Application {
    private ObjectGraph graph;

    @Override public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.asList(
                new AndroidModule(this),
                new ApplicationModule()
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}