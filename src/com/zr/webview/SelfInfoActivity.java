package com.zr.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.zxing.WriterException;
import com.zr.webview.util.CommUtils;
import com.zr.webview.util.DensityUtil;
import com.zr.webview.zxing.encoding.EncodingHandler;

/**
 * 参数设置界面后台
 */
public class SelfInfoActivity extends Activity {
    private Button bt_confirm;
    private TextView tv_more;
    private TextView tv_help;
    private TextView playerName_yj;
    private ImageView iv_setting;
    private ImageView ivBackBtn;
    private ImageView iv_qr_code;
    private ImageView iv_logo;
    private View viewDropDown;
    private View viewAtLocation;
    private PopupWindow popupWindowDropDown;
    private PopupWindow popupWindowAtLocation;
    private String qrCodeString;
    private RelativeLayout rl_contain_qr_code;
    private SharedPreferences sharedPreferences;   //私有文件对象
    private static final Logger logger = LoggerFactory.getLogger();
    private boolean isFirst = true;
    int screenWidth;
    int screenHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_view);
//        qrCodeString = "http://www.guangjiaoyun.com/business.php?g=Business&c=Login&a=indexnew&gjno=";
        qrCodeString = "http://www.momocity.cn/business.php?g=Business&c=Login&a=indexnew&gjno=";
        initView();
        initGUI();
        CrashApplication.getInstance().addActivity(this);
        try {
            if (!qrCodeString.equals("")) {
                //根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
                Bitmap qrCodeBitmap = EncodingHandler.createQRCode(qrCodeString, 350);
                iv_qr_code.setImageBitmap(qrCodeBitmap);
            } else {
                Toast.makeText(this, "Text can not be empty", Toast.LENGTH_SHORT).show();
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (getIntent().getBooleanExtra("ifShowPopup", false) && isFirst) {
//            isFirst = false;
//            showPopupWindowAtLocation();
//            iv_setting.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (popupWindowAtLocation != null) popupWindowAtLocation.dismiss();
//                }
//            }, 5000);
//        }
        ViewGroup.LayoutParams params= rl_contain_qr_code.getLayoutParams();
        params.width=rl_contain_qr_code.getHeight();
        rl_contain_qr_code.setLayoutParams(params);
        ViewGroup.LayoutParams params1= iv_logo.getLayoutParams();
        params1.width=rl_contain_qr_code.getHeight()/7;
        params1.height=rl_contain_qr_code.getHeight()/7;
        iv_logo.setLayoutParams(params1);
    }

    private void initView() {
        screenHeight = DensityUtil.getScreenHeight(this);
        screenWidth = DensityUtil.getScreenWidth(this);
        playerName_yj = (TextView) findViewById(R.id.playerName);
        iv_setting = (ImageView) findViewById(R.id.iv_setting);
        ivBackBtn = (ImageView) findViewById(R.id.iv_back_btn);
        iv_qr_code = (ImageView) findViewById(R.id.iv_qr_code);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        rl_contain_qr_code = (RelativeLayout) findViewById(R.id.rl_contain_qr_code);

        iv_setting.setOnClickListener(onClickListener);
//        textVersion_yj.setText(CommUtils.AppVersion);
        ivBackBtn.setOnClickListener(onClickListener);

    }

    private void initGUI() {
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        String serviceIp = sharedPreferences.getString("serviceIp", CommUtils.Server_Url);
        String serverPort_yj = sharedPreferences.getString("serverPort_yj", String.valueOf(CommUtils.Server_Port));
        String serverRegistCode_yj = sharedPreferences.getString("serverRegistCode_yj", String.valueOf(CommUtils.Server_RegistCode));
        String playerNameString = sharedPreferences.getString("playerName_yj", String.valueOf(CommUtils.PlayerName));
//        String playerGJNO_yj = sharedPreferences.getString("gjnoString", String.valueOf(CommUtils.PlayerGJNO));
        playerName_yj.setText(playerNameString);
        //GJNO_yj.setText(playerGJNO_yj);
        qrCodeString = qrCodeString + CommUtils.PlayerDeviceSn;
    }

    /**
     * 对控件点击的监听
     */
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //更多按钮
                case R.id.iv_setting:
                    showPopupWindowAsDropDown();
                    break;
                //返回按钮
                case R.id.iv_back_btn:
//                    finish();
                    break;
                //弹出框中的 更多 按钮
                case R.id.tv_more:
                    if (popupWindowDropDown != null)
                        popupWindowDropDown.dismiss();
                    startActivityForResult(new Intent(SelfInfoActivity.this, MoreInfoActivity.class), 100);
                    break;
                //弹出框中的 帮助 按钮
                case R.id.tv_help:
                    if (popupWindowDropDown != null)
                        popupWindowDropDown.dismiss();
                    showPopupWindowAtLocation();
                    break;
                case R.id.bt_confirm:
                    if (popupWindowAtLocation != null)
                        popupWindowAtLocation.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 弹出popupWindow
     */
    private void showPopupWindowAsDropDown() {
        if (popupWindowDropDown == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            viewDropDown = layoutInflater.inflate(R.layout.popup_inside_below_layout, null);
            tv_more = (TextView) viewDropDown.findViewById(R.id.tv_more);
            tv_help = (TextView) viewDropDown.findViewById(R.id.tv_help);
            tv_more.setOnClickListener(onClickListener);
            tv_help.setOnClickListener(onClickListener);

            popupWindowDropDown = new PopupWindow(viewDropDown, screenHeight / 10, screenHeight / 8);

        }
        popupWindowDropDown.setFocusable(true);
        popupWindowDropDown.setOutsideTouchable(true);
        // 点击物理返回键也能使其消失，并且不会影响你的背景
        popupWindowDropDown.setBackgroundDrawable(new BitmapDrawable());

        popupWindowDropDown.showAsDropDown(iv_setting, 4, 4);
    }

    private void showPopupWindowAtLocation() {
        if (popupWindowAtLocation == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            viewAtLocation = layoutInflater.inflate(R.layout.popup_inside_center_layout, null);
            bt_confirm = (Button) viewAtLocation.findViewById(R.id.bt_confirm);
            bt_confirm.setOnClickListener(onClickListener);
            popupWindowAtLocation = new PopupWindow(viewAtLocation, screenHeight * 3 / 8, screenHeight / 2);

        }
        popupWindowAtLocation.setFocusable(true);
        popupWindowAtLocation.setOutsideTouchable(true);
        // 点击物理返回键也能使其消失，并且不会影响你的背景
        popupWindowAtLocation.setBackgroundDrawable(new BitmapDrawable());

        popupWindowAtLocation.showAtLocation(((ViewGroup) findViewById(android.R.id.content)).getChildAt(0), Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataTransfer) {
        //从设置界面返回时进行的操作
        if (requestCode == 100 && 150 == resultCode) {
            Intent data = new Intent();
            data.putExtra("serviceIp_yj", dataTransfer.getStringExtra("serviceIp_yj"));
            data.putExtra("servicePort_yj", dataTransfer.getStringExtra("servicePort_yj"));
            data.putExtra("serverRegistCode_yj", dataTransfer.getStringExtra("serverRegistCode_yj"));
            data.putExtra("playerName_yj", dataTransfer.getStringExtra("playerName_yj"));
//            sharedPreferences.edit().putString("serviceIp", ipStr_yj).commit();
//            sharedPreferences.edit().putString("serverPort_yj", portStr_yj).commit();
//            sharedPreferences.edit().putString("serverRegistCode_yj", registerStr_yj).commit();
//            sharedPreferences.edit().putString("playerName_yj", playerN_yj).commit();
            setResult(150, data);
        }
        //从设置界面清空节目单返回时进行的操作
        if (requestCode == 100 && 160 == resultCode) {
            Intent data = new Intent();
            setResult(160, data);
        }

        //从设置界面打开调试框返回时进行的操作
        if (requestCode == 100 && 170 == resultCode) {
            Intent data = new Intent();
            setResult(170, data);
        }
        if (requestCode == 100 && 180 == resultCode) {
            Intent data = new Intent();
            setResult(180, data);
        }
        finish();
        super.onActivityResult(requestCode, resultCode, dataTransfer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
