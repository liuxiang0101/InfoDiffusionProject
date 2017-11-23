package com.zr.webview.debug;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.zr.webview.play.VideoView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * @author Hannes Dorfmann
 */
public class Service99viewclass {
	private static Service99viewclass INSTANCE;
	private static Logger logger11;
  private Service99viewclass(Context context) {
    Intent intent = new Intent(context, Service99view.class);
    configLog();
    ServiceConnection serviceConnection = new ServiceConnection() {
      @Override public void onServiceConnected(ComponentName name, IBinder binder) {
        DebugOverlayService service =
            ((DebugOverlayService.DebugOverlayServiceBinder) binder).getService();
      }

      @Override public void onServiceDisconnected(ComponentName name) {
        INSTANCE = null;
      }
    };
    boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    if (!bound) {
      throw new RuntimeException(
          "Could not bind the Service " + Service99view.class.getSimpleName()
              + " -  Is Service declared in Android manifest and is Permission SYSTEM_ALERT_WINDOW granted?");
    }
  }

  public static Service99viewclass with(Context context) {
    if (INSTANCE == null) {
      INSTANCE = new Service99viewclass(context.getApplicationContext());
    }

    return INSTANCE;
  }

  public void destroyView(){

  }
  
  public void configLog()  
  {  
      final LogConfigurator logConfigurator = new LogConfigurator();  
        
      logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator +"99view"+ File.separator + "Service99viewclass.log");  
      // Set the root log level  
      logConfigurator.setRootLevel(Level.ALL);  
      // Set log level of a specific logger  
      logConfigurator.setLevel("org.apache", Level.ERROR);  
      
      logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
      //logConfigurator.setFilePattern("%d %p\#%L\#%c\.%M\#%t\#%m%n");
      logConfigurator.setMaxFileSize(1024*5*1024);  
      logConfigurator.setImmediateFlush(true); 
      
      logConfigurator.configure();  

      logger11 = Logger.getLogger(Service99viewclass.class);  
      logger11.debug("logger11 test!!!");
      
  }
}