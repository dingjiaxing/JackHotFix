package com.jackting.myhotfix;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class HotFix {
    private final static String apkPath = "/sdcard/plugin-debug.apk";

    public static void fix(Context context){
        try {
            Class<?> classLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field  pathListField = classLoader.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            DexClassLoader dexClassLoader = new DexClassLoader(apkPath,context.getCacheDir().getAbsolutePath(),
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
}
