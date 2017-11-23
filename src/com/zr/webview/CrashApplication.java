package com.zr.webview;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
/**
* @ClassName: CrashApplication
* @Description: 全局处理activity生命周期的应用类
* @author xwl
* @date 2014-3-4
* tags
*/
public class CrashApplication extends Application {
	ArrayList<Activity> list = new ArrayList<Activity>();  
	private static CrashApplication singleton;
	public void init() {
		UnCeHandler catchExcep = new UnCeHandler(this);  
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);   
	}
	 @Override
    public void onCreate()
    {
        super.onCreate();
        singleton = this;
    }
	// Returns the application instance
    public static CrashApplication getInstance() {
        return singleton;
    }
	/** 
     * Activity关闭时，删除Activity列表中的Activity对象*/  
    public void removeActivity(Activity a){  
        list.remove(a);  
    }  
      
    /** 
     * 向Activity列表中添加Activity对象*/  
    public void addActivity(Activity a){  
        list.add(a);  
    }  
      
	 /** 
     * 关闭Activity列表中的所有Activity*/  
    public void finishActivity(){  
        for (Activity activity : list) {    
            if (null != activity) {    
                activity.finish();    
            }    
        }  
        //杀死该应用进程  
       android.os.Process.killProcess(android.os.Process.myPid());    
    } 
}
