package com.jackting.myhotfix;

import android.app.Application;

public class AndroidApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        HotFix.fix(this);
    }



}
