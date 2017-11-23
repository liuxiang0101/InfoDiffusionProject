package com.zr.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.zr.webview.util.CommUtils;


/**
 * Created by Administrator on 2017/5/2.
 */
public class MoreInfoActivity extends Activity {
    private Button clearBtn;
    private Button openDebugText;
    private Button commitBtn;
    private Button bt_confirm;
    private EditText registCode_yj;
    private EditText port_yj;
    private EditText ip_yj;
    private EditText playerName_yj;
    private EditText GJNO_yj;
    private EditText et_version;
    private TextView textVersion_yj;
    private TextView tv_more;
    private TextView tv_help;
    private ImageView ivBackBtn;
    private View viewDropDown;
    private View viewAtLocation;
    private PopupWindow popupWindowDropDown;
    private PopupWindow popupWindowAtLocation;
    private SharedPreferences sp;   //私有文件对象

    private boolean isFirst = true;
    private static final Logger logger = LoggerFactory.getLogger();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        initView();
        initGUI();
        CrashApplication.getInstance().addActivity(this);

    }

    private void initView() {
        openDebugText = (Button) findViewById(R.id.OpenDebugText);
        ip_yj = (EditText) findViewById(R.id.serviceIp_yj);
        port_yj = (EditText) findViewById(R.id.servicePort_yj);
        registCode_yj = (EditText) findViewById(R.id.RegistCode);
        playerName_yj = (EditText) findViewById(R.id.playerName);
        GJNO_yj = (EditText) findViewById(R.id.GJNOET);
        et_version = (EditText) findViewById(R.id.et_version);
        textVersion_yj = (TextView) findViewById(R.id.textVersion);
        ivBackBtn = (ImageView) findViewById(R.id.iv_back_btn);
        commitBtn = (Button) findViewById(R.id.setting_commit_btn);
        clearBtn = (Button) findViewById(R.id.setting_clear_btn);

        textVersion_yj.setOnClickListener(onClickListener);
//        textVersion_yj.setText(CommUtils.AppVersion);
        ivBackBtn.setOnClickListener(onClickListener);
        openDebugText.setOnClickListener(onClickListener);
        clearBtn.setOnClickListener(onClickListener);
        commitBtn.setOnClickListener(onClickListener);
    }

    private void initGUI() {
        sp = getSharedPreferences("setting", MODE_PRIVATE);
        String serviceIp = sp.getString("serviceIp", CommUtils.Server_Url);
        String serverPort_yj = sp.getString("serverPort_yj", String.valueOf(CommUtils.Server_Port));
        String serverRegistCode_yj = sp.getString("serverRegistCode_yj", String.valueOf(CommUtils.Server_RegistCode));
        String playerNameString = sp.getString("playerName_yj", String.valueOf(CommUtils.PlayerName));
//        String playerGJNO_yj = sp.getString("gjnoString", String.valueOf(CommUtils.PlayerGJNO));
        ip_yj.setText(serviceIp);
        ip_yj.requestFocus();
        registCode_yj.setText(serverRegistCode_yj);
        port_yj.setText(serverPort_yj);
        playerName_yj.setText(playerNameString);
        //GJNO_yj.setText(playerGJNO_yj);
        GJNO_yj.setText(CommUtils.PlayerDeviceSn);
        et_version.setText(CommUtils.AppVersion);

    }

    /**
     * 对控件点击的监听
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //更多按钮
                case R.id.textVersion:
                    showPopupWindowAsDropDown();
                    break;
                //返回按钮
                case R.id.iv_back_btn:
                    finish();
                    break;
                //调试信息按钮
                case R.id.OpenDebugText:
                    Intent data = new Intent();
                    setResult(170, data);
                    finish();
                    break;
                //清除界面单按钮
                case R.id.setting_clear_btn:
                    clickCleatButton();
                    break;
                //设置按钮
                case R.id.setting_commit_btn:
                    clickSetUpButton();
                    break;
                //弹出框中的 更多 按钮
                case R.id.tv_more:
                    if (popupWindowDropDown != null)
                        popupWindowDropDown.dismiss();
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
     * 点击清除按钮功能
     */
    private void clickCleatButton() {
        //				Intent data=new Intent();
//                setResult(160, data);
//                finish();
        new AlertDialog.Builder(MoreInfoActivity.this).setTitle("升级提示")//设置对话框标题
                .setMessage("是否继续完成升级？")//设置显示的内容
                .setPositiveButton("是", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        Intent data = new Intent();
                        setResult(180, data);
                        finish();
                    }
                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {//添加返回按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {//响应事件
                Intent data = new Intent();
                setResult(160, data);
                finish();
            }
        }).show();//在按键响应事件中显示此对话框
    }

    /**
     * 点击设置按钮功能
     */
    private void clickSetUpButton() {
        String ipStr_yj = ip_yj.getText().toString();
        String portStr_yj = port_yj.getText().toString();
        String registerStr_yj = registCode_yj.getText().toString();
        String playerN_yj = playerName_yj.getText().toString();
        if (ipStr_yj==null || ipStr_yj.length() == 0) {
            Toast toast = Toast.makeText(MoreInfoActivity.this, "服务端IP不能为空!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
//				Pattern pattern = Pattern.compile( "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$" );
//				if(!pattern.matcher(ipStr_yj).matches())
//				{
//					Toast toast = Toast.makeText(SelfInfoActivity.this, "服务端IP不合法!", Toast.LENGTH_SHORT);
//					toast.show();
//					return;
//				}
//				if(portStr_yj == null || portStr_yj.length() == 0){
//					Toast toast = Toast.makeText(SelfInfoActivity.this, "预警服务端端口不能为空!", Toast.LENGTH_SHORT);
//					toast.show();
//					return;
//				}
        if (registerStr_yj.length() != 0) {
            if (registerStr_yj.length() != 10) {
                Toast toast = Toast.makeText(MoreInfoActivity.this, "请在注册码栏填写10位数字或空白!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else {
                boolean isNum = registerStr_yj.matches("[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]");
                if (!isNum) {
                    Toast toast = Toast.makeText(MoreInfoActivity.this, "注册码栏包含非数字字符!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
            }
        }
        logger.debug("serviceIp_yj:" + ipStr_yj);
        logger.debug("serverRegistCode_yj:" + registerStr_yj);
        logger.debug("playerName_yj:" + playerN_yj);
        Intent data = new Intent();
        data.putExtra("serviceIp_yj", ipStr_yj);
        data.putExtra("servicePort_yj", portStr_yj);
        data.putExtra("serverRegistCode_yj", registerStr_yj);
        data.putExtra("playerName_yj", playerN_yj);
        sp.edit().putString("serviceIp", ipStr_yj).commit();
        sp.edit().putString("serverPort_yj", portStr_yj).commit();
        sp.edit().putString("serverRegistCode_yj", registerStr_yj).commit();
        sp.edit().putString("playerName_yj", playerN_yj).commit();
        setResult(150, data);
        finish();
    }

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

            popupWindowDropDown = new PopupWindow(viewDropDown, 200, 220);

        }
        popupWindowDropDown.setFocusable(true);
        popupWindowDropDown.setOutsideTouchable(true);
        // 点击物理返回键也能使其消失，并且不会影响你的背景
        popupWindowDropDown.setBackgroundDrawable(new BitmapDrawable());

        popupWindowDropDown.showAsDropDown(textVersion_yj, 4, 4);
    }

    private void showPopupWindowAtLocation() {
        if (popupWindowAtLocation == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            viewAtLocation = layoutInflater.inflate(R.layout.popup_inside_center_layout, null);
            bt_confirm = (Button) viewAtLocation.findViewById(R.id.bt_confirm);
            bt_confirm.setOnClickListener(onClickListener);
            popupWindowAtLocation = new PopupWindow(viewAtLocation, 800, 1000);

        }
        popupWindowAtLocation.setFocusable(true);
        popupWindowAtLocation.setOutsideTouchable(true);
        // 点击物理返回键也能使其消失，并且不会影响你的背景
        popupWindowAtLocation.setBackgroundDrawable(new BitmapDrawable());

        popupWindowAtLocation.showAtLocation(((ViewGroup) findViewById(android.R.id.content)).getChildAt(0), Gravity.CENTER, 0, 0);
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
