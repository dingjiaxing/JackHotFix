package com.jackting.myhotfix.plugin;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;

import com.jackting.myhotfix.AndroidApplication;

public class PluginApplication extends AndroidApplication{

    static Resources pluginResource = null;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            LoadUtil.loadClass(this);
        }
        pluginResource = LoadUtil.loadResource(this);

        HookUtil.hookAMS();
        HookUtil.hookHandler();
    }

    @Override
    public Resources getResources() {
        return pluginResource;
    }
}
