package com.jackting.myhotfix.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadUtil {

    public static final String APK_PATH = "";

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static void loadClass(Context context){
        try {
            Class<?> classLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = classLoader.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            DexClassLoader dexClassLoader = new DexClassLoader(APK_PATH,context.getCacheDir().getAbsolutePath(),
                    null,context.getClassLoader());
            Object pluginPathList = pathListField.get(dexClassLoader);

            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);


            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();

            Object hostPathList = pathListField.get(pathClassLoader);
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            Object[] dexElements = (Object[]) Array.newInstance(pluginDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements,0,dexElements,0,hostDexElements.length);
            System.arraycopy(pluginDexElements,0,dexElements,hostDexElements.length,pluginDexElements.length);
        } catch (Exception e) {
            e.printStackTrace();
        }


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
