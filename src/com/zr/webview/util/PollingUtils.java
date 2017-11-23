package com.zr.webview.util;

import java.util.Calendar;
import java.util.TimeZone;

import com.zr.webview.AlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PollingUtils {
	//开启轮询服务
		public static void startPollingService(Context context,int start , int seconds, Class<?> cls) {
			//获取AlarmManager系统服务
			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			
			//包装需要执行Service的Intent
			Intent intent = new Intent(context , cls);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			//触发服务的起始时间
			long triggerAtTime = System.currentTimeMillis() + start ;//* 1000;
			
			//使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
			manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
					seconds , pendingIntent);
					//seconds * 1000, pendingIntent);
		}
		public static void startPollingServiceNoRepeat(Context context,int index , int start ,String url , String video , String size) {
			//获取AlarmManager系统服务 
			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			
			//包装需要执行Service的Intent
			Intent intent = new Intent(context , AlarmReceiver.class);
			intent.putExtra("val", url);
			intent.putExtra("size", size);
			intent.putExtra("vid", video);
			intent.putExtra("idx", String.valueOf(index));
			Calendar calendar = Calendar.getInstance();
		 	calendar.setTimeInMillis(System.currentTimeMillis());
		 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			calendar.add(Calendar.MILLISECOND, start);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index,
					intent, 0);
			//触发服务的起始时间
			manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
		}
		public static void startPollingServiceNoRepeatReboot(Context context) {
			//获取AlarmManager系统服务 
			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			
			//包装需要执行Service的Intent
			Intent intent = new Intent(context , AlarmReceiver.class);
			intent.putExtra("val", "url");
			intent.putExtra("size", "a");
			intent.putExtra("vid", "b");
			intent.putExtra("idx", "reboot");
			Calendar calendar = Calendar.getInstance();
		 	calendar.setTimeInMillis(System.currentTimeMillis());
		 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			//calendar.add(Calendar.MILLISECOND, start);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
					intent, 0);
			//触发服务的起始时间
			manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
		}
		//停止轮询服务
		public static void stopPollingServiceNoRepeat(Context context,int index) {
			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context , AlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, index,
					intent, 0);
			manager.cancel(pendingIntent);
		}
		public static void stopPollingService(Context context,Class<?> cls) {
			AlarmManager manager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context , cls);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			//取消正在执行的服务
			manager.cancel(pendingIntent);
		}
}
