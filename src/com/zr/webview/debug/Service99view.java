package com.zr.webview.debug;
import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.zr.webview.MainActivity;
import com.zr.webview.util.CommUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class Service99view extends Service {  
    private static final String TAG = "Service99view" ;  
    public static final String ACTION = "com.zr.webview.debug.Service99view";  
    private static Logger logger11;
    private static Context context;
    @Override  
    public IBinder onBind(Intent intent) {  
    	logger11.debug("logger11 onBind!!!");
        return null;  
    }  
    @Override  
    public void onDestroy() {  
    	logger11.debug("logger11 onDestroy!!!");
        super.onDestroy();  
    }    
    @Override  
    public void onCreate() {  
    	configLog123(); 
    	logger11.debug("logger11 onCreate!!!");
        super.onCreate();  
    }  
    public  boolean isAppInForeground() {  
    	//context = (Context)MainActivity;
    	String cn="com.zr.webview.MainActivity";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();  
        for (RunningAppProcessInfo appProcess : appProcesses) {  
            if (appProcess.processName.equals(cn)) {  
                return appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;  
            }  
        }  
        return false;  
    }    
    public boolean isForeground(){  
    	  // Get the Activity Manager  
    	  ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);  
    	  String PackageName="com.zr.webview";
    	  // Get a list of running tasks, we are only interested in the last one,  
    	  // the top most so we give a 1 as parameter so we only get the topmost.  
    	  List< ActivityManager.RunningTaskInfo > task = manager.getRunningTasks(1);  
    	   
    	  // Get the info we need for comparison.  
    	  ComponentName componentInfo = task.get(0).topActivity;  
    	   
    	  // Check if it matches our package name.  
    	  if(componentInfo.getPackageName().equals(PackageName)) return true;  
    	       
    	  // If not then our app is not on the foreground.  
    	  return false;  
    	} 
    @Override  
    public void onStart(Intent intent, int startId) {  
        //Log.v(TAG, "ServiceDemo onStart");  
    	context = this;
    	logger11.debug("logger11 onStart!!!");
    	File ffffff = new File(CommUtils.appworkpath+"heartt");
		if (ffffff.exists()){
			ffffff.delete();
		}
    	handler.sendEmptyMessageDelayed(0, 100000);//启动handler，实现4秒定时循环执行  
        super.onStart(intent, startId);  
    }  
    
    private Handler handler = new Handler(){  
    public void handleMessage(android.os.Message msg) {
    	
    	//boolean ret=isAppInForeground();
    	File ffffff = new File(CommUtils.appworkpath+"heartt");
    	boolean ret=false;
		if (ffffff.exists()){
			ret=true;
			ffffff.delete();
		}else{
			ret=false;
			Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
        	mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	context.startActivity(mainActivityIntent);
        	logger11.debug("logger11 startActivity");
		}
    	logger11.debug("logger11 handleMessage:ret:"+ret);
    	//logger11.debug("logger11 isForeground:"+isForeground());
    	handler.sendEmptyMessageDelayed(0,120000);//4秒后再次执行  
      }  
    }; 
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
    	logger11.debug("logger11 onStartCommand!!!");
        return super.onStartCommand(intent, flags, startId);  
    }  
    public void configLog123()  
    {  
        final LogConfigurator logConfigurator = new LogConfigurator();  
          
        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator +"99view"+ File.separator +"log"+ File.separator + "Service99view.log");  
        // Set the root log level  
        logConfigurator.setRootLevel(Level.ALL);  
        // Set log level of a specific logger  
        logConfigurator.setLevel("org.apache", Level.ERROR);  
        
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //logConfigurator.setFilePattern("%d %p\#%L\#%c\.%M\#%t\#%m%n");
        logConfigurator.setMaxFileSize(1024*5*1024);  
        logConfigurator.setImmediateFlush(true); 
        
        logConfigurator.configure();  

        logger11 = Logger.getLogger(Service99view.class);  
        logger11.debug("logger11 test!!!");
        
    }
}  