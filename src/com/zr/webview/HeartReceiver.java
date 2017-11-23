package com.zr.webview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zr.webview.util.CommUtils;
import com.zr.webview.util.HeartActivityEvent;

import de.greenrobot.event.EventBus;

/**
 * 
 * @ClassName: AlarmReceiver  
 * @Description: 闹铃时间到了会进入这个广播，这个时候可以做一些该做的业务。
 * @author HuHood
 * @date 2013-11-25 下午4:44:30  
 *
 */
public class HeartReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		EventBus.getDefault().post(new HeartActivityEvent(CommUtils.Socket_Flag_Heart , null));
    }

}
