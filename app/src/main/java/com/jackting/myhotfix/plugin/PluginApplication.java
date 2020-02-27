package com.jackting.myhotfix.plugin;

import android.app.Application;
import android.content.res.Resources;

public class PluginApplication extends Application{

    static Resources pluginResource = null;

    @Override
    public void onCreate() {
        super.onCreate();

        LoadUtil.loadClass(this);
        pluginResource = LoadUtil.loadResource(this);

        HookUtil.hookAMS();
        HookUtil.hookHandler();
    }

    @Override
    public Resources getResources() {
        return pluginResource;
    }
}
