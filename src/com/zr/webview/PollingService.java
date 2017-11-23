package com.zr.webview;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class PollingService extends Service {

	public static final String ACTION = "com.ryantang.service.PollingService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Toast.makeText(getApplicationContext(), "闹铃响了, 可以做点事情了~~", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("Service:onDestroy");
	}

}
