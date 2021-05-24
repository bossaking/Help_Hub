package com.example.help_hub.OtherClasses;

import android.annotation.SuppressLint;
import android.content.Context;

public class MyApplication {

    public Context currentActivity = null;
    public Context getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Context currentActivity) {
        this.currentActivity = currentActivity;
    }

    @SuppressLint("StaticFieldLeak")
    public static MyApplication instance;

    public static MyApplication getInstance(){
        if(instance == null){
            instance = new MyApplication();
        }

        return instance;
    }

}
