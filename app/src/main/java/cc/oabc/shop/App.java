package cc.oabc.shop;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;

import java.sql.Time;

import cc.oabc.shop.util.CLog;

public class App extends Application {
    private static App INSTANCE;
    private static Context CONTEXT;
    private static Long startTime;

    public static App getInstance() {
        return INSTANCE;
    }
    public static Context getContext() {
        return CONTEXT;
    }
    public static Boolean logged=false;
    public static Integer selectAddressId;
    public static String selectTime;
    public static Boolean newOrderSuccess=false;

    @Override
    public void onCreate() {
        startTime = SystemClock.elapsedRealtime();
        super.onCreate();
        CONTEXT = getApplicationContext();
        INSTANCE = this;
    }

    public static String pastTime(String str){
        Long time = SystemClock.elapsedRealtime()-startTime;
        return time.toString()+"："+str;
    }
}
