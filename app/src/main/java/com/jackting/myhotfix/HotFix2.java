package com.jackting.myhotfix;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class HotFix2 {
    private String apkPath = "";

    public void fix(Context context){
        try {
            Class<?> baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoader.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListField = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListField.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            DexClassLoader dexClassLoader = new DexClassLoader(apkPath,context.getCacheDir().getAbsolutePath(),
                    null,context.getClassLoader());
            Object pluginPathList = pathListField.get(dexClassLoader);
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Object hostPathList = pathListField.get(pathClassLoader);
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            Object[] newHostDexElements = (Object[]) Array.newInstance(pluginDexElements.getClass().getComponentType(),
                    pluginDexElements.length + hostDexElements.length);

            System.arraycopy(hostDexElements,0,newHostDexElements,0,newHostDexElements.length);
            System.arraycopy(pluginDexElements,0,newHostDexElements,hostDexElements.length,pluginDexElements.length);

            dexElementsField.set(hostPathList,newHostDexElements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
