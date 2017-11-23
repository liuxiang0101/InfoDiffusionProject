package com.zr.webview.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.greenrobot.event.EventBus;

public class CronRebootJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		String jobName = context.getJobDetail().getName();
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		String jobCmd = dataMap.getString("cmd");
		//Log.i("MainActivity", jobName + "---" + urlType + "----------------"+urlId + "|"+url + "|" + len + "|" + vid + "|" + size);
		//EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_Plan , url , len , vid , size));
		EventBus.getDefault().post(new PlanActivityEvent(CommUtils.Socket_Flag_Reboot , jobCmd));
	}

}
