package com.zr.webview.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import android.util.Log;
import de.greenrobot.event.EventBus;

public class CronJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		String jobName = context.getJobDetail().getName();
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		String url = dataMap.getString("url");
		String len = dataMap.getString("len");
		String vid = dataMap.getString("vid");
		String size = dataMap.getString("size");
		String urlId= dataMap.getString("urlId");
		String urlType= dataMap.getString("urlType");
		String startTime= dataMap.getString("startTime");
		String endTIme= dataMap.getString("endTime");
		//int remainlen= dataMap.getInt("remainlen");
		//long startPlayTime= dataMap.getLong("startPlayTime");
		Log.i("MainActivity", jobName + "---" + urlType + "----------------"+urlId + "|"+url + "|" + len + "|" + vid + "|" + size);
		//EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_Plan , url , len , vid , size));
		EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_NewPlan , startTime, endTIme, urlType, urlId, url , len , vid , size));
	}

}
