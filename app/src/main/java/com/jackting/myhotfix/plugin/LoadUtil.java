package com.jackting.myhotfix.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class LoadUtil {

    public static final String APK_PATH = "";

    public static void loadClass(Context context){

    }

    public static Resources loadResource(Context context){
        //android 26
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath",String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager,APK_PATH);

            Resources systemResource = context.getResources();
            return new Resources(assetManager,systemResource.getDisplayMetrics(),systemResource.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
