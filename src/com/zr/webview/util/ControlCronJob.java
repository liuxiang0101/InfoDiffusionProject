package com.zr.webview.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import android.util.Log;
import de.greenrobot.event.EventBus;

public class ControlCronJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		String jobName = context.getJobDetail().getName();
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		String url = dataMap.getString("value");
		String urlId= dataMap.getString("id");
		String urlType= dataMap.getString("cmd");
		String startTime= dataMap.getString("startTime");
		String endTIme= dataMap.getString("endTime");
		//Log.i("MainActivity", jobName + "---" + urlType + "----------------"+urlId + "|"+url + "|" + len + "|" + vid + "|" + size);
		//EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_Plan , url , len , vid , size));
		EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_NewControl , startTime, endTIme, urlType, urlId, url));
	}

}
