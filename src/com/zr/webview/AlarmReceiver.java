package com.zr.webview;

import com.zr.webview.util.SecondActivityEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ClassName: AlarmReceiver  
 * @Description: 闹铃时间到了会进入这个广播，这个时候可以做一些该做的业务。
 * @author HuHood
 * @date 2013-11-25 下午4:44:30  
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		EventBus.getDefault().post(new SecondActivityEvent(intent.getStringExtra("idx") , intent.getStringExtra("val"),intent.getStringExtra("vid"),intent.getStringExtra("size")));
    }

}
