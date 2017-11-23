package com.zr.webview.util;


import android.content.Context;
import android.os.Build;

import com.zr.webview.R;


public class TipToastUtil {
	private static TipsToast tipsToast;
	private Context context;
	public TipToastUtil(Context context){
		this.context = context;
	}
	public void showTipsWarn(String msg){
		showTips(R.drawable.tips_warning, msg);
	}
	public void showTipsSuccess(String msg){
		showTips(R.drawable.tips_success, msg);
	}
	public void showTipsError(String msg){
		showTips(R.drawable.tips_error, msg);
	}
	private void showTips(int iconResId, String msg) {
		if (tipsToast != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				tipsToast.cancel();
			}
		} else {
			tipsToast = TipsToast.makeText(context, msg, TipsToast.LENGTH_SHORT);
		}
		tipsToast.show();
		tipsToast.setIcon(iconResId);
		tipsToast.setText(msg);
	}
}
