package com.jackting.myhotfix.plugin;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.jackting.myhotfix.plugin.ProxyActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtil {

    private static final String TARGET_INTENT="target_intent";

    public static void hookAMS(){
        try {
            Class<?> cls= Class.forName("android.app.ActivityManager");
            Field sigletonField = cls.getDeclaredField("IActivityManagerSingleton");
            sigletonField.setAccessible(true);
            Object singleton = sigletonField.get(null);

            Class<?> singletonClass = Class.forName("android.util.Singleton");
            final Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");

            // IActivityManager的class对象
            Object proxyInstance= Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // args表示参数的集合

                            /**
                             *
                             * Instrumentation
                             * int result = ActivityManager.getService()
                             .startActivity(whoThread, who.getBasePackageName(), intent,
                             intent.resolveTypeIfNeeded(who.getContentResolver()),
                             token, target != null ? target.mEmbeddedID : null,
                             requestCode, 0, null, options);
                             */
                            if("startActivity".equals(method.getName())){

                                int index = 0;

                                //将插件的intent替换为代理的intent
                                for(int i=0;i<args.length;i++){
                                    if(args[i] instanceof Intent){
                                        index = i;
                                        break;
                                    }
                                }
                                Intent intent = (Intent) args[index];

                                //替换成代理的intent
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.jackting.hot",ProxyActivity.class.getName());

                                proxyIntent.putExtra(TARGET_INTENT,intent);
                                args[index] = proxyIntent;
                            }

                            //iActivityManager 对象,通过反射
                            return method.invoke(mInstance,args);
                        }
                    });

            mInstanceField.set(singleton,proxyInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookHandler(){


        try {
            Class<?> clazz= Class.forName("android.app.ActivityThread");
            Field sActivityThreadField = clazz.getField("sCurrentActivityThread");
            sActivityThreadField.setAccessible(true);
            Object activityThread= sActivityThreadField.get(null);

            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Object mH = mHField.get(activityThread);

            Class<?> handlerCls=Class.forName("android.os.Handler");
            Field mCallbackField = handlerCls.getField("mCallback");
            mCallbackField.setAccessible(true);

            // Handler 对象
            //将创建一个Callback替换系统的Callback对象
            mCallbackField.set(mH,new Handler.Callback(){

                @Override
                public boolean handleMessage(Message msg) {
                    // 替换Intent

                    switch (msg.what){
                        //LAUNCH_ACTIVITY时
                        case 100:
                            // ActivityClientRecord == msg.obj
                            try {
                                Field intentField =msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);

                                //插件的intent
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                //判断调用的是否时插件的，如果不是插件的，intent就会为空
                                if(intent != null){
                                    intentField.set(msg.obj,intent);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
