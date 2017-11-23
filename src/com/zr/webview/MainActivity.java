package com.zr.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.HttpGet;
import com.zr.webview.debug.DebugOverlay;
import com.zr.webview.debug.Service99view;
import com.zr.webview.loading.LoadingView;
import com.zr.webview.model.OrderPlayListModel;
import com.zr.webview.model.OrderPlayModel;
import com.zr.webview.netty.MyClientInitializer;
import com.zr.webview.play.PlayControlUtil;
import com.zr.webview.play.Utils;
import com.zr.webview.play.VideoView;
import com.zr.webview.util.CommUtils;
import com.zr.webview.util.DataActivityEvent;
import com.zr.webview.util.HeartActivityEvent;
import com.zr.webview.util.LoadingDialog;
import com.zr.webview.util.PlanActivityEvent;
import com.zr.webview.util.PlanModel;
import com.zr.webview.util.PollingUtils;
import com.zr.webview.util.QuartzManager;
import com.zr.webview.util.SecondActivityEvent;
import com.zr.webview.util.TipToastUtil;
import com.zr.webview.view.ProgressWebView;

import net.tsz.afinal.http.HttpHandler;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


public class MainActivity extends Activity {
    private static String TAG = MainActivity.class.getName();
    private Context context;
    private static AudioManager audioManager = null;

    public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
    public static final String FTP_CONNECT_FAIL = "ftp连接失败";
    public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
    public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";
    public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
    public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
    public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";
    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";
    public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
    public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";
    private static String defaultUrl = "file:///android_asset/ad.html";  //默认加载页面
    private final static String defaultAdUrl = "file:///android_asset/ad.html";  //默认加载页面"http://yscene.99we.cn/index.php?s=Admin/Index/index/showType/2";//
    private static String defaulVideoUrl = CommUtils.Video_Default_FName;  //默认视频地址
    private static String defaulVideoSize = "";  //视频大小和位置
    private static String defaulVideoSplit = ":"; //大小位置分隔符
    private String deviceSn = "";
    private String currentUrl = defaultUrl;
    private String appName = "WebviewBox";
    private String videoUrl = "";  //当前播放的视频地址
    private String USER_AGENT_STRINGdef;// = webView.getSettings().getUserAgentString();// + " Rong/2.0";

    private LoadingDialog progress_dialog = null;
    private TipToastUtil tipToastUtil;
    private WebChromeClient chromeClient = new MyChromeClient();
    private MyWebviewCient myWebviewCient = new MyWebviewCient();
    private WebChromeClient.CustomViewCallback myCallBack = null;
    private NioEventLoopGroup group;
    private HttpHandler<File> handlerDown;
    private DebugOverlay debugOverlay;
    private SharedPreferences sp;   //私有文件对象

    private ImageView blankView; //视频播放器
    private ProgressBar updatePB;
    private View myView = null;
    private ProgressWebView map_view = null;
    private VideoView videoView; //视频播放器

    private Channel channel = null;
    private UpdataInfo updataxmlinfo;
    private PlanModel waitingPlan1;
    private PlanModel waitingPlan2;
    private PlanModel waitingPlan3;
    private PlanModel waitingPlan4;
    private PlanModel currentPlan;

    private String defaultUrlId;
    private String nowJobName;
    private String updateapkname = "99viewupdate.apk";
    private String updateapkversionname = "99viewupdateversion.txt";

    private Gson gson;

    private static Logger logger;//=Logger.getLogger(MainActivity.class);
    private Bootstrap bootstrap;

    private final int THEAD_POOL_NUM = 10;
    private int socketConNum = 0;
    private int gj_no = 12345;
    private int currentUrlType = 1;
    private int currentStatus = 1;
    private int orderListIndex = 0;
    private int instantOrderListIndex = 0;
    private int waitingPlan1Tag = 0;
    private int waitingPlan2Tag = 0;
    private int waitingPlan3Tag = 0;
    private int waitingPlan4Tag = 0;
    private int heartbeatCount = 0;
    private int connectServerCount = 0;
    private int g_sync_time_once = 0;
    private int ifvideolocalplaycount = 0;
    private int ifvideolocalplaycounttag = 3;
    private int ftpdownloadnewupdateprocesscount = 0;
    private int ftpdownloadnewupdateprocesscounttag = 3;
    private int rebootmini = -1;
    private int videoviewplaybacktime = 0;

    private boolean bootstrapinit = false;
    private boolean ifvideoplaytag = false;
    private boolean timessyncstatus = false;
    private boolean configupdate = false;
    private boolean hackconfigfiletttag = false;
    private boolean connFlagInactive = false;
    private boolean connFlagTag = false;
    private boolean urltimetag = false;
    private boolean firstConnFlag = false;
    private boolean debugOverViState = false;
    private boolean debugOver = true;
    private boolean sdCardLogEnable = true;
    private boolean videoFlag = false;
    private boolean ifNeedToInfoActivity = false;
    private boolean isDelayDealConducted = false;
    private boolean isActivityOnPause = false;
    private boolean isOrderListPlay = false;
    private boolean connFlag = false;

    private long boottimes;
    private List<PlanModel> listOrderSavePlanModel;//垫片顺播存储集合
    private List<PlanModel> listInstantOrderSavePlanModel;//立即顺播存储集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        //PropertyConfigurator.getConfigurator(this).configure();
        debugOverlay = DebugOverlay.with(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        CrashApplication.getInstance().addActivity(this);
        sp = getSharedPreferences("setting", MODE_PRIVATE);
        gson=new Gson();
        CommUtils.Server_Url = sp.getString("serviceIp", CommUtils.Server_Url);
        CommUtils.Server_Port = Integer.parseInt(sp.getString("serverPort_yj", String.valueOf(CommUtils.Server_Port)));
//		CommUtils.Server_RegistCode = sp.getString("serverRegistCode_yj", CommUtils.Server_RegistCode);
        CommUtils.PlayerName = sp.getString("playerName_yj", CommUtils.PlayerName);
        appName = CommUtils.PlayerName;

        //CommUtils.Ftpport = sp.getInt("ftpserverport", CommUtils.Ftpport);
        CommUtils.Ftpusr = sp.getString("ftpserveruser", CommUtils.Ftpusr);
        CommUtils.Ftppasswd = sp.getString("ftpserverpasswd", CommUtils.Ftppasswd);
        CommUtils.Ftpserver = sp.getString("ftpserveraddr", CommUtils.Ftpserver);

        CommUtils.playlisthtmladdrhead = "http://" + CommUtils.Ftpserver + "/";
        CommUtils.playlisthtmladdrheadTag = CommUtils.Ftpserver.substring(CommUtils.Ftpserver.indexOf("."), CommUtils.Ftpserver.length());
        CommUtils.playlisthtmlvideoftpaddr = CommUtils.Ftpserver;

        CommUtils.updatexml = sp.getString("updatexml", CommUtils.updatexml);
        CommUtils.playlisthtmladdr = sp.getString("playlisthtmladdr", CommUtils.playlisthtmladdr);
        CommUtils.playlisthtmladdrEnd = sp.getString("playlisthtmladdrEnd", CommUtils.playlisthtmladdrEnd);


        CommUtils.syslogserverdir = sp.getString("syslogserverdir", CommUtils.syslogserverdir);
        CommUtils.Ftpdisksmallsize = Integer.parseInt(sp.getString("ftpdisksmallsize", String.valueOf(CommUtils.Ftpdisksmallsize)));
        CommUtils.Ftpdiskcleansize = Integer.parseInt(sp.getString("ftpdiskcleansize", String.valueOf(CommUtils.Ftpdiskcleansize)));

        CommUtils.PlayerGJNO = sp.getInt("playerGJNO_yj", CommUtils.PlayerGJNO);
        gj_no = CommUtils.PlayerGJNO;
        CommUtils.appworkpath = Environment.getExternalStorageDirectory() + File.separator + "99view" + File.separator;
        CommUtils.appworkpath = sp.getString("appworkpath", CommUtils.appworkpath);

        rebootmini = sp.getInt("apprebootmini", -1);
        if (rebootmini == -1) {
            int maxmin = 59;
            int minmin = 0;
            Random random = new Random();
            int smin = random.nextInt(maxmin) % (maxmin - minmin + 1) + minmin;
            if ((smin >= 0) && (smin <= 59)) {
                sp.edit().putInt("apprebootmini", smin).apply();
                rebootmini = smin;
            } else {
                sp.edit().putInt("apprebootmini", 0).apply();
                rebootmini = 0;
            }
        }

        configLog();
        checkappworkpath();
        Intent intent = new Intent("com.zr.webview.debug.Service99view");
        intent.setClass(this, Service99view.class);
        startService(intent);
        waitingPlan2Tag = 0;
        waitingPlan3Tag = 0;
        waitingPlan4Tag = 0;
        waitingPlan1 = new PlanModel();
        waitingPlan2 = new PlanModel();
        waitingPlan3 = new PlanModel();
        waitingPlan4 = new PlanModel();
        currentPlan = new PlanModel();
        updatePB = (ProgressBar) super.findViewById(R.id.updataProcessBar);
        updatePB.setVisibility(View.GONE);

        logger.debug("boot....................................:" + rebootmini);
        logger.debug("loger.debug.begin test:" + getLineNumber());
        logger.error("loger.error.begin test:" + getLineNumber());

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        //获取屏幕大小
        getScreenSize();
        //创建sd卡目录结构
        Utils.initDir();
        //复制assets内容到sd卡，暂时可以不需要
        PlayControlUtil.copyAssets(this, "defaultAd");

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        deviceSn = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        CommUtils.PlayerDeviceSn = deviceSn;
        CommUtils.PlayerName = "gj_" + deviceSn;
        appName = CommUtils.PlayerName;
        ExecutorService executorService = Executors.newFixedThreadPool(THEAD_POOL_NUM);
        map_view = (ProgressWebView) this.findViewById(R.id.map_view);
        videoView = (VideoView) findViewById(R.id.play_video);
        blankView = (ImageView) findViewById(R.id.blank_view);
        videoView.setVisibility(View.GONE);
        map_view.setVisibility(View.VISIBLE);
        blankView.setVisibility(View.GONE);
        try {
            Bitmap blankbitmap;
            InputStream fis = getAssets().open("blank.bmp");
            blankbitmap = BitmapFactory.decodeStream(fis);
            blankView.setImageBitmap(blankbitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initWebViewSet(map_view);
        EventBus.getDefault().register(this);
        progress_dialog = new LoadingDialog(this);
        tipToastUtil = new TipToastUtil(this);
        QuartzManager.shutdownJobs();
        //writeTxtToFile("1",apppppp, "mag.txt");
        connFlagTag = false;//传输标记
        nowJobName = "";
        //writeTxtToFile("2",apppppp, "mag.txt");
        recordWindowLog(getLineNumber() + ":appworkpath:" + CommUtils.appworkpath + "!!!!");
        updateconfigfile();
        File file = new File(new File(Utils.getExBaseDir() + File.separator + "ad"), CommUtils.Video_Default_FName);
        if (file.exists()) {
            //writeTxtToFile("3file exist!!",apppppp, "mag.txt");
            recordWindowLog(getLineNumber() + ":PlayerName:" + CommUtils.PlayerName + ":configName:" + CommUtils.hackconfigfilename + ":AppVersion:" + CommUtils.AppVersion + ":Server_RegistCode:" + CommUtils.Server_RegistCode + ":Server_Url:" + CommUtils.Server_Url + ":updatexml:" + CommUtils.updatexml + "!!!!");
            videoView.setVisibility(View.GONE);
            planPlay();
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //connectServer();
                //updateconfigfile();
                beginrun();
            }
        });
        map_view.postDelayed(new Runnable() {
            @Override
            public void run() {
                isDelayDealConducted = true;
                if (ifNeedToInfoActivity && !isActivityOnPause)
                    startActivityForResult(new Intent(MainActivity.this, SelfInfoActivity.class).putExtra("ifShowPopup", true), 100);
            }
        }, 5000);

        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(this)) {
                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent1,10);
            }
        }
    }

    /**
     * 使用log4j.jar，存储日志信息到本地
     */
    public void configLog() {
        final LogConfigurator logConfigurator1 = new LogConfigurator();

        logConfigurator1.setFileName(Environment.getExternalStorageDirectory() + File.separator + "99view" + File.separator + "log" + File.separator + "99view.log");
        // Set the root log level
        logConfigurator1.setRootLevel(Level.ALL);
        // Set log level of a specific logger
        logConfigurator1.setLevel("org.apache", Level.ERROR);

        logConfigurator1.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //logConfigurator.setFilePattern("%d %p\#%L\#%c\.%M\#%t\#%m%n");
        logConfigurator1.setMaxFileSize(1024 * 5 * 1024);
        logConfigurator1.setImmediateFlush(true);

        logConfigurator1.configure();

        //gLogger = Logger.getLogger(this.getClass());
        logger = Logger.getLogger(MainActivity.class);
    }

    /**
     * 获取屏幕尺寸信息
     */
    private void getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int widthPixels = dm.widthPixels;
        int heightPixels = dm.heightPixels;
        float density = 1;
        int screenWidth = (int) (widthPixels * density);
        int screenHeight = (int) (heightPixels * density);
        Utils.screen_Width = screenWidth;
        Utils.screen_Height = screenHeight;
        Utils.width_Factor = screenWidth;
        Utils.height_Factor = screenHeight;
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        //Utils.width_Factor = new Double(screenWidth/(double)CommUtils.Video_Rel_Size);
        //Utils.height_Factor = new Double(screenHeight/(double)CommUtils.Video_Rel_Size);
        Log.i(TAG, "获取屏幕大小【" + Utils.screen_Width + "*" + Utils.screen_Height + "|" + Utils.width_Factor + "|" + Utils.height_Factor + "】");
        String msgStr = "screenwidth:" + String.valueOf(screenWidth) + ",screenHeight:" + String.valueOf(screenHeight) + "screenDensity:" + String.valueOf(screenDensity);
        recordWindowLog(msgStr);
        //logger.debug(msgStr);
        //webView.getSettings().setDefaultZoom(zoomDensity);
    }

    /**
     * 初始化webview属性
     */
    private void initWebViewSet(WebView webView) {
        map_view.setWebViewClient(myWebviewCient);
        map_view.setWebChromeClient(chromeClient);
        map_view.setHorizontalScrollBarEnabled(false);
        map_view.setVerticalScrollBarEnabled(false);
        map_view.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        WebSettings webSettings = map_view.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);

        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);   // 可以读取文件缓存(manifest生效)
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        USER_AGENT_STRINGdef = webSettings.getUserAgentString();
        webSettings.setSupportZoom(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);


        String databasePath = context.getApplicationContext().getDir("database", 0).getPath();
        recordWindowLog("databasePath:" + databasePath);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheMaxSize(100 * 1024 * 1024);
        //webSettings.setAppCachePath(databasePath);Environment.getExternalStorageDirectory() + "/ad";
        String cacheDirPath = Environment.getExternalStorageDirectory() + File.separator + "ad";
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setDatabasePath(cacheDirPath);
        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setBuiltInZoomControls(true);
        //int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.FAR;
        //WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.CLOSE ;
        webSettings.setDefaultZoom(zoomDensity);
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        map_view.setInitialScale(0);
//		webSettings.setMediaPlaybackRequiresUserGesture(false);

    }

    /**
     * 获取系统音量
     */
    private static int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void loadVideo(String video, String size) {
        //如果需要播放视频
        if (videoFlag && video != null && video.length() > 0) {
            //位置信息后期扩展使用
//			int w = Integer.parseInt(size.split(defaulVideoSplit)[0]);
//			int h = Integer.parseInt(size.split(defaulVideoSplit)[1]);
//			int x = Integer.parseInt(size.split(defaulVideoSplit)[2]);
//			int y = Integer.parseInt(size.split(defaulVideoSplit)[3]);
            if (videoView != null) {
                File file = new File(new File(Utils.getExBaseDir() + File.separator + "ad"), PlayControlUtil.getFileName(video));
                if (file.exists()) {
                    videoView.setVideoFile(file.getAbsolutePath());
                } else {
                    file = new File(new File(Utils.getExBaseDir() + File.separator + "ad"), CommUtils.Video_Default_FName);
                    videoView.setVideoFile(file.getAbsolutePath());
                }
                videoView.setVisibility(View.GONE);
            }
        } else {
            if (videoView != null) {
                recordWindowLog(getLineNumber() + ":old onPause 2!!!");
                //videoView.pause();
                //videoView.setVisibility(View.GONE);
            }
            map_view.setVisibility(View.VISIBLE);
        }
    }

    Handler update_tip = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String msgOut = "msg.what:" + msg.what;
            switch (msg.what) {
                case 500:
                    //recordWindowLog(msgOut);
                    endLoad();
                    if (!firstConnFlag) {
//             			tipToastUtil.showTipsWarn("服务端无法连接!");
//             			defaultUrl = "file:///android_asset/ad.html";
//                    	map_view.loadUrl(getRefreshUrl(defaultUrl));
//            			currentUrl = defaultUrl;
                        firstConnFlag = true;
                    }
                    reConnectServer();
                    break;
                case 300:
                    //recordWindowLog(msgOut);
//             		if(videoView != null){
//                		videoView.setVisibility(View.GONE);
//                	}
                    break;
                case 600: //打印日志到屏幕
                    String msgStr = (String) msg.obj;
                    recordWindowLog(msgOut + "--" + msgStr);
                    break;
                case 601: //打印日志到屏幕
                    String msgStrs = (String) msg.obj;
                    debugOverlay.log(msgStrs);
                    break;
                case 410: //控制计划
                    PlanModel planModelC = (PlanModel) msg.obj;
                    String urlc = planModelC.getUrl();
                    String urlTypec = planModelC.getUrlType();
                    //recordWindowLog("prepare to control");
                    try {
                        planControl(Integer.valueOf(urlTypec), urlc);
                    } catch (Exception e) {
                        //recordWindowLog(urlc + "err!!!");
                        e.printStackTrace();
                    }
                    break;
                case 400: //播放计划处理
                    PlanModel planModel = (PlanModel) msg.obj;
                    String url = planModel.getUrl();
                    String video = planModel.getVideo();
                    String size = planModel.getSize();
                    recordWindowLog(url + " prepare to play");

                    String urlIdget = currentPlan.getUrlId();
                    String urlTypeget = currentPlan.getUrlType();
                    String urlget = url;//currentPlan.getUrl();

                    String playtime = CommUtils.getCurrentTimeNew();
                    String urlIdStr = "@" + urlIdget + "@";
                    String urlTypeString;
                    if (Integer.valueOf(urlTypeget) == 1) {
                        urlTypeString = "default";
                    } else if (Integer.valueOf(urlTypeget) == 2) {
                        urlTypeString = "zhouqi";
                    } else if (Integer.valueOf(urlTypeget) == 3) {
                        urlTypeString = "chabo";
                    } else {
                        urlTypeString = "liji";
                    }
                    String playtimewithjobName = CommUtils.get32MD5(urlIdStr + urlget + "_" + urlTypeString + playtime);


                    if (url.contains("android_video_asset")) {

                        recordWindowLog("android_asset play");
                        loadVideo(video, size);
                        videoUrl = video;
                        map_view.loadUrl("about:blank");
                        currentUrlType = 0;//video only
                        beginPlaySomething(playtimewithjobName, urlget, "video", Integer.valueOf(urlTypeget), playtime);
                    } else {
                        //recordWindowLog("no android_asset play");

                        //loadVideo(video, size);
                        videoUrl = video;
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        boolean b = url.startsWith("~@W@~");
                        if (b) {
                            //map_view.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
                            url = url.replace("~@W@~", "");
                            url=url.trim();
                        } else {
                            //map_view.getSettings().setUserAgentString(USER_AGENT_STRINGdef);
                        }
                        //recordWindowLog("111_1page load start  " + url);
                        //currentUrl = url;
                        //currentUrlType = Integer.valueOf(currentPlan.getUrlType());
//                        if (!findvideofile(url)) {
                        //map_view.loadUrl(getRefreshUrl(url));
                        //map_view.setVisibility(View.VISIBLE);
//                        }
                        findvideofile(url);
                        beginPlaySomething(playtimewithjobName, urlget, "html", Integer.valueOf(urlTypeget), playtime);
                    }
                    nowJobName = playtimewithjobName;
                    break;
                case 801:    //UPDATA_CLIENT:
                    //对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case 802:    //GET_UNDATAINFO_ERROR:
                    //服务器超时
                    //Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1).show();
                    LoginMain();
                    break;
                case 803:    //DOWN_ERROR:
                    //下载apk失败
                    //Toast.makeText(getApplicationContext(), "下载新版本失败", 1).show();
                    //LoginMain();
                    break;
                case 804:    //
                    int sid = CommUtils.updatexml.trim().indexOf("http://");
                    if (sid == 0) {
                        logger.debug("begin:804");
                        //设置黑屏
                        //writeSysfs("/sys/class/graphics/fb0/blank","1");
                        recordWindowLog(getLineNumber() + ":beginHttpUpdata!!:" + CommUtils.updatexml);
                        beginHttpUpdata();
                        logger.debug("end:804");
                    } else {
                        recordWindowLog(getLineNumber() + ":beginUpdata!!:" + CommUtils.updatexml);
                        beginFtpUpdata();
                    }
                    break;
                case 805:
                    //点亮屏幕
                    //writeSysfs("/sys/class/graphics/fb0/blank","0");
                    String msgStr805 = (String) msg.obj;
                    updatePB.setVisibility(View.VISIBLE);
                    updatePB.setMax(Integer.parseInt(msgStr805));//设置进度条的最大值
                    updatePB.setProgress(0);
                    break;
                case 806:
                    //设置黑屏
                    //writeSysfs("/sys/class/graphics/fb0/blank","1");
                    String msgStr806 = (String) msg.obj;
                    updatePB.setProgress(Integer.parseInt(msgStr806));
                    break;
                case 807:
                    //点亮屏幕
                    //writeSysfs("/sys/class/graphics/fb0/blank","0");
                    String msgStr807 = (String) msg.obj;
                    updatePB.setVisibility(View.GONE);
                    break;
                case 808:
                    //播放视频
                    String msgStr808 = (String) msg.obj;
                    map_view.setVisibility(View.GONE);
                    videoView.setVideoFile(msgStr808);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.play();
                    recordWindowLog("808 play:" + msgStr808);
                    break;
                case 809:
                    //清空视频列表
                    videoView.stop();
                    videoView.setVisibility(View.GONE);
                    videoView.clearPlaylist();
                    //map_view.setVisibility(View.GONE);
                    //videoView.setVideoFile(msgStr809);
                    recordWindowLog("809 clearplaylist!!!");
                    break;
                case 810:
                    //插入视频列表
                    String msgStr810 = (String) msg.obj;
                    videoView.addToPlaylist(msgStr810);
//    				MediaMetadataRetriever retr = new MediaMetadataRetriever();
//    				retr.setDataSource(msgStr810);
//    				String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
//    				String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
//    				recordWindowLog("810 add playlist:" + msgStr810 + ":width:" + width + ":height:" + height);
                    break;
                case 811:
                    //播放视频列表
                    try {
                        String msgStr811 = (String) msg.obj;
                        map_view.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.play();
                        recordWindowLog("811 play playlist:" + msgStr811);
                    } catch (Exception e) {
                        recordWindowLog(getLineNumber() + ":errr:" + e.getMessage());
                    }
                    break;
                case 812:
                    //播放url
                    String msgStr812 = (String) msg.obj;
                    startLoad();
                    if (urltimetag) {
                        recordWindowLog("812 play url:00000");
                        map_view.loadUrl(msgStr812);
                    } else {
                        recordWindowLog("812 play url:11111");
                        map_view.loadUrl(getRefreshUrl(msgStr812));
                    }
                    map_view.setVisibility(View.VISIBLE);
                    videoView.stop();
                    videoView.setVisibility(View.GONE);
                    recordWindowLog("812 play url:" + msgStr812);
                    break;
                case 813:
                    //播放视频列表
                    String msgStr813 = (String) msg.obj;
                    try {
                        recordWindowLog("Utils.screen_Width:" + Utils.screen_Width);
                        recordWindowLog("Utils.screen_Height:" + Utils.screen_Height);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.play();
                        videoviewplaybacktime = 0;
                        recordWindowLog("813 play playlist:" + msgStr813);
                    } catch (Exception e) {
                        recordWindowLog(getLineNumber() + ":errr:" + e.getMessage());
                    }
                    break;
                case 814:
                    //播放url
                    String msgStr814 = (String) msg.obj;
                    startLoad();
                    if (urltimetag) {
                        recordWindowLog("814 play url:00000");
                        map_view.loadUrl(msgStr814);
                    } else {
                        recordWindowLog("814 play url:11111");
                        map_view.loadUrl(getRefreshUrl(msgStr814));
                    }
                    map_view.setVisibility(View.VISIBLE);
                    recordWindowLog("814 play url:" + msgStr814);
                    break;
                case 820:
                    //测试上传
                    beginFtpUpload();
                    break;
                default:
                    break;
            }
        }
    };

    /*
     * 避免url内容不刷新
     */
    private String getRefreshUrl(String url) {
        return url + "?t=" + new Date().getTime();
    }

    /**
     * 重连netty
     */
    private void reConnectServer() {
        update_tip.postDelayed(new Runnable() {
            @Override
            public void run() {
                //recordWindowLog("begin to reconnect server",true);
                connectServer();
            }
        }, CommUtils.Server_Reconn_Period);

    }

    /**
     * load page and play video
     */
    private void loadPlanUrl(String url, String video, String size) {
        PlanModel model = new PlanModel();
        model.setSize(size);
        model.setUrl(url);
        model.setVideo(video);
        Message msg = Message.obtain();
        msg.what = 400;
        msg.obj = model;
        update_tip.sendMessage(msg);
    }

    /**
     * control plan
     */
    private void loadControlPlanUrl(String cmd, String value) {
        PlanModel model = new PlanModel();
        model.setUrlType(cmd);
        model.setUrl(value);
        Message msg = Message.obtain();
        msg.what = 410;
        msg.obj = model;
        update_tip.sendMessage(msg);
    }

    /**
     * 记录日志和打印日志到屏幕
     */
    private void recordWindowLog(String str) {
        recordWindowLog(str, false);
    }

    private void recordWindowLog(String str, boolean error) {
        String timeS = CommUtils.getCurrentTime();
        if (debugOver) {
            Message msg = Message.obtain();
            msg.what = 601;
            msg.obj = timeS + ":" + str;
            update_tip.sendMessage(msg);
        }
        if (sdCardLogEnable) {
            if (error) {
                logger.error(timeS + ":" + str);
            } else {
                logger.debug(timeS + ":" + str);
            }
        } else {
            if (error) {
                Log.e(TAG, str);
            } else {
                Log.i(TAG, str);
            }
        }

    }

    /***
     * 将字符串写入到文本文件中
     */
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错

        String strFilePath = filePath + File.separator + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    public static int getLineNumber() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        return stackTrace[1].getLineNumber();
    }

    public static String getMethodName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        return stackTrace[1].getMethodName();
    }

    public static String getFileName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        return stackTrace[1].getFileName();
    }

    public static String getClassName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        return stackTrace[1].getClassName();
    }

    private void checkappworkpath() {
        String tfpath = "/mnt/extsd/";
        File mkFile = new File(tfpath);
        if (mkFile.exists()) {
            StatFs fsss = new StatFs(tfpath);
            //可用的blocks的数量
            long availableBolocks = fsss.getAvailableBlocksLong();
            //单个block的大小
            long blockSize = fsss.getBlockSizeLong();
            long available = availableBolocks * blockSize;
            recordWindowLog(getLineNumber() + ":tf size:" + available + "!!!!");
            if (available > CommUtils.Ftpdiskcleansize * 1024 * 1024) {
                CommUtils.appworkpath = tfpath;
            }
        }
    }

    /**
     * 计划播放(程序onCreate、netty连接、心跳连接时调用)
     */
    private void planPlay() {
        String default_url = sp.getString("default_url", "");
        String rn_url_id = sp.getString("rn_id_buffer", "");
        String default_listStr = sp.getString("default_list", "");
        defaultUrlId = sp.getString("default_url_id", "");

        recordWindowLog("defaultUrlId:" + defaultUrlId);

//        if (!default_listStr.equals("")) {//本地存储的垫片播逻辑为顺播时
//            sp.edit().putBoolean("is_order_playlist_type1", true).apply();
//            isOrderListPlay = true;
//            orderListIndex = 0;
//            //解析数组数据
//            String string = "{list:" + default_listStr + "}";
//            OrderPlayModel model = gson.fromJson(string, OrderPlayModel.class);
//
//            //建立与接收的顺播播出单集合个数相同的存储播放model的集合
//            listOrderSavePlanModel = new ArrayList<PlanModel>();
//            for (OrderPlayListModel listModel : model.getList()) {
//                PlanModel planModel = new PlanModel();
//                planModel.setUrl(listModel.getUrl());
//                planModel.setVideo(videoUrl);
//                planModel.setSize(currentPlan.getSize());
//                planModel.setUrlId(currentPlan.getUrlId());
//                planModel.setUrlType("" + currentUrlType);
//                planModel.setRemainlen(listModel.getPlaytimelength());
//                planModel.setBeginTime(currentPlan.getBeginTime());
//                planModel.setEndTime(currentPlan.getEndTime());
//                planModel.setAllLen(currentPlan.getAllLen());
//                listOrderSavePlanModel.add(planModel);
//            }
//            default_url = listOrderSavePlanModel.get(0).getUrl();
//            defaultUrl = default_url;
//            if (rn_url_id.equals("")) {//立即播存储播出单id为空时
//
//                currentPlan.setAllLen(model.getList().get(0).getPlaytimelength());
//                currentPlan.setStartPlayTime(System.currentTimeMillis());
//                currentPlan.setUrl(currentUrl);
//
//
//                currentUrl = default_url;
//                currentUrlType = 1;
//                currentPlan.setUrlId(defaultUrlId);
//                currentPlan.setUrlType(String.valueOf(currentUrlType));
//                String url;
//                boolean b = default_url.startsWith("~@W@~");
//                if (b) {
//                    String urlNew = default_url.replace("~@W@~", "");
//                    recordWindowLog("after format the url -- " + urlNew);
//                    url = urlNew;
//                    url = url.trim();
//                } else {
//                    url = default_url;
//                }
//                String videoSize = null;
//                loadPlanUrl(model.getList().get(0).getUrl(), url, videoSize);
//                if (orderListIndex < listOrderSavePlanModel.size() - 1)
//                    orderListIndex++;
//                else
//                    orderListIndex = 0;
//                PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(defaultUrlId),
//                        model.getList().get(0).getPlaytimelength(),
//                        model.getList().get(0).getUrl(), url, videoSize);
////                findvideofile(url);
////                String playtime = CommUtils.getCurrentTimeNew();
////                String urlIdStr = "@" + String.valueOf(defaultUrlId) + "@";
////                recordWindowLog("urlIdStr:" + urlIdStr);
////                String playtimewithjobName = CommUtils.get32MD5(urlIdStr + currentUrl + "_" + "default" + playtime);
////                beginPlaySomething(playtimewithjobName, currentUrl, "html", currentUrlType, playtime);
////                nowJobName = playtimewithjobName;
//            } else {
//                //读取立即播出单ID对应的信息
//                String localJsonText = sp.getString(rn_url_id, "");
//                if (localJsonText.length() <= 0) {//立即播ID对应的信息为空
//                    whenPlayType4Unexist(rn_url_id, default_url);
//                } else {//立即播ID对应的信息不为空
//                    whenPlayType4Exist(localJsonText, rn_url_id, default_url);
//                }
//            }
//            return;
//        }

        //default_url="~@W@~http://web7xl6dv0xv2d62f4.99view.com";
        if (default_url.length() > 0) {//如果可以从本地读取垫片播地址
            defaultUrl = default_url;
            if (rn_url_id.equals("")) {//立即播存储播出单id为空时
                currentUrl = default_url;
                currentUrlType = 1;
                currentPlan.setUrlId(defaultUrlId);
                currentPlan.setUrlType(String.valueOf(currentUrlType));
                String url;
                boolean b = default_url.startsWith("~@W@~");
                if (b) {
                    String urlNew = default_url.replace("~@W@~", "");
                    recordWindowLog("after format the url -- " + urlNew);
                    url = urlNew;
                    url = url.trim();
                } else {
                    url = default_url;
                }
                //map_view.loadUrl(getRefreshUrl(url));
                findvideofile(url);
                String playtime = CommUtils.getCurrentTimeNew();
                String urlIdStr = "@" + String.valueOf(defaultUrlId) + "@";
                recordWindowLog("urlIdStr:" + urlIdStr);
                String playtimewithjobName = CommUtils.get32MD5(urlIdStr + currentUrl + "_" + "default" + playtime);
                beginPlaySomething(playtimewithjobName, currentUrl, "html", currentUrlType, playtime);
                nowJobName = playtimewithjobName;
            } else {
                //读取立即播出单ID对应的信息
                String localJsonText = sp.getString(rn_url_id, "");
                if (localJsonText.length() <= 0) {//立即播ID对应的信息为空
                    whenPlayType4Unexist(rn_url_id, default_url);
                } else {//立即播ID对应的信息不为空
                    whenPlayType4Exist(localJsonText, rn_url_id, default_url);
                }
            }
        } else {//如果无法从本地读取垫片播地址，播放默认的本地网页
            default_url = defaultAdUrl;//"file:///android_asset/ad.html";
            if (rn_url_id.equals("")) {//如果立即播播出单ID为空，执行逻辑
                whenPlayType4Unexist(rn_url_id, default_url);
            } else {//立即播播出单ID不为为空
                String jsonText = sp.getString(rn_url_id, "");
                if (jsonText.length() <= 0) {//根据立即播ID获取信息为空时
                    whenPlayType4Unexist(rn_url_id, default_url);
                } else {
                    whenPlayType4Exist(jsonText, rn_url_id, default_url);
                }
            }
        }
    }

    private void whenPlayType4Unexist(String rn_url_id, String default_url) {
        sp.edit().remove(rn_url_id).apply();            //删掉立即播
        sp.edit().remove("rn_id_buffer").apply();       //删掉立即播
        default_url = defaultAdUrl;                     //"file:///android_asset/ad.html";
        map_view.loadUrl(getRefreshUrl(default_url));
        currentUrl = defaultUrl;
        currentUrlType = 1;
        currentPlan.setUrlId(defaultUrlId);
        currentPlan.setUrlType(String.valueOf(currentUrlType));
        currentPlan.setUrl(currentUrl);

        String urlIdget = currentPlan.getUrlId();
        String urlTypeget = currentPlan.getUrlType();
        String urlget = currentPlan.getUrl();
        String playtime = CommUtils.getCurrentTimeNew();
        String urlIdStr = "@" + urlIdget + "@";
        String urlTypeString;
        if (Integer.valueOf(urlTypeget) == 1) {
            urlTypeString = "default";
        } else if (Integer.valueOf(urlTypeget) == 2) {
            urlTypeString = "zhouqi";
        } else if (Integer.valueOf(urlTypeget) == 3) {
            urlTypeString = "chabo";
        } else {
            urlTypeString = "liji";
        }
        String playtimewithjobName = CommUtils.get32MD5(urlIdStr + urlget + "_" + urlTypeString + playtime);
        beginPlaySomething(playtimewithjobName, urlget, "html", Integer.valueOf(urlTypeget), playtime);
        nowJobName = playtimewithjobName;
    }

    private void whenPlayType4Exist(String jsonText, String rn_url_id, String default_url) {
        int currentUrlTypeTmp;
        String currentUrlTmp;
        try {
            JSONObject jsonObject = new JSONObject(jsonText);
            String type = jsonObject.getString("type");
            if (type.equals("playlist")) {
                currentUrlTypeTmp = jsonObject.getInt("plisttype");
                currentUrlTmp = jsonObject.getString("url");
                String tmpVideoUrl = "";
                String videoSize = "";
                //String playlistOP = jsonObject.getString("op");
                if (jsonObject.has("videourl")) {
                    tmpVideoUrl = jsonObject.getString("videourl");
                    int videoW = jsonObject.getInt("videow");
                    int videoH = jsonObject.getInt("videoh");
                    int videoX = jsonObject.getInt("videox");
                    int videoY = jsonObject.getInt("videoy");
                    videoSize = videoW + defaulVideoSplit + videoH + defaulVideoSplit + videoX + defaulVideoSplit + videoY;
                }
                int playSec = jsonObject.getInt("playtimelength");
                int urlId = jsonObject.getInt("id");
                String enddate0 = jsonObject.getString("enddate");
                String endtime = jsonObject.getString("endtime");
                String enddate = "";
                String enddatetime = "";
                if (enddate0.equals("") || endtime.equals("")) {

                } else {
                    enddate = enddate0.replaceAll("-", ":");
                    enddatetime = enddate + "-" + endtime;
                    long enddatetimewhen;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                    enddatetimewhen = sdf.parse(enddatetime).getTime();
                    playSec = (int) (enddatetimewhen - System.currentTimeMillis());
                    recordWindowLog("receive rnow playlist re lenInnn:" + playSec);
                }
                if (playSec > 0) {
                    recordWindowLog("playSec:" + playSec);
                    currentUrlType = currentUrlTypeTmp;
                    currentUrl = currentUrlTmp;
                    currentPlan.setUrlId(String.valueOf(urlId));
                    currentPlan.setUrlType(String.valueOf(currentUrlTypeTmp));
                    currentPlan.setSize(videoSize);
                    currentPlan.setAllLen(playSec);
                    currentPlan.setStartPlayTime(System.currentTimeMillis());
                    loadPlanUrl(currentUrlTmp, tmpVideoUrl, videoSize);
                    PollingUtils.startPollingServiceNoRepeat(context, urlId, playSec, currentUrlTmp, tmpVideoUrl, videoSize);
                } else {
                    whenPlayType4Unexist(rn_url_id, default_url);
                }
            } else {
                whenPlayType4Unexist(rn_url_id, default_url);
            }
        } catch (Exception e) {
            whenPlayType4Unexist(rn_url_id, default_url);
        }
    }

    private void updateconfigfile() {
        try {
            File configfile1 = new File(CommUtils.appworkpath + CommUtils.hackconfigfilename);
            configfile1.delete();
            //hack server read
            recordWindowLog(getLineNumber() + ":" + CommUtils.hackconfigfileserver);
            new FTP(CommUtils.hackconfigfileserver, "admin", "admin").downloadSingleFile1(CommUtils.hackconfigfilepath + CommUtils.hackconfigfilename, CommUtils.appworkpath, CommUtils.hackconfigfilename);//"99view1x__update_comtest.xml");//
            recordWindowLog(getLineNumber() + ":" + CommUtils.hackconfigfileserver);
            File configfile = new File(CommUtils.appworkpath + CommUtils.hackconfigfilename);
            recordWindowLog(getLineNumber() + ":" + CommUtils.appworkpath + CommUtils.hackconfigfilename);
            InputStream insss = new FileInputStream(configfile);
            recordWindowLog(getLineNumber() + ":" + CommUtils.appworkpath + CommUtils.hackconfigfilename);
            updataxmlinfo = getUpdataInfoAll(insss);
            String serverurl = updataxmlinfo.getServerUrl();
            if (serverurl.length() > 0) {
                if (!CommUtils.Server_Url.equals(serverurl)) {
                    CommUtils.Server_Url = serverurl;
                    sp.edit().putString("serviceIp", serverurl).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverregcode = updataxmlinfo.getServerRegcode();
            if (serverregcode.length() > 0) {
                if (!CommUtils.Server_RegistCode.equals(serverregcode)) {
                    CommUtils.Server_RegistCode = serverregcode;
                    sp.edit().putString("serverRegistCode_yj", serverregcode).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverupdatexml = updataxmlinfo.getUpdatexml();
            if (serverupdatexml.length() > 0) {
                if (!CommUtils.updatexml.equals(serverupdatexml)) {
                    CommUtils.updatexml = serverupdatexml;
                    sp.edit().putString("updatexml", serverupdatexml).apply();
                    hackconfigfiletttag = true;
                }
            }
            String servervideoftpaddr = updataxmlinfo.getPlaylisthtmlvideoftpaddr();
            if (servervideoftpaddr.length() > 0) {
                if (!CommUtils.playlisthtmlvideoftpaddr.equals(servervideoftpaddr)) {
                    CommUtils.playlisthtmlvideoftpaddr = servervideoftpaddr;
                    CommUtils.playlisthtmladdrhead = "http://" + CommUtils.playlisthtmlvideoftpaddr + "/";
                    CommUtils.playlisthtmladdrheadTag = CommUtils.playlisthtmlvideoftpaddr.substring(CommUtils.playlisthtmlvideoftpaddr.indexOf("."), CommUtils.playlisthtmlvideoftpaddr.length());
                    CommUtils.Ftpserver = servervideoftpaddr;
                    sp.edit().putString("ftpserveraddr", servervideoftpaddr).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverplaylisthtmladdr = updataxmlinfo.getPlaylisthtmladdr();
            if (serverplaylisthtmladdr.length() > 0) {
                if (!CommUtils.playlisthtmladdr.equals(serverplaylisthtmladdr)) {
                    CommUtils.playlisthtmladdr = serverplaylisthtmladdr;
                    sp.edit().putString("playlisthtmladdr", serverplaylisthtmladdr).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverplaylisthtmladdrend = updataxmlinfo.getPlaylisthtmladdrEnd();
            if (serverplaylisthtmladdrend.length() > 0) {
                if (!CommUtils.playlisthtmladdrEnd.equals(serverplaylisthtmladdrend)) {
                    CommUtils.playlisthtmladdrEnd = serverplaylisthtmladdrend;
                    sp.edit().putString("playlisthtmladdrEnd", serverplaylisthtmladdrend).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverappworkpath = updataxmlinfo.getappworkpath();
            if (serverappworkpath.length() > 0) {
                if (!CommUtils.appworkpath.equals(serverappworkpath)) {
                    CommUtils.appworkpath = serverappworkpath;
                    sp.edit().putString("appworkpath", serverappworkpath).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serversyslogserverdir = updataxmlinfo.getsyslogserverdir();
            if (serversyslogserverdir.length() > 0) {
                if (!CommUtils.syslogserverdir.equals(serversyslogserverdir)) {
                    CommUtils.syslogserverdir = serversyslogserverdir;
                    sp.edit().putString("syslogserverdir", serversyslogserverdir).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverftpusr = updataxmlinfo.getftpusr();
            if (serverftpusr.length() > 0) {
                if (!CommUtils.Ftpusr.equals(serverftpusr)) {
                    CommUtils.Ftpusr = serverftpusr;
                    sp.edit().putString("ftpserveruser", serverftpusr).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverftppasswd = updataxmlinfo.getftppasswd();
            if (serverftppasswd.length() > 0) {
                if (!CommUtils.Ftppasswd.equals(serverftppasswd)) {
                    CommUtils.Ftppasswd = serverftppasswd;
                    sp.edit().putString("ftpserverpasswd", serverftppasswd).apply();
                    hackconfigfiletttag = true;
                }
            }
            String serverftpdisksmallsize = updataxmlinfo.getftpdisksmallsize();
            if (serverftpdisksmallsize.length() > 0) {
                int serverftpdisksmallsizeint = Integer.valueOf(serverftpdisksmallsize);
                if (serverftpdisksmallsizeint > 0) {
                    if (CommUtils.Ftpdisksmallsize != serverftpdisksmallsizeint) {
                        CommUtils.Ftpdisksmallsize = serverftpdisksmallsizeint;
                        sp.edit().putString("ftpdisksmallsize", serverftpdisksmallsize).apply();
                        hackconfigfiletttag = true;
                    }
                }
            }

            String serverftpdiskcleansize = updataxmlinfo.getftpdiskcleansize();
            if (serverftpdiskcleansize.length() > 0) {
                int serverftpdiskcleansizeint = Integer.valueOf(serverftpdiskcleansize);
                if (serverftpdiskcleansizeint > 0) {
                    if (CommUtils.Ftpdiskcleansize != serverftpdiskcleansizeint) {
                        CommUtils.Ftpdiskcleansize = serverftpdiskcleansizeint;
                        sp.edit().putString("ftpdiskcleansize", serverftpdiskcleansize).apply();
                        hackconfigfiletttag = true;
                    }
                }
            }

            String serverPlayerName = updataxmlinfo.getplayerName();
            if (serverPlayerName.length() > 0) {
                serverPlayerName = serverPlayerName + "_" + CommUtils.PlayerDeviceSn;
                if (!CommUtils.PlayerName.equals(serverPlayerName)) {
                    CommUtils.PlayerName = serverPlayerName;
                    hackconfigfiletttag = true;
                    appName = serverPlayerName;
                    sp.edit().putString("playerName_yj", serverPlayerName).apply();
                }
            }
            if (hackconfigfiletttag) {
                checkifautoupdate();
            }
            recordWindowLog(getLineNumber() + ":getServerUrl:" + updataxmlinfo.getServerUrl());
            recordWindowLog(getLineNumber() + ":getServerRegcode:" + updataxmlinfo.getServerRegcode());
            recordWindowLog(getLineNumber() + ":getUpdatexml:" + updataxmlinfo.getUpdatexml());
            recordWindowLog(getLineNumber() + ":getPlaylisthtmladdrhead:" + updataxmlinfo.getPlaylisthtmladdrhead());
            recordWindowLog(getLineNumber() + ":getPlaylisthtmladdrheadTag:" + updataxmlinfo.getPlaylisthtmladdrheadTag());
            recordWindowLog(getLineNumber() + ":getPlaylisthtmladdr:" + updataxmlinfo.getPlaylisthtmladdr());
            recordWindowLog(getLineNumber() + ":getPlaylisthtmladdrEnd:" + updataxmlinfo.getPlaylisthtmladdrEnd());
            recordWindowLog(getLineNumber() + ":getPlaylisthtmlvideoftpaddr:" + updataxmlinfo.getPlaylisthtmlvideoftpaddr());
        } catch (Exception e) {
            recordWindowLog(getLineNumber() + ":ftpdownload config err!!!:" + e.getMessage());
        }
    }

    private boolean beginrun() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 下载
                try {
//					File configfile1 = new File(CommUtils.appworkpath+CommUtils.hackconfigfilename);
//					configfile1.delete();
//					//hack server read
//					recordWindowLog(getLineNumber()+":"+CommUtils.hackconfigfileserver);
//					new FTP(CommUtils.hackconfigfileserver).downloadSingleFile1(CommUtils.hackconfigfilepath+CommUtils.hackconfigfilename,CommUtils.appworkpath,CommUtils.hackconfigfilename);//"99view1x__update_comtest.xml");//
//					recordWindowLog(getLineNumber()+":"+CommUtils.hackconfigfileserver);
//					File configfile = new File(CommUtils.appworkpath+CommUtils.hackconfigfilename);
//					recordWindowLog(getLineNumber()+":"+CommUtils.appworkpath+CommUtils.hackconfigfilename);
//					InputStream insss = new FileInputStream(configfile);
//					recordWindowLog(getLineNumber()+":"+CommUtils.appworkpath+CommUtils.hackconfigfilename);
//					updataxmlinfo = getUpdataInfoAll(insss);
//					CommUtils.Server_Url = updataxmlinfo.getServerUrl();
//					CommUtils.Server_RegistCode = updataxmlinfo.getServerRegcode();
//					CommUtils.updatexml = updataxmlinfo.getUpdatexml();
//					CommUtils.playlisthtmladdrhead = updataxmlinfo.getPlaylisthtmladdrhead();
//					CommUtils.playlisthtmladdrheadTag = updataxmlinfo.getPlaylisthtmladdrheadTag();
//					CommUtils.playlisthtmladdr = updataxmlinfo.getPlaylisthtmladdr();
//					CommUtils.playlisthtmladdrEnd = updataxmlinfo.getPlaylisthtmladdrEnd();
//					CommUtils.playlisthtmlvideoftpaddr = updataxmlinfo.getPlaylisthtmlvideoftpaddr();
//					recordWindowLog(getLineNumber()+":getServerUrl:"				+updataxmlinfo.getServerUrl());
//					recordWindowLog(getLineNumber()+":getServerRegcode:"			+updataxmlinfo.getServerRegcode());
//					recordWindowLog(getLineNumber()+":getUpdatexml:"				+updataxmlinfo.getUpdatexml());
//					recordWindowLog(getLineNumber()+":getPlaylisthtmladdrhead:"		+updataxmlinfo.getPlaylisthtmladdrhead());
//					recordWindowLog(getLineNumber()+":getPlaylisthtmladdrheadTag:"	+updataxmlinfo.getPlaylisthtmladdrheadTag());
//					recordWindowLog(getLineNumber()+":getPlaylisthtmladdr:"			+updataxmlinfo.getPlaylisthtmladdr());
//					recordWindowLog(getLineNumber()+":getPlaylisthtmladdrEnd:"		+updataxmlinfo.getPlaylisthtmladdrEnd());
//					recordWindowLog(getLineNumber()+":getPlaylisthtmlvideoftpaddr:"	+updataxmlinfo.getPlaylisthtmlvideoftpaddr());
                    connectServer();
                    updatesystemlog();
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":ftpdownload config err!!!:" + e.getMessage());
//					if(CommUtils.Server_Url.compareTo("yk.99we.cn")==0){
//						CommUtils.playlisthtmladdrhead = "http://"+CommUtils.playlisthtmlvideoftpaddryscene+"/";
//						CommUtils.playlisthtmladdrheadTag = CommUtils.playlisthtmlvideoftpaddryscene.substring(CommUtils.playlisthtmlvideoftpaddryscene.indexOf("."), CommUtils.playlisthtmlvideoftpaddryscene.length());
//						CommUtils.playlisthtmlvideoftpaddr =  CommUtils.playlisthtmlvideoftpaddryscene;
//
//						recordWindowLog(getLineNumber()+":0CommUtils.playlisthtmladdrhead:"+CommUtils.playlisthtmladdrhead);
//						recordWindowLog(getLineNumber()+":0CommUtils.playlisthtmladdrheadTag:"+CommUtils.playlisthtmladdrheadTag);
//						recordWindowLog(getLineNumber()+":0CommUtils.playlisthtmlvideoftpaddr:"+CommUtils.playlisthtmlvideoftpaddr);
//					}else if(CommUtils.Server_Url.compareTo("bk.99we.cn")==0){
//						CommUtils.playlisthtmladdrhead = "http://"+CommUtils.playlisthtmlvideoftpaddrh50+"/";
//						CommUtils.playlisthtmladdrheadTag = CommUtils.playlisthtmlvideoftpaddrh50.substring(CommUtils.playlisthtmlvideoftpaddrh50.indexOf("."), CommUtils.playlisthtmlvideoftpaddrh50.length());
//						CommUtils.playlisthtmlvideoftpaddr =  CommUtils.playlisthtmlvideoftpaddrh50;
//
//						recordWindowLog(getLineNumber()+":1CommUtils.playlisthtmladdrhead:"+CommUtils.playlisthtmladdrhead);
//						recordWindowLog(getLineNumber()+":1CommUtils.playlisthtmladdrheadTag:"+CommUtils.playlisthtmladdrheadTag);
//						recordWindowLog(getLineNumber()+":1CommUtils.playlisthtmlvideoftpaddr:"+CommUtils.playlisthtmlvideoftpaddr);
//					}
                    connectServer();
                    updatesystemlog();
                }
            }
        }).start();
        return false;
    }

    private boolean findvideofile(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 下载
                boolean ret = false;
                try {
                    ret = ifvideolocalplay(url);//ifvideolocalplay("webs7gifyv.99view.com");//
                    ifvideolocalplaycount = 0;
                    if (ret) {
                        //ftpdownloadnew(CommUtils.downloadaddr);
                        ftpdownloadnewupdateprocess(CommUtils.downloadaddr);
                        ftpdownloadnewupdateprocesscount = 0;
                        ifvideoplaytag = true;
//						Message msg = new Message();
//		    	        msg.what = 814;//
//		    	        msg.obj = url;
//		    	        update_tip.sendMessage(msg);
                    } else {
                        ifvideoplaytag = false;
                        Message msg = new Message();
                        msg.what = 812;//
                        msg.obj = url;
                        update_tip.sendMessage(msg);

//						videoView.setVisibility(View.GONE);
//						map_view.loadUrl(getRefreshUrl(url));
//            			map_view.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":findvideofile err!!!:" + e.getMessage());
                }
            }
        }).start();
        return false;
    }

    private void addvideofile2db(final String filename) {
        String vf = sp.getString("VDName", "");
        recordWindowLog(getLineNumber() + ":addvideofile2db vf:" + vf);
        if (vf.contains(filename)) {
            long templ = System.currentTimeMillis();
            String timestr = "" + templ;
            String vdn = "VDN" + filename;
            sp.edit().putString(vdn, timestr).apply();
            recordWindowLog(getLineNumber() + ":" + vdn);
            recordWindowLog(getLineNumber() + ":" + timestr);
        } else {
            vf = vf + filename + ";";
            long templ = System.currentTimeMillis();
            String timestr = "" + templ;
            String vdn = "VDN" + filename;
            sp.edit().putString(vdn, timestr).apply();
            sp.edit().putString("VDName", vf).apply();
            recordWindowLog(getLineNumber() + ":" + vdn);
            recordWindowLog(getLineNumber() + ":" + timestr);
            recordWindowLog(getLineNumber() + ":" + vf);
        }
    }

    private void delvideofilefromdb(final String filename) {
        try {
            String vf = sp.getString("VDName", "");
            recordWindowLog(getLineNumber() + ":delvideofilefromdb vf:" + vf);
            recordWindowLog(getLineNumber() + ":filename:" + filename);
            if (vf.contains(filename)) {
                String rs = filename + ";";
                String vfffff = vf.replaceAll(rs, "");
                recordWindowLog(getLineNumber() + ":rs:" + rs);
                recordWindowLog(getLineNumber() + ":vf:" + vf);
                sp.edit().putString("VDName", vfffff).apply();
                String vdn = "VDN" + filename;
                sp.edit().remove(vdn).apply();
            } else {
                String vdn = "VDN" + filename;
                sp.edit().remove(vdn).apply();
            }
            String vf1 = sp.getString("VDName", "");
            recordWindowLog(getLineNumber() + ":delvideofilefromdb vf1:" + vf1);
            //String controlPlanName = "CPName:"+id;
            //sp.edit().putString("control_plan_id_buffer", localCrons).commit();
            //sp.edit().remove(controlPlanName).commit();
        } catch (Exception e) {
            recordWindowLog(getLineNumber() + ":delvideofilefromdb err!!!");
        }
    }

    private boolean checkifplaying(final String filename) {
        boolean ret = false;
        recordWindowLog(getLineNumber() + ":filename:" + filename);
        if (CommUtils.downloadaddr == null || CommUtils.downloadaddr.length <= 0) {

        } else {
            for (String addrsig : CommUtils.downloadaddr) {
                recordWindowLog(getLineNumber() + ":addrsig:" + addrsig);
                String tmpfilename = addrsig.substring(addrsig.lastIndexOf("/") + 1, addrsig.length());
                if (tmpfilename.equals(filename)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    private String delvideofile(final String filename) {
        try {
            String vf = sp.getString("VDName", "");
            recordWindowLog(getLineNumber() + ":vf: " + vf);
            String[] cronIdArr;
            String retFilename = "";
            long rftime = -1;
            if (!vf.isEmpty()) {
                cronIdArr = vf.split(";");
                retFilename = "";
                rftime = -1;
                if (!vf.isEmpty()) {
                    for (String cronIdsig : cronIdArr) {
                        String vdn = "VDN" + cronIdsig;
                        recordWindowLog(getLineNumber() + ":vdn:" + vdn);
                        String ttttttt = sp.getString(vdn, "");
                        recordWindowLog(getLineNumber() + ":ttttttt:" + ttttttt);
                        if (!ttttttt.isEmpty()) {
                            long ttttttl = Long.valueOf(ttttttt);
                            if (rftime == -1) {
                                rftime = ttttttl;
                                retFilename = cronIdsig;
                            } else if (rftime > ttttttl) {
                                rftime = ttttttl;
                                retFilename = cronIdsig;
                            }
                        } else {
                            if (cronIdsig.length() != 0) {
                                delvideofilefromdb(cronIdsig);
                            }
                        }
                    }
                }
            }
            //recordWindowLog(getLineNumber()+":downloadaddr: "+CommUtils.downloadaddr[0]);
            if (rftime != -1) {
                if (!checkifplaying(retFilename)) {
                    delvideofilefromdb(retFilename);
                    File vffffff = new File(CommUtils.appworkpath + retFilename);
                    vffffff.delete();
                    recordWindowLog(getLineNumber() + ":delete " + retFilename);
                }
            }


            //File dirfile=new File(Environment.getExternalStorageDirectory() + File.separator +"99view"+ File.separator);
            File dirfile = new File(CommUtils.appworkpath);
            File[] fs = dirfile.listFiles();
            for (File f : fs) {
                String local = f.getCanonicalPath();
                String remotefilename = f.getName();
                recordWindowLog(getLineNumber() + ":local:" + local + ":remote:" + remotefilename + "!!!");
                if (remotefilename.contains(".")) {
                    if (!checkifplaying(remotefilename)) {
                        File vffffff = new File(local);
                        vffffff.delete();
                        recordWindowLog(getLineNumber() + ":delete " + local);
                    }
                }
            }
            return retFilename;
        } catch (Exception e) {
            recordWindowLog(getLineNumber() + ":delvideofilefromdb err!!!" + e.getMessage());
            return "";
        }
    }

    private void ftpdownloadThread(final String tmpalladdr, final String tmpfilename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 下载
                try {
                    FTP fcccc = new FTP(CommUtils.playlisthtmlvideoftpaddr);
                    int rt = fcccc.downloadSingleFile2(tmpalladdr, CommUtils.appworkpath, tmpfilename);
                    if (rt >= 0) {
                        addvideofile2db(tmpfilename);
                    }
                    recordWindowLog("ftpdownloadThread " + tmpfilename + "   end!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void ftpdownloadnewupdateprocess(final String[] downloadadr) {
        //下载功能
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 下载
                try {
                    if (ftpdownloadnewupdateprocesscount > ftpdownloadnewupdateprocesscounttag) {
                        recordWindowLog(getLineNumber() + ":ftpdownloadnewupdateprocesscount>ftpdownloadnewupdateprocesscounttag  !!!");
                        return;
                    }
                    Message msg = new Message();
                    msg.what = 809;//
                    update_tip.sendMessage(msg);
                    int tmpiiii = 0;
                    for (String addrsig : downloadadr) {
                        String tmpfilename = addrsig.substring(addrsig.lastIndexOf("/") + 1, addrsig.length());
                        String tmpalladdr = "/" + addrsig;
                        recordWindowLog(getLineNumber() + ":tmpfilename:" + tmpfilename);
                        //FTP fcccc=new FTP(CommUtils.playlisthtmlvideoftpaddr);
                        //int rt=fcccc.downloadSingleFile2(tmpalladdr,CommUtils.appworkpath,tmpfilename);
                        //if(rt>=0){
                        //	addvideofile2db(tmpfilename);
                        //}

//						File videofilen = new File(CommUtils.appworkpath+tmpfilename);
//						videofilen.delete();

                        FTP fc = new FTP(CommUtils.playlisthtmlvideoftpaddr);
                        FTPClient ftpclint = fc.getftpClientHandel();
                        // 打开FTP服务
                        try {
                            fc.openConnect();

                        } catch (IOException e1) {
                            e1.printStackTrace();

                            continue;
                        }

                        // 先判断服务器文件是否存在
                        FTPFile[] files = ftpclint.listFiles(tmpalladdr);
                        if (files.length == 0) {
                            continue;
                        }

                        //创建本地文件夹
                        File mkFile = new File(CommUtils.appworkpath);
                        if (!mkFile.exists()) {
                            mkFile.mkdirs();
                        }

                        String localPath = CommUtils.appworkpath + tmpfilename;
                        // 接着判断下载的文件是否能断点下载
                        long serverSize = files[0].getSize(); // 获取远程文件的长度

                        //String storage = Environment.getExternalStorageDirectory().getAbsolutePath();CommUtils.appworkpath
                        String storage = CommUtils.appworkpath;
                        StatFs fsss = new StatFs(storage);
                        //可用的blocks的数量
                        long availableBolocks = fsss.getAvailableBlocksLong();
                        //单个block的大小
                        long blockSize = fsss.getBlockSizeLong();
                        long available = availableBolocks * blockSize;
                        if (available < (serverSize + CommUtils.Ftpdisksmallsize * 1024 * 1024)) {
                            recordWindowLog(getLineNumber() + ":空间不足！！！！！！:" + tmpfilename + ":" + serverSize);
                            continue;
                        }

                        File localFile = new File(localPath);
                        long localSize = 0;
                        boolean iffinished = false;
                        if (localFile.exists()) {
                            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
                            if (localSize > serverSize) {
                                File file = new File(localPath);
                                file.delete();
                                localSize = 0;
                            } else if (localSize == serverSize) {
                                iffinished = true;
                            }
                            logger.debug("localSize:" + localSize);
                        }
                        if (iffinished) {
                            recordWindowLog(getLineNumber() + ":已经下载过了!!!");
                            addvideofile2db(tmpfilename);
                            msg = new Message();
                            msg.what = 810;//
                            msg.obj = CommUtils.appworkpath + tmpfilename;
                            update_tip.sendMessage(msg);
                            if (tmpiiii == 0) {
                                tmpiiii += 1;
                                msg = new Message();
                                msg.what = 813;
                                msg.obj = CommUtils.appworkpath + tmpfilename;
                                update_tip.sendMessage(msg);
                            }
                        } else {
                            msg = new Message();
                            String lenS = "" + serverSize;
                            msg.what = 805;//
                            msg.obj = lenS;
                            update_tip.sendMessage(msg);
                            recordWindowLog(getLineNumber() + ":filedownloadwithprocess开始下载!!!");
                            // 进度
                            long step = serverSize / 100;
                            long process = 0;
                            long currentSize = 0;
                            // 开始准备下载文件
                            OutputStream out = new FileOutputStream(localFile, true);
                            ftpclint.setRestartOffset(localSize);
                            InputStream input = ftpclint.retrieveFileStream(tmpalladdr);
                            byte[] b = new byte[1024];
                            int length = 0;
                            while ((length = input.read(b)) != -1) {
                                out.write(b, 0, length);
                                currentSize = currentSize + length;
                                if (currentSize / step != process) {
                                    process = currentSize / step;
                                    if (process % 5 == 0) {  //每隔%5的进度返回一次
                                        //listener.onDownLoadProgress(MainActivity.FTP_DOWN_LOADING, process, null);
                                        String lenS1 = "" + currentSize;
                                        Message msg1 = new Message();
                                        msg1.what = 806;//
                                        msg1.obj = lenS1;//
                                        update_tip.sendMessage(msg1);
                                    }
                                }
                            }
                            out.flush();
                            out.close();
                            input.close();

                            // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
                            if (ftpclint.completePendingCommand()) {
                                recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_SUCCESS);                        //listener.onDownLoadProgress(MainActivity.FTP_DOWN_SUCCESS, 0, new File(localPath));
                                // 下载完成之后关闭连接
                                //fc.closeConnect();
                                msg = new Message();
                                msg.what = 807;// /
                                update_tip.sendMessage(msg);
                                addvideofile2db(tmpfilename);
                                msg = new Message();
                                msg.what = 810;//
                                msg.obj = CommUtils.appworkpath + tmpfilename;
                                update_tip.sendMessage(msg);
                                if (tmpiiii == 0) {
                                    tmpiiii += 1;
                                    msg = new Message();
                                    msg.what = 813;
                                    msg.obj = CommUtils.appworkpath + tmpfilename;
                                    update_tip.sendMessage(msg);
                                }

                            } else {
                                recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_FAIL);
                            }
                        }
                        recordWindowLog("ftpdownload end !:" + tmpalladdr);
                    }
//	                msg = new Message();
//	    	        msg.what = 811;
//	    	        update_tip.sendMessage(msg);
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":ftpdownload err!!!" + e.getMessage());
                    ftpdownloadnewupdateprocesscount++;
                    ftpdownloadnewupdateprocess(downloadadr);
                }
            }
        }).start();
    }

    private void ftpdownloadnew(final String[] downloadadr) {
        //下载功能
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 下载
                try {
                    Message msg = new Message();
                    msg.what = 809;//
                    update_tip.sendMessage(msg);
                    int tmpiiii = 0;
                    for (String addrsig : downloadadr) {
                        String tmpfilename = addrsig.substring(addrsig.lastIndexOf("/") + 1, addrsig.length());
                        String tmpalladdr = "/" + addrsig;
                        recordWindowLog(getLineNumber() + ":tmpfilename:" + tmpfilename);
                        //new FTP(CommUtils.playlisthtmlvideoftpaddr).downloadSingleFile1(tmpalladdr,CommUtils.appworkpath,tmpfilename);
                        FTP fcccc = new FTP(CommUtils.playlisthtmlvideoftpaddr);
                        int rt = fcccc.downloadSingleFile2(tmpalladdr, CommUtils.appworkpath, tmpfilename);
                        if (rt >= 0) {
                            addvideofile2db(tmpfilename);
                        }
                        recordWindowLog("ftpdownload ing !:" + tmpalladdr);
                        //ftpdownloadThread(tmpalladdr,tmpfilename);
                        msg = new Message();
                        msg.what = 810;//
                        msg.obj = CommUtils.appworkpath + tmpfilename;
                        update_tip.sendMessage(msg);
                        if (tmpiiii == 0) {
                            tmpiiii += 1;
                            msg = new Message();
                            msg.what = 813;
                            msg.obj = CommUtils.appworkpath + tmpfilename;
                            update_tip.sendMessage(msg);
                        }
                        //break;
                    }
//	                msg = new Message();
//	    	        msg.what = 811;
//	    	        update_tip.sendMessage(msg);
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":ftpdownload err!!!");
                    ftpdownloadnew(downloadadr);
                }
            }
        }).start();
    }


    private void abccopyAssets(Context context, String assetDir) {
        String[] subFileDirs;
        int fileNums = 0;
        int fileNums_F = 0;
        File outFile = null;
        InputStream in = null;
        try {
            subFileDirs = context.getResources().getAssets().list(assetDir);
            for (String subDir : subFileDirs) {
                recordWindowLog("获取assets下子目录【" + assetDir + "/" + subDir + "】");
                String llllg1;
                llllg1 = "!!test11!!:" + subDir;
                recordWindowLog(llllg1);
                String[] subFiles = context.getAssets().list(assetDir + "/" + subDir);
                fileNums_F += subFiles.length;
                for (String subFile : subFiles) {
                    //Log.i(TAG, "获取assets下子文件【"+assetDir+"/"+subDir+"/"+subFile+"】");
                    //logger.debug("获取assets下子文件【"+assetDir+"/"+subDir+"/"+subFile+"】");
                    recordWindowLog("获取assets下子文件【" + assetDir + "/" + subDir + "/" + subFile + "】");
                    String llllg2;
                    llllg2 = "!!test12!!:" + subFile;
                    recordWindowLog(llllg2);
                    in = context.getAssets().open(assetDir + "/" + subDir + "/" + subFile);
                    String llllg3;
                    llllg3 = "!!test13!!:" + assetDir + "/" + subDir + "/" + subFile;
                    recordWindowLog(llllg3);
                    int fileSize = in.available();
                    File outDir = new File(Utils.getExBaseDir() + "/" + subDir);
                    if (outDir.exists() && outDir.isDirectory()) {
                        outFile = new File(Utils.getExBaseDir() + "/" + subDir + "/" + subFile);
//	                			if(outFile.exists()){
//	                				Log.i(TAG, "文件【"+subFile+"】已经存在");
//	                				fileNums++;
//	                				continue;
//	                			}
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];
                        int len;
                        int count = 0;
                        int nStartPos = 0;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                            nStartPos += len;
                            if (fileSize > 0) {
                                count = nStartPos * 100 / fileSize;
                            }
                            if (count >= 100) {
                                fileNums++;
                            }
                        }
                        in.close();
                        out.close();
                    }
                }
            }
            if (fileNums == fileNums_F) {
                //Log.i(TAG, "共复制【"+fileNums+"】个文件");
                //logger.debug("共复制【"+fileNums+"】个文件");
                recordWindowLog("共复制【" + fileNums + "】个文件");
            }
        } catch (IOException e1) {   //Log.e(TAG, "列举Assets文件异常:"+e1.getMessage());
            //logger.debug("列举Assets文件异常:"+e1.getMessage());
            recordWindowLog("列举Assets文件异常:" + e1.getMessage());
        }
    }

    public boolean ifvideolocalplay(final String url) {
        boolean ret11 = false;
        try {
            // 使用Jsoup解析html
            if (ifvideolocalplaycount > ifvideolocalplaycounttag) {//ifvideolocalplaycounttag=3
                recordWindowLog(getLineNumber() + ":ifvideolocalplaycount>ifvideolocalplaycounttag  !!!");
                return false;
            }
            urltimetag = false;
            recordWindowLog(getLineNumber() + ":" + "begin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
            final String urlendtag = CommUtils.playlisthtmladdrheadTag;//".99view.com";//
            int idddd = url.indexOf("http://www.");
            if (idddd >= 0) {
                recordWindowLog(getLineNumber() + ":is not video url!!");
                urltimetag = true;
                return false;
            }
            int id = url.indexOf(urlendtag);
            if (id == -1 || id < 1) {
                recordWindowLog(getLineNumber() + ":" + url + ":can't find tag string!!");
                return false;
            }
            int videotag = 0;
            int id1 = url.indexOf("http://");
            String urlshorter;
            if (id1 == -1) {
                urlshorter = url.substring(0, (id));
            } else {
                urlshorter = url.substring(id1 + 7, (id));
            }
            recordWindowLog(getLineNumber() + ":urlshorter:" + urlshorter);
            String urlAll = CommUtils.playlisthtmladdrhead + CommUtils.playlisthtmladdr + urlshorter + CommUtils.playlisthtmladdrEnd;
            recordWindowLog(getLineNumber() + ":urlAll:" + urlAll);
            //Document doc = Jsoup.connect("http://h50.99view.com/Application/Admin/View/webRoot/webs7gifyv/index.html").get();
            Document doc = Jsoup.connect(urlAll).get();
            recordWindowLog(getLineNumber() + ":urlAll:" + urlAll);
            Elements attris = doc.getElementsByAttribute("data-module");
            recordWindowLog(getLineNumber() + ":urlAll:" + urlAll);

            for (Element attri : attris) {
                String attri111 = attri.attr("data-module");
                String attri222 = attri.attr("data-url");
                String attri333 = attri.attr("style");
                recordWindowLog(getLineNumber() + ":attri111:" + attri111 + ":attri222:" + attri222 + ":attri333:" + attri333);
                //recordWindowLog(getLineNumber());
                //recordWindowLog(getLineNumber());
                if (attri111.equals("video")) {
                    if (attri222.length() != 0) {
                        if (attri333.length() != 0) {
                            int iddddd1 = attri333.indexOf("left:");
                            if (iddddd1 >= 0) {
                                videotag = 1;
                                int iddddd2 = attri333.indexOf("%;", iddddd1);
                                if (iddddd2 >= 0) {
                                    String subssss = attri333.substring(iddddd1 + 5, iddddd2);
                                    recordWindowLog(getLineNumber() + ":left subssss:" + subssss + ":float:" + Float.valueOf(subssss.trim()));
                                    float tmpppp = Float.valueOf(subssss.trim());
                                    if (tmpppp != -1) {
                                        CommUtils.parsevideox = tmpppp;
                                    }
                                }
                                iddddd1 = attri333.indexOf("top:");
                                if (iddddd1 >= 0) {
                                    iddddd2 = attri333.indexOf("%;", iddddd1);
                                    if (iddddd2 >= 0) {
                                        String subssss = attri333.substring(iddddd1 + 4, iddddd2);
                                        recordWindowLog(getLineNumber() + ":top subssss:" + subssss + ":float:" + Float.valueOf(subssss.trim()));
                                        float tmpppp = Float.valueOf(subssss.trim());
                                        if (tmpppp != -1) {
                                            CommUtils.parsevideoy = tmpppp;
                                        }
                                    }
                                }
                                iddddd1 = attri333.indexOf("width:");
                                if (iddddd1 >= 0) {
                                    iddddd2 = attri333.indexOf("%;", iddddd1);
                                    if (iddddd2 >= 0) {
                                        String subssss = attri333.substring(iddddd1 + 6, iddddd2);
                                        recordWindowLog(getLineNumber() + ":width subssss:" + subssss + ":float:" + Float.valueOf(subssss.trim()));
                                        float tmpppp = Float.valueOf(subssss.trim());
                                        if (tmpppp != -1) {
                                            CommUtils.parsevideow = tmpppp;
                                        }
                                    }
                                }
                                iddddd1 = attri333.indexOf("height:");
                                if (iddddd1 >= 0) {
                                    iddddd2 = attri333.indexOf("%;", iddddd1);
                                    if (iddddd2 >= 0) {
                                        String subssss = attri333.substring(iddddd1 + 7, iddddd2);
                                        recordWindowLog(getLineNumber() + ":height subssss:" + subssss + ":float:" + Float.valueOf(subssss.trim()));
                                        float tmpppp = Float.valueOf(subssss.trim());
                                        if (tmpppp != -1) {
                                            CommUtils.parsevideoh = tmpppp;
                                        }
                                    }
                                }
                                //if(CommUtils.xmlparsevideow>0.8 && CommUtils.xmlparsevideoh>0.8){
                                videotag = 1;
                                CommUtils.parsedataurl = attri222;
                                CommUtils.parsedatatype = attri111;
                                CommUtils.downloadaddr = attri222.split(",");
                                break;
                                //}
                            }
                        }
                    }
                }
            }
            recordWindowLog(getLineNumber() + " end!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + videotag);
            //logger.debug(getLineNumber()+" end!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (videotag == 1) {
                ret11 = true;
            }
        } catch (IOException e) {
            //logger.debug("htmlparsetest:eerrrr!!!!:"+getLineNumber());
            recordWindowLog(getLineNumber() + " htmlparsetest:eerrrr!!!!:" + e.getMessage());
            //httpuploadhtml();
            ifvideolocalplaycount++;
            ret11 = ifvideolocalplay(url);
        }
        return ret11;
    }

    public void htmlparsetest1(final String urlshorter) {
        try {
            //使用Jsoup解析html
            logger.debug("htmlparsetest1 begin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + getLineNumber());
            logger.debug("htmlparsetest1:" + getLineNumber());
            //final String urlshorter = "webs7gifyv";
            //连接主页，获取html，开始进行解析
            String urlAll = CommUtils.playlisthtmladdrhead + CommUtils.playlisthtmladdr + urlshorter + CommUtils.playlisthtmladdrEnd;
            //Document doc = Jsoup.connect("http://h50.99view.com/Application/Admin/View/webRoot/webs7gifyv/index.html").get();
            Document doc = Jsoup.connect(urlAll).get();
            //获得一个以movie_show_shot（热播电影）为id节点
            //logger.debug("htmlparsetest:"+getLineNumber());
            //Element nodes = doc.getElementById("posPage");
            //logger.debug("htmlparsetest:"+getLineNumber()+":ele.classNames="+nodes.classNames());
            Elements attris = doc.getElementsByAttribute("data-module");
            for (Element attri : attris) {
                String attri111 = attri.attr("data-module");
                String attri222 = attri.attr("data-url");
                String attri333 = attri.attr("style");
                recordWindowLog("attri111:" + getLineNumber() + ":" + attri111);
                recordWindowLog("attri222:" + getLineNumber() + ":" + attri222);
                recordWindowLog("attri333:" + getLineNumber() + ":" + attri333);
            }
            logger.debug("end!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + getLineNumber());
        } catch (IOException e) {
            logger.debug("htmlparsetest:eerrrr!!!!:" + getLineNumber());
            e.printStackTrace();
        }
    }

    public void htmlparsetest(final String urlshorter) {
        new Thread() {

            public void run() {

                try {
                    // 使用Jsoup解析html
                    logger.debug("begin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + getLineNumber());
                    logger.debug("htmlparsetest:" + getLineNumber());
                    //final String urlshorter = "webs7gifyv";
                    //连接主页，获取html，开始进行解析
                    String urlAll = CommUtils.playlisthtmladdrhead + CommUtils.playlisthtmladdr + urlshorter + CommUtils.playlisthtmladdrEnd;
                    //Document doc = Jsoup.connect("http://h50.99view.com/Application/Admin/View/webRoot/webs7gifyv/index.html").get();
                    Document doc = Jsoup.connect(urlAll).get();
                    //获得一个以movie_show_shot（热播电影）为id节点
                    //logger.debug("htmlparsetest:"+getLineNumber());
                    //Element nodes = doc.getElementById("posPage");
                    //logger.debug("htmlparsetest:"+getLineNumber()+":ele.classNames="+nodes.classNames());
                    Elements attris = doc.getElementsByAttribute("data-module");
                    for (Element attri : attris) {
                        String attri111 = attri.attr("data-module");
                        String attri222 = attri.attr("data-url");
                        logger.debug("attri111:" + getLineNumber() + ":" + attri111);
                        logger.debug("attri222:" + getLineNumber() + ":" + attri222);
                    }
                    //获得一个以<class="video"节点集合
//					Elements links = nodes.getElementsByClass("commEdit videoButton icon-play");
//					logger.debug("htmlparsetest:"+getLineNumber()+":eles.txt="+links.text());
////					StringBuffer stringBuffer = new StringBuffer();
//					for (Element link : links) {
//						  String linkurl = link.attr("data-url");
//						  String linkstyle = link.attr("style");
//						  logger.debug("linkurl:"+getLineNumber()+":"+linkurl);
//						  logger.debug("linkstyle:"+getLineNumber()+":"+linkstyle);
//						}
//					int i = 0;
//					for (i = 0; i < links.size(); i++) {
//						//遍历集合获得第一个节点元素
//						//Element et = links.get(i).select("a[href]").first();
//						//获取元素的href属性
//						//stringBuffer.append(URL_MAIN + et.attr("href") + "\n");
//
//						links.attr("href");
//					}
//					content = stringBuffer.toString();
//					mHandler.sendEmptyMessage(0);
                    logger.debug("end!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + getLineNumber());
                } catch (IOException e) {
                    logger.debug("htmlparsetest:eerrrr!!!!:" + getLineNumber());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 开始播放计划回调
     */
    public void onEvent(PlanActivityEvent event) {
        if (event != null) {
            String flag = event.getFlag();
            String url = event.getUrl();
            String len = event.getLength();
            String video = event.getVideoUrl();
            String size = event.getVideoSize();
            String urlType = event.getUrlType();
            String urlId = event.getUrlId();
            String startTime = event.getStartTime();
            String endTime = event.getEndTIme();
            if (flag.equals(CommUtils.Socket_Flag_NewPlan)) {
                if (currentStatus == 1) {//播放状态下，可以播放播出单
                    recordWindowLog("newplan times come " + url + "-" + len);
                    update_tip.sendEmptyMessage(300);
                    long templ = System.currentTimeMillis();
                    long startTimeLong = Long.valueOf(startTime);
                    long endTimeLong = Long.valueOf(endTime);
                    if ((startTimeLong <= templ) && (templ < endTimeLong))// 激发时间在开始时间段内，则触发；在开始时间段外，则不触发
                    {
                        int lenInnn = Integer.valueOf(len);
                        if ((templ + lenInnn) > endTimeLong)    //结束时间超过时间段，则重新写播放时长
                        {
                            lenInnn = (int) (endTimeLong - templ);
                            //lenInnn = (int)(ttttp/1000);
                            recordWindowLog("re lenInnn:" + lenInnn);
                        }
                        int urlttmp = Integer.valueOf(urlType);
                        recordWindowLog("urlttmp:" + urlttmp);
                        if (currentUrlType == 1) {
                            try {
                                endPlaySomethingfunc();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            currentUrlType = Integer.valueOf(urlType);
                            currentUrl = url;
                            currentPlan.setUrlId(urlId);
                            currentPlan.setUrlType(urlType);
                            currentPlan.setSize(size);
                            currentPlan.setAllLen(lenInnn);
                            currentPlan.setRemainlen(lenInnn);
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(startTime);
                            currentPlan.setEndTime(endTime);
                            loadPlanUrl(url, video, size);
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(urlId), lenInnn, url, video, size);
                        } else if ((currentUrlType == 2) && (urlttmp == 3))    //插播  来时 有周期播
                        {
                            try {
                                endPlaySomethingfunc();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            long nowtime = System.currentTimeMillis();
                            int remainlen = currentPlan.getRemainlen() - (int) ((nowtime - currentPlan.getStartPlayTime()));///1000);
                            waitingPlan2.setUrl(currentUrl);
                            waitingPlan2.setVideo(videoUrl);
                            waitingPlan2.setSize(currentPlan.getSize());
                            waitingPlan2.setUrlId(currentPlan.getUrlId());
                            waitingPlan2.setUrlType("" + currentUrlType);
                            waitingPlan2.setRemainlen(remainlen);
                            waitingPlan2.setBeginTime(currentPlan.getBeginTime());
                            waitingPlan2.setEndTime(currentPlan.getEndTime());
                            waitingPlan2.setAllLen(lenInnn);
                            waitingPlan2Tag = 1;//备份当前的周期播，播放插播
                            PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));

                            currentUrlType = Integer.valueOf(urlType);
                            currentUrl = url;
                            currentPlan.setUrlId(urlId);
                            currentPlan.setUrlType(urlType);
                            currentPlan.setSize(size);
                            currentPlan.setAllLen(lenInnn);
                            currentPlan.setRemainlen(lenInnn);
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(startTime);
                            currentPlan.setEndTime(endTime);
                            loadPlanUrl(url, video, size);
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(urlId), lenInnn, url, video, size);

                        } else if ((currentUrlType == 2) && (urlttmp == 4)) {

                        }
                        recordWindowLog("plan currentUrlType:" + currentUrlType);
                        recordWindowLog("plan waitingPlan3Tag:" + waitingPlan3Tag);
                        recordWindowLog("plan waitingPlan2Tag:" + waitingPlan2Tag);
                    }
                } else {//非播放状态，不播放
                    recordWindowLog("play status is stop,do not play！！！ ,newplan times come " + url + "-" + len);
                }
            } else if (flag.equals(CommUtils.Socket_Flag_Plan)) {
                recordWindowLog("control plan come:" + url + "-" + len);
                update_tip.sendEmptyMessage(300);
                loadPlanUrl(url, video, size);
                PollingUtils.startPollingServiceNoRepeat(context, CommUtils.getRandomInt(), Integer.parseInt(len), url, video, size);
            } else if (flag.equals(CommUtils.Socket_Flag_NewControl)) {
                recordWindowLog("plan times come id:cmd:value:" + urlId + "-" + urlType + "-" + url);
                update_tip.sendEmptyMessage(300);
                loadControlPlanUrl(urlType, url);
            } else if (flag.equals(CommUtils.Socket_Flag_Reboot)) {
                if (url.equals("reboot")) {
//                    recordWindowLog("go to reboot....");
//                    PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);  //重启到fastboot模式
//                    pManager.reboot("");
                }
            }
        }
    }

    /**
     * 打印日志
     */
    private void sendLogMsg(String msgStr) {
        Message msg = Message.obtain();
        msg.what = 600;
        msg.obj = msgStr;
        update_tip.sendMessage(msg);
    }

    private int writeSysfs(String path, String val) {
        if (!new File(path).exists()) {
            logger.debug("writeSysfs File not found: " + path);
            Log.e("hello", "File not found: " + path);
            recordWindowLog("writeSysfs File not found: " + path + ":" + val);
            return 1;
        } else {
            logger.debug("writeSysfs file exist!");
        }
        logger.debug("writeSysfs 0:" + val);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);
            logger.debug("writeSysfs 1");
            try {
                writer.write(val);
            } finally {
                writer.close();
            }
            logger.debug("writeSysfs 2");
            return 0;

        } catch (IOException e) {
            Log.e(TAG, "IO Exception when write: " + path, e);
            return 1;
        }
    }

    private int beginPlaySomethingfunc() {

        String urlIdget = currentPlan.getUrlId();
        String urlTypeget = currentPlan.getUrlType();
        String urlget = currentUrl;    //currentPlan.getUrl();
        String playtime = CommUtils.getCurrentTimeNew();
        String urlIdStr = "@" + urlIdget + "@";
        String urlTypeString;
        if (Integer.valueOf(urlTypeget) == 1) {
            urlTypeString = "default";
        } else if (Integer.valueOf(urlTypeget) == 2) {
            urlTypeString = "zhouqi";
        } else if (Integer.valueOf(urlTypeget) == 3) {
            urlTypeString = "chabo";
        } else {
            urlTypeString = "liji";
        }
        String playtimewithjobName = CommUtils.get32MD5(urlIdStr + urlget + "_" + urlTypeString + playtime);
        beginPlaySomething(playtimewithjobName, urlget, "html", Integer.valueOf(urlTypeget), playtime);
        return 0;
    }

    private int beginPlaySomething(String jobName, String val, String type, int playlistType, String time) {
        //String time = CommUtils.getCurrentTimeNew();
        String stoptime = "";
        int updatestatus = 0;
        String urlIdget = currentPlan.getUrlId();
        //recordWindowLog("urlIdget:"+urlIdget);
        if (urlIdget.length() == 0) urlIdget = "00000";
        String playRecords = "{\"url\":\"" + val + "\",\"type\":\"loginfo\",\"devicesn\":\"" + CommUtils.PlayerDeviceSn + "\",\"id\":" + urlIdget + ",\"urltype\":\"" + type + "\",\"playtype\":" + playlistType + ",\"starttime\":\"" + time + "\",\"stoptime\":\"" + stoptime + "\",\"update\":" + updatestatus + "}";
        String playLogNameS = sp.getString("playLogName", "");
        String NewplayLogNameS = jobName + ";" + playLogNameS;
        sp.edit().putString(jobName, playRecords).apply();
        sp.edit().putString("playLogName", NewplayLogNameS).apply();
        recordWindowLog("playRecords:" + playRecords);
//			if(channel != null){
//				CommUtils.sendNettyData(channel, playRecords);
//			}
        return 0;
    }

    private int endPlaySomethingfunc() throws NumberFormatException, JSONException {

        String urlIdget = currentPlan.getUrlId();
        String urlTypeget = currentPlan.getUrlType();
        String urlget = currentUrl;    //currentPlan.getUrl();
        String playtime = CommUtils.getCurrentTimeNew();
        String urlIdStr = "@" + urlIdget + "@";
        String urlTypeString;
        if (Integer.valueOf(urlTypeget) == 1) {
            urlTypeString = "default";
        } else if (Integer.valueOf(urlTypeget) == 2) {
            urlTypeString = "zhouqi";
        } else if (Integer.valueOf(urlTypeget) == 3) {
            urlTypeString = "chabo";
        } else {
            urlTypeString = "liji";
        }
        //String playtimewithjobName = CommUtils.get32MD5(urlIdStr + urlget + "_" + urlTypeString + playtime);
        String playtimewithjobName = nowJobName;
        endPlaySomething(playtimewithjobName, urlget, "html", Integer.valueOf(urlTypeget), playtime);
        return 0;
    }

    private int endPlaySomething(String jobName, String val, String type, int playlistType, String endTime) throws JSONException {
        String stoptime = "";
        int updatestatus = 0;
        String playLogNameS = sp.getString("playLogName", "");
        if (playLogNameS.equals("")) {
            return -1;
        } else {
            int id = playLogNameS.indexOf(jobName);
            if (id == -1) {
                Log.i(TAG,"ID = -1");
            } else {
                String jsonS = sp.getString(jobName, "");
                if (jsonS.length() != 0) {
                    JSONObject jsonObject = new JSONObject(jsonS);
                    String starttime = jsonObject.getString("starttime");
                    updatestatus = 0;
                    String urlIdget = currentPlan.getUrlId();
                    String playRecords = "{\"url\":\"" + val + "\",\"type\":\"loginfo\",\"devicesn\":\"" + CommUtils.PlayerDeviceSn + "\",\"id\":" + urlIdget + ",\"urltype\":\"" + type + "\",\"playtype\":" + playlistType + ",\"starttime\":\"" + starttime + "\",\"stoptime\":\"" + endTime + "\",\"update\":" + updatestatus + "}";
                    recordWindowLog("end play playRecords:" + playRecords);
                    sp.edit().putString(jobName, playRecords).apply();

//        			if((channel != null)&&(!connFlagInactive)){
//        				updatestatus = 1;
//	        			String playRecords = "{\"url\":\""+val+"\",\"type\":\""+type+"\",\"playtype\":"+playlistType+",\"starttime\":\""+starttime+"\",\"stoptime\":\""+endTime+"\",\"update\":"+updatestatus+"}";
//	        			sp.edit().putString(jobName, playRecords).commit();
//	        			recordWindowLog("end play,send playRecords:"+playRecords);
//	        			CommUtils.sendNettyData(channel, playRecords);
//        			}else{
//        				updatestatus = 0;
//        				String playRecords = "{\"url\":\""+val+"\",\"type\":\""+type+"\",\"playtype\":"+playlistType+",\"starttime\":\""+starttime+"\",\"stoptime\":\""+endTime+"\",\"update\":"+updatestatus+"}";
//	        			sp.edit().putString(jobName, playRecords).commit();
//	        			recordWindowLog("end play,channel not ready,don't send playRecords:"+playRecords);
//        			}
                }
            }
        }
        return 0;
    }

    private void sendInfoToServer() {
        new Thread() {
            public void run() {
                if (!connFlagTag) {//没有处于传输状态
                    if ((channel == null) || (connFlagInactive)) {
                        Log.i("","channel is null");
                    } else {
                        try {
                            connFlagTag = true;//处于传输状态
                            String localLogs = sp.getString("playLogName", "");
                            //recordWindowLog("sendInfoToServer---playLogName:" + localLogs);
                            if (localLogs.length() > 0) {
                                String[] cronIdArr = localLogs.split(";");
                                //recordWindowLog("cronIdArr.length:" + cronIdArr.length);
                                for (String cronIdsig : cronIdArr) {
                                    //recordWindowLog("cronIdsig0:"+cronIdsig);
                                    if (cronIdsig.length() == 0) {
                                        //recordWindowLog("cronIdsig.length()0");
                                        continue;
                                    }
                                    try {
                                        String jsonText = sp.getString(cronIdsig, "");
                                        //recordWindowLog("jsonText1:"+jsonText);
                                        if (jsonText.length() != 0) {
                                            //recordWindowLog(getLineNumber()+":cronIdsig2:"+cronIdsig);
                                            int currentUrlTypeTmp;
                                            String currentUrlTmp;
                                            //recordWindowLog(getLineNumber()+":cronIdsig3:"+cronIdsig);
                                            JSONObject jsonObject = new JSONObject(jsonText);
                                            //recordWindowLog(getLineNumber()+":cronIdsig4:"+cronIdsig);
                                            int jobstatus = jsonObject.getInt("update");
                                            //recordWindowLog(getLineNumber()+":cronIdsig5:"+cronIdsig);
                                            String stoptimeValue = jsonObject.getString("stoptime");
                                            //recordWindowLog(getLineNumber()+":jsonText:"+jsonText);
                                            if ((jobstatus == 0) && (stoptimeValue.length() != 0)) {//未发送
                                                //recordWindowLog(getLineNumber()+":log name:" + cronIdsig);
                                                String valValue = jsonObject.getString("url");
                                                String typeValue = jsonObject.getString("urltype");
                                                int playlistTypeValue = jsonObject.getInt("playtype");
                                                String starttimeValue = jsonObject.getString("starttime");
                                                jobstatus = 1;
                                                int idstatus = jsonObject.getInt("id");
                                                String playRecords = "{\"url\":\"" + valValue + "\",\"type\":\"loginfo\",\"devicesn\":\"" + CommUtils.PlayerDeviceSn + "\",\"id\":" + idstatus + ",\"urltype\":\"" + typeValue + "\",\"playtype\":" + playlistTypeValue + ",\"starttime\":\"" + starttimeValue + "\",\"stoptime\":\"" + stoptimeValue + "\",\"update\":" + jobstatus + "}";
                                                //recordWindowLog(getLineNumber()+":before sendNettyData!!!");
                                                CommUtils.sendNettyData(channel, playRecords);
                                                //recordWindowLog(getLineNumber()+":after sendNettyData!!!");
                                                sp.edit().putString(cronIdsig, playRecords).apply();
                                            }
                                        }
                                    } catch (Exception e) {
                                        //Log.e("Exception when JSONObject", e.toString());
                                        //recordWindowLog(getLineNumber()+":Exception!!!");
                                    }
                                }
                            }
                            connFlagTag = false;
                        } catch (Exception e) {
                            Log.e("Exception when sendPointerSync", e.toString());
                            recordWindowLog(getLineNumber() + ":Exception!!!");
                            connFlagTag = false;
                        }
                    }
                }
            }
        }.start();
    }

    private void sendKeyCode(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    Log.e("Exception when sendPointerSync", e.toString());
                }
            }
        }.start();
    }

    private void load_playlistPlan_controlPlan_rebootPlan() {
        new Thread() {
            public void run() {
                try {
                    //rebootJob();
                    //获取本地化存储的播放计划节目单
                    loadplaylistdelplaylist();
                } catch (Exception e) {
                    Log.e("Exception when load_playlistPlan_controlPlan_rebootPlan", e.toString());
                    recordWindowLog("load_playlistPlan_controlPlan_rebootPlan err!!!");
                }
            }
        }.start();
    }

    public int planControl(int cmd, String value) throws IOException, InterruptedException {
        int ret = -1;
        //recordWindowLog("cmd:" + cmd);
        //recordWindowLog("value:" + value);
        if (currentStatus == 3) { //关机状态下，只能开机
            if (cmd != 2) {
                //recordWindowLog("关机状态下，只能开机！！！");
                return ret;
            }
        }
        switch (cmd) {
            case 1://关机
                if (currentStatus != 3) {
                    blankView.setVisibility(View.VISIBLE);
                    map_view.setVisibility(View.GONE);
                    map_view.loadUrl("about:blank");
                    videoView.setVisibility(View.GONE);
                    videoView.stop();
                    currentStatus = 3;
                    //writeSysfs("/sys/class/graphics/fb0/blank","1");
                    //sendKeyCode(KeyEvent.KEYCODE_POWER);
                    ret = 0;
                }
                break;
            case 2://开机
                if (currentStatus == 3) {
                    loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                    blankView.setVisibility(View.GONE);
                    currentStatus = 1;
                    //writeSysfs("/sys/class/graphics/fb0/blank","0");
                    //sendKeyCode(KeyEvent.KEYCODE_POWER);
                    ret = 0;
                }
                break;
            case 3://音量
                int volume;
                volume = Integer.parseInt(value.trim());
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                ret = 0;
                break;
            case 4://时间
                try {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                    long when = 0;
                    when = sdf1.parse("2016:08:18-09:50:50").getTime();
                    //recordWindowLog("2016:08:18-09:50:50 的字符串是:" + when);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                    //前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
                    java.util.Date dt = new Date(Long.valueOf(value));
                    String sDateTime = sdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
                    //recordWindowLog("时间:" + sDateTime);
                    CommUtils.setDateTimeNew(value, "yyyy:MM:dd-HH:mm:ss");
                    if (g_sync_time_once == 0) {
                        //recordWindowLog("装载计划!!!");
                        load_playlistPlan_controlPlan_rebootPlan();
                        g_sync_time_once = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ret = 0;
                break;
            case 5://设置GJNO
                gj_no = Integer.parseInt(value.trim());
                CommUtils.PlayerGJNO = gj_no;
                sp.edit().putInt("playerGJNO_yj", gj_no).apply();
                ret = 0;
                break;
            case 6://设置播放器名
                appName = value;
                CommUtils.PlayerName = appName;
                sp.edit().putString("playerName_yj", appName).apply();
                ret = 0;
                break;
            case 7://停止
                //map_view.loadUrl("about:blank");
                currentStatus = 2;

                map_view.setVisibility(View.GONE);
                map_view.loadUrl("about:blank");
                videoView.setVisibility(View.GONE);
                videoView.stop();
                //currentStatus = 3;
                //writeSysfs("/sys/class/graphics/fb0/blank","1");
                //sendKeyCode(KeyEvent.KEYCODE_POWER);

                ret = 0;
                break;
            case 8://播放

//			try {
//	        	if(videoView != null){
//	        		videoView.playfromvideotime(videoviewplaybacktime);
//	        	}
//            } catch (Exception e) {
//            	recordWindowLog(getLineNumber()+":errr:"+e.getMessage());
//            }

                loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                currentStatus = 1;

                //Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
                //mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //context.startActivity(mainActivityIntent);


//			Intent mainActivityIntent = new Intent();  // 要启动的Activity
//            mainActivityIntent.setClass(MainActivity.this,SelfInfoActivity.class);
//            MainActivity.this.startActivity(mainActivityIntent);


                ret = 0;
                break;
            case 9://重启
                PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);  //重启到fastboot模式
                pManager.reboot("");
                ret = 0;
                break;
            case 20://设置ftp密码
                String[] ftppram = value.split(";");
                int a = ftppram.length;
                if (a == 3) {
                    try {
                        if (!hackconfigfiletttag) {
                            if ((ftppram[0].length() > 0) && (ftppram[1].length() > 0) && (ftppram[2].length() > 0)) {
                                if (!CommUtils.Ftpserver.equals(ftppram[0])) {
                                    CommUtils.Ftpserver = ftppram[0];
                                    sp.edit().putString("ftpserveraddr", ftppram[0]).apply();

                                    CommUtils.playlisthtmladdrhead = "http://" + CommUtils.Ftpserver + "/";
                                    CommUtils.playlisthtmladdrheadTag = CommUtils.Ftpserver.substring(CommUtils.Ftpserver.indexOf("."), CommUtils.Ftpserver.length());
                                    CommUtils.playlisthtmlvideoftpaddr = CommUtils.Ftpserver;
                                    recordWindowLog(getLineNumber() + ":2CommUtils.playlisthtmladdrhead:" + CommUtils.playlisthtmladdrhead);
                                    recordWindowLog(getLineNumber() + ":2CommUtils.playlisthtmladdrheadTag:" + CommUtils.playlisthtmladdrheadTag);
                                    recordWindowLog(getLineNumber() + ":2CommUtils.playlisthtmlvideoftpaddr:" + CommUtils.playlisthtmlvideoftpaddr);
                                    configupdate = true;
                                }
                                if (!CommUtils.Ftpusr.equals(ftppram[1])) {
                                    CommUtils.Ftpusr = ftppram[1];
                                    sp.edit().putString("ftpserveruser", ftppram[1]).apply();
                                    configupdate = true;
                                }
                                if (!CommUtils.Ftppasswd.equals(ftppram[2])) {
                                    CommUtils.Ftppasswd = ftppram[2];
                                    sp.edit().putString("ftpserverpasswd", ftppram[2]).apply();
                                    configupdate = true;
                                }
                                if (configupdate) {
                                    recordWindowLog(getLineNumber() + ":设置ftp密码 Ftpserver:" + CommUtils.Ftpserver);
                                    recordWindowLog(getLineNumber() + ":设置ftp密码 Ftpusr:" + CommUtils.Ftpusr);
                                    recordWindowLog(getLineNumber() + ":设置ftp密码 Ftppasswd:" + CommUtils.Ftppasswd);
                                }
                                checkifautoupdate();
                            }
                        }
                    } catch (Exception e) {
                        recordWindowLog(getLineNumber() + ":设置ftp密码 Exception!!!");
                    }
                }
                break;
            case 21://是否跳转配置页命令，value为1时，播放器回到主界面，value为0时播放器停留在配置界面
                int isSetted = Integer.parseInt(value.trim());
                recordWindowLog("cmd--21  value--" + isSetted);
                Log.e("-------", "cmd--21  value--" + isSetted);
                if (isSetted == 0) {
                    sp.edit().putString("playerName_yj", "gj_" + CommUtils.PlayerDeviceSn).apply();
                    ifNeedToInfoActivity = true;
                    if (isDelayDealConducted && !isActivityOnPause)
                        startActivityForResult(new Intent(MainActivity.this, SelfInfoActivity.class), 100);
                } else {
                    ifNeedToInfoActivity = false;
                    startActivity(new Intent(MainActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
                break;
            default:
                break;
        }
        return ret;
    }

    public void rebootJob() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("cmd", "reboot");
        recordWindowLog("add rebootJob!");
        String cron = "0 0 4 * * ? *";
        //String cron="0 0/2 19 * * ? ";
        QuartzManager.addJob("appRebootJob", "com.zr.webview.util.CronRebootJob", cron, params);
        recordWindowLog("add rebootJob end!");
    }

    /**
     * 处理命令事件
     */
    private void parseCmdEvent(DataActivityEvent event, String text) {
        if (text.contains("\'type\':\'servercmd\',\'cmd\':4,\'value\':")) {
            Log.i("","");
        } else {
            sendLogMsg("socket data coming " + text);
        }
        try {
            update_tip.sendEmptyMessage(300);
            JSONObject jsonObject = new JSONObject(text);
            String type = jsonObject.getString("type");
            //recordWindowLog("type:" + type);
            //播放计划
            if (type.equals("oldplaylist")) {//旧的播放计划，已废弃
                playOldList(jsonObject);
            } else if (type.equals("playlist")) {//播放计划
                playNewList(jsonObject, text);
            } else if (type.equals("controlplan")) { //控制计划
                int id = jsonObject.getInt("id");
                //int id = 1234;
                int cmd = jsonObject.getInt("cmd"); //1停止命令，2播放命令，3音量设置，4时间设置，5 gjno设置，6 app名称修改
                String playlistOP = jsonObject.getString("op");
                if (playlistOP.equals("del")) {
                    recordWindowLog("id:" + id + ",cmd:" + cmd);
                    String urlIdStr = "@" + String.valueOf(id) + "@";
                    String localCrons = sp.getString("control_plan_id_buffer", "");
                    String jobName = CommUtils.get32MD5(urlIdStr + String.valueOf(cmd));
                    if (localCrons.length() > 0) {
                        String cronIdsig1 = String.valueOf(id) + ";";
                        localCrons.replace(cronIdsig1, "");
                        String controlPlanName = "CPName:" + id;
                        sp.edit().putString("control_plan_id_buffer", localCrons).apply();
                        sp.edit().remove(controlPlanName).apply();
                        QuartzManager.removeJob(jobName);
                    }
                } else if (playlistOP.equals("add")) {
                    String value = jsonObject.getString("value");
                    recordWindowLog("id:" + id + ",cmd:" + cmd);
                    recordWindowLog("value:" + value);
                    String cron = jsonObject.getString("plancron");

                    String startdate0 = jsonObject.getString("startdate");
                    //String enddate0 = jsonObject.getString("enddate");
                    String starttime = jsonObject.getString("starttime");
                    //String endtime = jsonObject.getString("endtime");

                    String startdate = startdate0.replaceAll("-", ":");
                    //String enddate = enddate0.replaceAll("-", ":");

                    String startdatetime = startdate + "-" + starttime;
                    String enddatetime;// = enddate+"-"+endtime;

                    recordWindowLog("before startdatetime:" + startdatetime);
                    //recordWindowLog("before enddatetime:" + enddatetime);

                    long startdatetimewhen;
                    long enddatetimewhen;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                    startdatetimewhen = sdf.parse(startdatetime).getTime();
                    enddatetimewhen = startdatetimewhen + 24 * 3600 * 1000;
                    startdatetime = String.valueOf(startdatetimewhen);
                    enddatetime = String.valueOf(enddatetimewhen);
                    recordWindowLog("after startdatetime:" + startdatetime);
                    recordWindowLog("after enddatetime:" + enddatetime);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("value", value);
                    params.put("id", String.valueOf(id));
                    params.put("cmd", String.valueOf(cmd));
                    params.put("startTime", startdatetime);
                    params.put("endTime", enddatetime);
                    String urlIdStr = "@" + String.valueOf(id) + "@";
                    String jobName = CommUtils.get32MD5(urlIdStr + String.valueOf(cmd));
                    recordWindowLog("jobName:" + jobName);
                    //recordWindowLog("localCron:" + localCron);
                    //recordWindowLog("newCron:" + newCron);
                    String IdBuffer = sp.getString("control_plan_id_buffer", "");
                    String NewIdBuffer = IdBuffer + ";" + String.valueOf(id);
                    String controlPlanName = "CPName:" + id;
                    sp.edit().putString(controlPlanName, text).apply();
                    sp.edit().putString("control_plan_id_buffer", NewIdBuffer).apply();
                    QuartzManager.addJob(jobName, "com.zr.webview.util.ControlCronJob", cron, params);
                }
            } else if (type.equals("servercmd")) { //控制命令
                int cmd = jsonObject.getInt("cmd"); //1停止命令，2播放命令，3音量设置，4时间设置，5 gjno设置，6 app名称修改
                String value = jsonObject.getString("value");
                //recordWindowLog("cmd:" + cmd);
                //recordWindowLog("value:" + value);
                loadControlPlanUrl(String.valueOf(cmd), value);
            }
        } catch (Exception e) {
            Log.e(TAG, "messageFromDataActivity error " + e.getMessage());
            sendLogMsg("ERROR!!!! messageFromDataActivity  " + e.getMessage());
        }
    }

    private void playOldList(JSONObject jsonObject) throws JSONException {
        currentUrlType = jsonObject.getInt("plisttype");
        currentUrl = jsonObject.getString("url");
        recordWindowLog("currentUrlType:" + currentUrlType);
        recordWindowLog("currentUrl:" + currentUrl);
        String tmpVideoUrl = "";
        String videoSize = "";
        if (jsonObject.has("videoUrl")) {
            tmpVideoUrl = jsonObject.getString("videoUrl");
            int videoW = jsonObject.getInt("videoW");
            int videoH = jsonObject.getInt("videoH");
            int videoX = jsonObject.getInt("videoX");
            int videoY = jsonObject.getInt("videoY");
            videoSize = videoW + defaulVideoSplit + videoH + defaulVideoSplit + videoX + defaulVideoSplit + videoY;
        }
        if (currentUrlType == 1) {//默认播放
            defaultUrl = jsonObject.getString("url");
            defaulVideoUrl = tmpVideoUrl;
            defaulVideoSize = videoSize;
            recordWindowLog("defaultUrl:" + defaultUrl);
            recordWindowLog("defaulVideoUrl:" + defaulVideoUrl);
            recordWindowLog("defaulVideoSize:" + defaulVideoSize);
            sp.edit().putString("default_url", defaultUrl).apply();
            loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
        } else if (currentUrlType == 2) {//立即播放
            int playSec = jsonObject.getInt("playtimelength");
            recordWindowLog("playSec:" + playSec);
            loadPlanUrl(currentUrl, tmpVideoUrl, videoSize);
            PollingUtils.startPollingServiceNoRepeat(context, CommUtils.getRandomInt(), playSec, currentUrl, tmpVideoUrl, videoSize);
        } else if (currentUrlType == 3) {//计划播放
            int playSec = jsonObject.getInt("playtimelength");
            String cron = jsonObject.getString("planCron");
            recordWindowLog("playSec:" + playSec);
            recordWindowLog("cron:" + cron);
            Map<String, String> params = new HashMap<String, String>();
            params.put("url", currentUrl);
            params.put("len", String.valueOf(playSec));
            params.put("vid", tmpVideoUrl);
            params.put("size", videoSize);
            String jobName = CommUtils.get32MD5(currentUrl + "_" + cron.replaceAll(" ", "--"));
            String localCron = sp.getString("cron_url", "");
            String newCron = localCron + ";" + currentUrl + "_" + cron.replaceAll(" ", "--") + "_" + playSec + "_" + tmpVideoUrl + "_" + videoSize;
            recordWindowLog("jobName:" + jobName);
            recordWindowLog("localCron:" + localCron);
            recordWindowLog("newCron:" + newCron);
            sp.edit().putString("cron_url", newCron).apply();
            QuartzManager.addJob(jobName, "com.zr.webview.util.CronJob", cron, params);
        }
    }

    private void playNewList(JSONObject jsonObject, String text) throws Exception {
        int currentUrlTypeTmp = jsonObject.getInt("plisttype"); //当前播出单类型
        String currentUrlTmp = jsonObject.getString("url");     //当前播出单地址
        String playlistOP = jsonObject.getString("op");

        //屏幕打印，便于查看日志
        recordWindowLog("currentUrlTypeTmp:" + currentUrlTypeTmp);
        recordWindowLog("currentUrlTmp:" + currentUrlTmp);

        String tmpVideoUrl = "";    //播出单包含视频时的视频地址
        String videoSize = "";      //播出单中视频的规格信息

        //如果当前播出单记录信息中有video表示，表明是视频播出单
        if (jsonObject.has("videourl")) {
            tmpVideoUrl = jsonObject.getString("videourl");
            int videoW = jsonObject.getInt("videow");
            int videoH = jsonObject.getInt("videoh");
            int videoX = jsonObject.getInt("videox");
            int videoY = jsonObject.getInt("videoy");

            videoSize = videoW + defaulVideoSplit + videoH + defaulVideoSplit + videoX + defaulVideoSplit + videoY;
        }
        if (playlistOP.equals("add")) {
            if (currentUrlTypeTmp == 1) {//默认播放(播放等级1,垫片播)
                //全局变量赋值
                defaultUrl = jsonObject.getString("url");
                defaulVideoUrl = tmpVideoUrl;
                defaulVideoSize = videoSize;

                int urlId = jsonObject.getInt("id");

                String listStr = "";
                try {
                    listStr = jsonObject.getString("list");
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":no list playlist!!!:" + e.getMessage());
                }

                recordWindowLog("defaultUrl:" + defaultUrl);
                recordWindowLog("defaulVideoUrl:" + defaulVideoUrl);
                recordWindowLog("defaulVideoSize:" + defaulVideoSize);

                //需要本地化的播出单信息
                sp.edit().putString("default_list", listStr).apply();
                sp.edit().putString("default_url", defaultUrl).apply();
                sp.edit().putString("default_url_id", String.valueOf(urlId)).apply();

                if (currentUrlType == 1) {
                    try {
                        endPlaySomethingfunc();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //currentUrlType = currentUrlTypeTmp;
                    if (listStr.isEmpty()) {//如果没有list数据，默认垫片逻辑
                        sp.edit().putBoolean("is_order_playlist_type1", false).apply();
                        isOrderListPlay = false;
                        currentUrl = defaultUrl;
                        currentPlan.setUrlId(String.valueOf(urlId));
                        currentPlan.setUrlType(String.valueOf(currentUrlTypeTmp));
                        currentPlan.setSize(videoSize);
                        //currentPlan.setAllLen(playSec);
                        currentPlan.setStartPlayTime(System.currentTimeMillis());
                        currentPlan.setUrl(currentUrl);

                        loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                    } else {//如果list有数据，垫片的顺播逻辑
                        sp.edit().putBoolean("is_order_playlist_type1", true).apply();
                        isOrderListPlay = true;
                        orderListIndex = 0;
                        //解析数组数据
                        String string = "{list:" + listStr + "}";
                        OrderPlayModel model = gson.fromJson(string, OrderPlayModel.class);

                        //建立与接收的顺播播出单集合个数相同的存储播放model的集合
                        listOrderSavePlanModel = new ArrayList<PlanModel>();
                        for (OrderPlayListModel listModel : model.getList()) {
                            PlanModel planModel = new PlanModel();
                            planModel.setUrl(listModel.getUrl());
                            planModel.setVideo(videoUrl);
                            planModel.setSize(currentPlan.getSize());
                            planModel.setUrlId(currentPlan.getUrlId());
                            planModel.setUrlType("" + currentUrlType);
                            planModel.setRemainlen(listModel.getPlaytimelength());
                            planModel.setBeginTime(currentPlan.getBeginTime());
                            planModel.setEndTime(currentPlan.getEndTime());
                            planModel.setAllLen(currentPlan.getAllLen());
                            listOrderSavePlanModel.add(planModel);
                        }
                        currentUrlType = currentUrlTypeTmp;
                        currentUrl = model.getList().get(0).getUrl();
                        currentPlan.setUrlId(String.valueOf(urlId));
                        currentPlan.setUrlType(String.valueOf(currentUrlTypeTmp));
                        currentPlan.setSize(videoSize);
                        currentPlan.setAllLen(model.getList().get(0).getPlaytimelength());
                        currentPlan.setStartPlayTime(System.currentTimeMillis());
                        currentPlan.setUrl(currentUrl);
                        loadPlanUrl(model.getList().get(0).getUrl(), tmpVideoUrl, videoSize);
                        if (orderListIndex < listOrderSavePlanModel.size() - 1)
                            orderListIndex++;
                        else
                            orderListIndex = 0;
                        PollingUtils.startPollingServiceNoRepeat(context, urlId,
                                model.getList().get(0).getPlaytimelength(),
                                model.getList().get(0).getUrl(), tmpVideoUrl, videoSize);
                    }
                }
            } else if ((currentUrlTypeTmp == 2) || (currentUrlTypeTmp == 3)) {//周期播放  插播
                int playSec = jsonObject.getInt("playtimelength");
                String cron = jsonObject.getString("plancron");
                String startdate0 = jsonObject.getString("startdate");
                String enddate0 = jsonObject.getString("enddate");
                String starttime = jsonObject.getString("starttime");
                String endtime = jsonObject.getString("endtime");

                String startdate = startdate0.replaceAll("-", ":");
                String enddate = enddate0.replaceAll("-", ":");

                String startdatetime = startdate + "-" + starttime;
                String enddatetime = enddate + "-" + endtime;

                recordWindowLog("before startdatetime:" + startdatetime);
                recordWindowLog("before enddatetime:" + enddatetime);

                long startdatetimewhen;
                long enddatetimewhen;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                startdatetimewhen = sdf.parse(startdatetime).getTime();
                enddatetimewhen = sdf.parse(enddatetime).getTime();

                if ((startdatetimewhen + playSec) > enddatetimewhen)    //结束时间超过时间段，则重新写播放时长
                {
                    playSec = (int) (enddatetimewhen - startdatetimewhen);
                    recordWindowLog("receive re lenInnn:" + playSec);
                }
                startdatetime = String.valueOf(startdatetimewhen);
                enddatetime = String.valueOf(enddatetimewhen);
                recordWindowLog("after startdatetime:" + startdatetime);
                recordWindowLog("after enddatetime:" + enddatetime);
                int urlId = jsonObject.getInt("id");
                recordWindowLog("playSec:" + playSec);
                recordWindowLog("cron:" + cron);
                recordWindowLog("urlId:" + urlId);
                Map<String, String> params = new HashMap<String, String>();
                params.put("url", currentUrlTmp);
                params.put("len", String.valueOf(playSec));
                params.put("vid", tmpVideoUrl);
                params.put("size", videoSize);
                params.put("urlId", String.valueOf(urlId));
                params.put("urlType", String.valueOf(currentUrlTypeTmp));
                params.put("startTime", startdatetime);
                params.put("endTime", enddatetime);
                String urlIdStr = "@" + String.valueOf(urlId) + "@";
                String jobName = CommUtils.get32MD5(urlIdStr + currentUrlTmp + "_" + cron.replaceAll(" ", "--"));
                String localCron = sp.getString("cron_url", "");
                String newCron = localCron + ";" + urlIdStr + "_" + currentUrlTypeTmp + "_" + startdatetime + "_" + enddatetime + "_" + currentUrlTmp + "_" + cron.replaceAll(" ", "--") + "_" + playSec + "_" + tmpVideoUrl + "_" + videoSize + urlIdStr;
                recordWindowLog("jobName:" + jobName);
                //recordWindowLog("localCron:" + localCron);
                //recordWindowLog("newCron:" + newCron);
                String IdBuffer = sp.getString("id_buffer", "");
                String NewIdBuffer = IdBuffer + ";" + String.valueOf(urlId);
                sp.edit().putString(String.valueOf(urlId), text).apply();
                sp.edit().putString("id_buffer", NewIdBuffer).apply();
                //sp.edit().putString("cron_url", newCron).commit();
                QuartzManager.addJob(jobName, "com.zr.webview.util.CronJob", cron, params);
            } else if (currentUrlTypeTmp == 4) {//立即播放
                int playSec = jsonObject.getInt("playtimelength");
                int urlId = jsonObject.getInt("id");
                String enddate0 = jsonObject.getString("enddate");
                String endtime = jsonObject.getString("endtime");
                String enddate = "";
                String enddatetime = "";

                String urlIdStr = "@" + String.valueOf(urlId) + "@";
                String newCron = urlIdStr + "_" + currentUrlTypeTmp + "_" + enddate0 + "_" + endtime + "_" + currentUrlTmp + "_" + playSec + urlIdStr;
                //recordWindowLog("newCron:" + newCron);
                //sp.edit().putString("rn_cron_url", newCron).commit();

                sp.edit().putString(String.valueOf(urlId), text).apply();
                sp.edit().putString("rn_id_buffer", String.valueOf(urlId)).apply();

//            			if(enddate0.equals("")||endtime.equals("")){
//
//            			}else{
//            				enddate = enddate0.replaceAll("-", ":");
//            				enddatetime = enddate+"-"+endtime;
//            				long enddatetimewhen;
//                			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss");
//                			enddatetimewhen = sdf.parse(enddatetime).getTime();
//                			playSec=(int)(enddatetimewhen-System.currentTimeMillis());
//                    		recordWindowLog("receive rnow playlist re lenInnn:" + playSec);
//            			}
                if(currentUrlType == 1){
                    //是垫片播的顺播逻辑时
                    if(sp.getBoolean("is_order_playlist_type1",false)){
                        waitingPlan1Tag = 1;
                        waitingPlan1.setUrl(currentUrl);
                        waitingPlan1.setVideo(videoUrl);
                        waitingPlan1.setSize(currentPlan.getSize());
                        waitingPlan1.setUrlId(currentPlan.getUrlId());
                        waitingPlan1.setUrlType("" + currentUrlType);
                        waitingPlan1.setRemainlen(currentPlan.getRemainlen());
                        waitingPlan1.setBeginTime(currentPlan.getBeginTime());
                        waitingPlan1.setEndTime(currentPlan.getEndTime());
                        waitingPlan1.setAllLen(currentPlan.getAllLen());
                        PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                    }
                } else if (currentUrlType == 2) {
                    long nowtime = System.currentTimeMillis();
                    int remainlen = currentPlan.getRemainlen() - (int) ((nowtime - currentPlan.getStartPlayTime()));///1000);
                    waitingPlan2.setUrl(currentUrl);
                    waitingPlan2.setVideo(videoUrl);
                    waitingPlan2.setSize(currentPlan.getSize());
                    waitingPlan2.setUrlId(currentPlan.getUrlId());
                    waitingPlan2.setUrlType("" + currentUrlType);
                    waitingPlan2.setRemainlen(remainlen);
                    waitingPlan2.setBeginTime(currentPlan.getBeginTime());
                    waitingPlan2.setEndTime(currentPlan.getEndTime());
                    waitingPlan2.setAllLen(currentPlan.getAllLen());
                    waitingPlan2Tag = 1;//备份当前的周期播，播放插播
                    PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                } else if (currentUrlType == 3) {
                    long nowtime = System.currentTimeMillis();
                    int remainlen = currentPlan.getRemainlen() - (int) ((nowtime - currentPlan.getStartPlayTime()));///1000);
                    waitingPlan3.setUrl(currentUrl);
                    waitingPlan3.setVideo(videoUrl);
                    waitingPlan3.setSize(currentPlan.getSize());
                    waitingPlan3.setUrlId(currentPlan.getUrlId());
                    waitingPlan3.setUrlType("" + currentUrlType);
                    waitingPlan3.setRemainlen(remainlen);
                    waitingPlan3.setBeginTime(currentPlan.getBeginTime());
                    waitingPlan3.setEndTime(currentPlan.getEndTime());
                    waitingPlan3.setAllLen(currentPlan.getAllLen());
                    waitingPlan3Tag = 1;//备份当前的周期播，播放插播
                    PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                }
                try {
                    endPlaySomethingfunc();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                recordWindowLog("playSec:" + playSec);
                currentUrlType = currentUrlTypeTmp;
                currentUrl = currentUrlTmp;
                currentPlan.setUrlId(String.valueOf(urlId));
                currentPlan.setUrlType(String.valueOf(currentUrlTypeTmp));
                currentPlan.setSize(videoSize);
                currentPlan.setAllLen(playSec);
                currentPlan.setStartPlayTime(System.currentTimeMillis());
                currentPlan.setUrl(currentUrl);
                //currentPlan.setBeginTime(startTime);
                //currentPlan.setEndTime(endTime);
                loadPlanUrl(currentUrlTmp, tmpVideoUrl, videoSize);

                PollingUtils.startPollingServiceNoRepeat(context, urlId, playSec, currentUrlTmp, tmpVideoUrl, videoSize);
            }
        } else if (playlistOP.equals("del")) {
            if (currentUrlTypeTmp == 1) {//默认播放
                Log.i(TAG,"playlistOp is del and type = 1");
            } else if ((currentUrlTypeTmp == 2) || (currentUrlTypeTmp == 3)) {//周期播放
                int playSec = jsonObject.getInt("playtimelength");
                String cron = jsonObject.getString("plancron");
                int urlId = jsonObject.getInt("id");
                recordWindowLog("playSec:" + playSec);
                recordWindowLog("cron:" + cron);
                recordWindowLog("urlId:" + urlId);
                String urlIdStr111 = "@" + String.valueOf(urlId) + "@";
                String urlIdStr = ";" + urlIdStr111;
                String urlIdStrEnd = "@" + String.valueOf(urlId) + "@";
                String jobName = CommUtils.get32MD5(urlIdStr111 + currentUrlTmp + "_" + cron.replaceAll(" ", "--"));
                //String localCron =  sp.getString("cron_url", "");
                //int localCronBegin = localCron.indexOf(urlIdStr);
                int localCronBegin = 1;
                if (localCronBegin != -1) {
                    //int localCronEnd = localCron.indexOf(urlIdStrEnd,(localCronBegin+urlIdStr.length()));
                    int localCronEnd = 1;
                    if (localCronEnd != -1) {
                        String localCrons = sp.getString("id_buffer", "");
                        if (localCrons.length() > 0) {
                            String cronIdsig1 = String.valueOf(urlId) + ";";
                            localCrons.replace(cronIdsig1, "");
                            sp.edit().putString("id_buffer", localCrons).apply();
                            sp.edit().remove(String.valueOf(urlId)).apply();
                            QuartzManager.removeJob(jobName);// , "com.zr.webview.util.CronJob", cron , params);
                        }
                        if (urlId == Integer.valueOf(currentPlan.getUrlId())) {    //删掉的是当前在播放的id
                            try {
                                endPlaySomethingfunc();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (Integer.valueOf(currentPlan.getUrlType()) == 3) {
                                if (waitingPlan2Tag == 1) {
                                    PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                                    currentUrl = waitingPlan2.getUrl();
                                    currentUrlType = Integer.valueOf(waitingPlan2.getUrlType());
                                    currentPlan.setUrlId(waitingPlan2.getUrlId());
                                    currentPlan.setUrlType(waitingPlan2.getUrlType());
                                    currentPlan.setSize(waitingPlan2.getSize());
                                    currentPlan.setAllLen(waitingPlan2.getAllLen());
                                    currentPlan.setRemainlen(waitingPlan2.getRemainlen());
                                    currentPlan.setStartPlayTime(System.currentTimeMillis());
                                    currentPlan.setBeginTime(waitingPlan2.getBeginTime());
                                    currentPlan.setEndTime(waitingPlan2.getEndTime());
                                    waitingPlan2Tag = 0;
                                    loadPlanUrl(waitingPlan2.getUrl(), waitingPlan2.getVideo(), waitingPlan2.getSize());
                                    PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan2.getUrlId()), waitingPlan2.getRemainlen(), waitingPlan2.getUrl(), waitingPlan2.getVideo(), waitingPlan2.getSize());
                                } else {
                                    PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                                    currentUrl = defaultUrl;
                                    currentUrlType = 1;
                                    currentPlan.setUrlId(defaultUrlId);
                                    currentPlan.setUrlType(String.valueOf(1));
                                    currentPlan.setSize(defaulVideoSize);
                                    loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                                }
                            } else if (Integer.valueOf(currentPlan.getUrlType()) == 2) {
                                PollingUtils.stopPollingServiceNoRepeat(context, Integer.valueOf(currentPlan.getUrlId()));
                                currentUrl = defaultUrl;
                                currentUrlType = 1;
                                currentPlan.setUrlId(defaultUrlId);
                                currentPlan.setUrlType(String.valueOf(1));
                                currentPlan.setSize(defaulVideoSize);
                                loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                            }
                        } else {
                            if (waitingPlan2Tag == 1) {
                                if (urlId == Integer.valueOf(waitingPlan2.getUrlId())) {    //删掉的是备份的id
                                    waitingPlan2Tag = 0;
                                }
                            } else if (waitingPlan3Tag == 1) {
                                if (urlId == Integer.valueOf(waitingPlan3.getUrlId())) {    //删掉的是备份的id
                                    waitingPlan3Tag = 0;
                                }
                            }
                        }
                    } else {
                        recordWindowLog("urlId:" + urlIdStr + " not found string end!!!");
                    }
                } else {
                    recordWindowLog("urlId:" + urlIdStr + " not found!!!");
                }
            } else if (currentUrlTypeTmp == 4) {//立即播放
                int playSec = jsonObject.getInt("playtimelength");
                int urlId = jsonObject.getInt("id");
                recordWindowLog("playSec:" + playSec);
                //当前播放的是立即播才能删除
                if ((Integer.valueOf(currentPlan.getUrlId()) == urlId)
                        && (Integer.valueOf(currentPlan.getUrlType()) == currentUrlTypeTmp)) {
                    try {
                        endPlaySomethingfunc();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    PollingUtils.stopPollingServiceNoRepeat(context, urlId);
                    sp.edit().putString("rn_cron_url", "").apply();
                    if (waitingPlan3Tag == 1) {
                        currentUrl = waitingPlan3.getUrl();
                        videoUrl = waitingPlan3.getVideo();
                        currentUrlType = Integer.valueOf(waitingPlan3.getUrlType());
                        currentPlan.setUrlId(waitingPlan3.getUrlId());
                        currentPlan.setUrlType(waitingPlan3.getUrlType());
                        currentPlan.setStartPlayTime(System.currentTimeMillis());
                        currentPlan.setBeginTime(waitingPlan3.getBeginTime());
                        currentPlan.setEndTime(waitingPlan3.getEndTime());
                        currentPlan.setSize(waitingPlan3.getSize());
                        int lenInnn = waitingPlan3.getRemainlen();
                        waitingPlan3Tag = 0;
                        loadPlanUrl(currentUrl, videoUrl, defaulVideoSize);
                        PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan3.getUrlId()), lenInnn, currentUrl, videoUrl, waitingPlan3.getSize());
                    } else if (waitingPlan2Tag == 1) {
                        currentUrl = waitingPlan2.getUrl();
                        videoUrl = waitingPlan2.getVideo();
                        currentUrlType = Integer.valueOf(waitingPlan2.getUrlType());
                        currentPlan.setUrlId(waitingPlan2.getUrlId());
                        currentPlan.setUrlType(waitingPlan2.getUrlType());
                        currentPlan.setStartPlayTime(System.currentTimeMillis());
                        currentPlan.setBeginTime(waitingPlan2.getBeginTime());
                        currentPlan.setEndTime(waitingPlan2.getEndTime());
                        currentPlan.setSize(waitingPlan2.getSize());
                        int lenInnn = waitingPlan2.getRemainlen();
                        waitingPlan2Tag = 0;
                        loadPlanUrl(currentUrl, videoUrl, defaulVideoSize);
                        PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan2.getUrlId()), lenInnn, currentUrl, videoUrl, waitingPlan2.getSize());
                    } else {
                        recordWindowLog("url " + text + " play done");
                        //startLoad();
                        currentUrl = defaultUrl;
                        currentUrlType = 1;
                        currentPlan.setUrlId(defaultUrlId);
                        currentPlan.setUrlType(String.valueOf(1));
                        currentPlan.setSize(defaulVideoSize);
                        loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                    }
                }
            }
        } else if (playlistOP.equals("upd")) {
            if (currentUrlTypeTmp == 1) {//默认播放
                defaultUrl = jsonObject.getString("url");
                defaulVideoUrl = tmpVideoUrl;
                defaulVideoSize = videoSize;
                recordWindowLog("defaultUrl:" + defaultUrl);
                recordWindowLog("defaulVideoUrl:" + defaulVideoUrl);
                recordWindowLog("defaulVideoSize:" + defaulVideoSize);
                sp.edit().putString("default_url", defaultUrl).apply();
                loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
            } else if (currentUrlTypeTmp == 2) {//计划播放
                int playSec = jsonObject.getInt("playtimelength");
                String cron = jsonObject.getString("plancron");

                String startdate0 = jsonObject.getString("startdate");
                String enddate0 = jsonObject.getString("enddate");
                String starttime = jsonObject.getString("starttime");
                String endtime = jsonObject.getString("endtime");

                String startdate = startdate0.replaceAll("-", ":");
                String enddate = enddate0.replaceAll("-", ":");

                String startdatetime = startdate + "-" + starttime;
                String enddatetime = enddate + "-" + endtime;

                recordWindowLog("before startdatetime:" + startdatetime);
                recordWindowLog("before enddatetime:" + enddatetime);

                long startdatetimewhen;
                long enddatetimewhen;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                startdatetimewhen = sdf.parse(startdatetime).getTime();
                enddatetimewhen = sdf.parse(enddatetime).getTime();
                startdatetime = String.valueOf(startdatetimewhen);
                enddatetime = String.valueOf(enddatetimewhen);
                recordWindowLog("after startdatetime:" + startdatetime);
                recordWindowLog("after enddatetime:" + enddatetime);

                int urlId = jsonObject.getInt("id");
                recordWindowLog("playSec:" + playSec);
                recordWindowLog("cron:" + cron);
                recordWindowLog("urlId:" + urlId);
                String urlIdStr1tttt = "@" + String.valueOf(urlId) + "@";
                String urlIdStr = ";" + urlIdStr1tttt;
                String urlIdStrEnd = "@" + String.valueOf(urlId) + "@";
                String jobName = CommUtils.get32MD5(urlIdStr1tttt + currentUrlTmp + "_" + cron.replaceAll(" ", "--"));
                String localCron = sp.getString("cron_url", "");
                int localCronBegin = localCron.indexOf(urlIdStr);
                if (localCronBegin != -1) {
                    int localCronEnd = localCron.indexOf(urlIdStrEnd, (localCronBegin + urlIdStr.length()));
                    if (localCronEnd != -1) {
                        String local1tmp, local2tmp;
                        if (localCronBegin == 0) {
                            local1tmp = "";
                        } else {
                            local1tmp = localCron.substring(0, localCronBegin - 1);
                        }
                        local2tmp = localCron.substring(localCronEnd + urlIdStrEnd.length());
                        String newCron = local1tmp + local2tmp + urlIdStr + "_" + startdatetime + "_" + enddatetime + "_" + currentUrlTmp + "_" + cron.replaceAll(" ", "--") + "_" + playSec + "_" + tmpVideoUrl + "_" + videoSize + urlIdStrEnd;
                        recordWindowLog("jobName:" + jobName);
                        recordWindowLog("localCron:" + localCron);
                        recordWindowLog("newCron:" + newCron);
                        recordWindowLog("local1tmp:" + local1tmp);
                        recordWindowLog("local2tmp:" + local2tmp);
                        sp.edit().putString("cron_url", newCron).apply();
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("url", currentUrlTmp);
                        params.put("len", String.valueOf(playSec));
                        params.put("vid", tmpVideoUrl);
                        params.put("size", videoSize);
                        params.put("urlId", String.valueOf(urlId));
                        params.put("urlType", String.valueOf(currentUrlTypeTmp));
                        params.put("startTime", startdatetime);
                        params.put("endTime", enddatetime);

                        QuartzManager.modifyJobTime(jobName, cron, params);
                    } else {
                        recordWindowLog("urlId:" + urlIdStr + " not found string end!!!");
                    }
                } else {
                    recordWindowLog("urlId:" + urlIdStr + " not found!!!");
                }
            } else if (currentUrlTypeTmp == 3) {//计划播放
                int playSec = jsonObject.getInt("playtimelength");
                String cron = jsonObject.getString("plancron");
                int urlId = jsonObject.getInt("id");
                recordWindowLog("playSec:" + playSec);
                recordWindowLog("cron:" + cron);
                recordWindowLog("urlId:" + urlId);
                String urlIdStr = ";@" + String.valueOf(urlId) + "@";
                String urlIdStrEnd = "@" + String.valueOf(urlId) + "@";
                String jobName = CommUtils.get32MD5(urlIdStr + currentUrlTmp + "_" + cron.replaceAll(" ", "--"));
                String localCron = sp.getString("cron_url", "");
                int localCronBegin = localCron.indexOf(urlIdStr);
                if (localCronBegin != -1) {
                    int localCronEnd = localCron.indexOf(urlIdStrEnd, (localCronBegin + urlIdStr.length()));
                    if (localCronEnd != -1) {
                        String local1tmp, local2tmp;
                        if (localCronBegin == 0) {
                            local1tmp = "";
                        } else {
                            local1tmp = localCron.substring(0, localCronBegin - 1);
                        }
                        local2tmp = localCron.substring(localCronEnd + urlIdStrEnd.length());
                        String newCron = local1tmp + local2tmp;//localCron + ";" + urlIdStr + currentUrlTmp + "_" + cron.replaceAll(" ", "--") + "_" + playSec + "_" + tmpVideoUrl + "_" + videoSize;
                        recordWindowLog("jobName:" + jobName);
                        recordWindowLog("localCron:" + localCron);
                        recordWindowLog("newCron:" + newCron);
                        recordWindowLog("local1tmp:" + local1tmp);
                        recordWindowLog("local2tmp:" + local2tmp);
                        sp.edit().putString("cron_url", newCron).apply();
                        QuartzManager.removeJob(jobName);// , "com.zr.webview.util.CronJob", cron , params);
                    } else {
                        recordWindowLog("urlId:" + urlIdStr + " not found string end!!!");
                    }
                } else {
                    recordWindowLog("urlId:" + urlIdStr + " not found!!!");
                }
            } else if (currentUrlTypeTmp == 4) {//立即播放
                int playSec = jsonObject.getInt("playtimelength");
                recordWindowLog("playSec:" + playSec);
                loadPlanUrl(currentUrlTmp, tmpVideoUrl, videoSize);
                PollingUtils.startPollingServiceNoRepeat(context, CommUtils.getRandomInt(), playSec, currentUrlTmp, tmpVideoUrl, videoSize);
            }
        }
    }

    /**
     * 收到netty服务端发送的数据
     */
    public void onEvent(DataActivityEvent event) {
        if (event != null) {
            String flag = event.getFlag();
            String text = event.getText();
            if (flag != null) {
                //recordWindowLog("evnet com:" + flag);
                if (flag.equals(CommUtils.Socket_Flag_Data)) {
                    parseCmdEvent(event, text);
                } else if (flag.equals(CommUtils.Socket_Flag_Inactive)) {
                    if (!connFlag) {
                        connFlagInactive = true;
                        Log.e(TAG, "socket inactive");
                        sendLogMsg("socket inactive ");
                        logger.error("socket inactive ");
                        reConnectServer();
                    }
                }

            }
        }
    }

    /**
     * 心跳线程
     */
    public void onEvent(HeartActivityEvent event) {
        if (event != null) {
            String flag = event.getFlag();
            if (flag != null) {
                String heartToSend = CommUtils.getHeartProtocolNew(appName, gj_no, currentStatus, getVolume(), currentUrl, currentUrlType, deviceSn, videoUrl, CommUtils.Server_RegistCode, CommUtils.AppVersion);
                if (heartbeatCount++ % 10 == 0) {
                    recordWindowLog("heart go to send:" + heartToSend);
                    try {
                        updatesystemlog();
                    } catch (Exception e) {
                        recordWindowLog(getLineNumber() + ":updatesystemlog Exception!!!");
                    }
                }
                if (timessyncstatus) {
                    if ((rebootmini >= 0) && (rebootmini <= 59)) {
                       long nowtimes = System.currentTimeMillis();
                        Calendar now = Calendar.getInstance();
                        int nowmin = now.get(Calendar.MINUTE);
                        int nowhour = now.get(Calendar.HOUR_OF_DAY);
                        //if((nowhour==17)&&(nowmin==rebootmini)&&(nowtimes-boottimes>60000)){  //防止重启完毕再次重启
                        if ((nowhour == 17) && (nowmin == 30) && (nowtimes - boottimes > 60000)) {  //防止重启完毕再次重启
                            recordWindowLog(getLineNumber() + ":go to everyday reboot....!!!");
                            try {
                                PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);  //重启到fastboot模式
                                pManager.reboot("");
                            } catch (Exception e) {
                                recordWindowLog(getLineNumber() + ":everyday reboot Exception!!!");
                            }
                        }
                    }
                } else {
                    Calendar now = Calendar.getInstance();
                    int nowyear = now.get(Calendar.YEAR);
                    if (nowyear > 2016) {
                        boottimes = System.currentTimeMillis();
                        timessyncstatus = true;
                        recordWindowLog(getLineNumber() + ":no 1970 years....!!!");
                    }
                }
                //updatesystemlog();
                try {
                    //if(videoView != null){
                    if (ifvideoplaytag) {
                        if (videoView != null) {
                            int videoviewplaybacktimeold = videoviewplaybacktime;
                            videoviewplaybacktime = videoView.getvideotime();
                            if (videoviewplaybacktime - videoviewplaybacktimeold < 40000) {
                                recordWindowLog(getLineNumber() + ":0:oldtime:" + videoviewplaybacktimeold + ":time:" + videoviewplaybacktime + "!!!");
                            }
                            if (videoviewplaybacktime == videoviewplaybacktimeold) {
                                recordWindowLog(getLineNumber() + ":1:oldtime:" + videoviewplaybacktimeold + ":time:" + videoviewplaybacktime + "!!!");
                                if (videoView != null) {
                                    videoView.playfromvideotime(videoviewplaybacktime);
                                }
                            }
                        }
                    }
                    if (!channel.isOpen()) reConnectServer();
                    CommUtils.sendNettyData(channel, heartToSend);
                } catch (Exception e) {
                    Log.e("Exception when JSONObject", e.toString());
                    recordWindowLog(getLineNumber() + ":sendNettyData Exception!!!");
                }
                if ((channel != null) && (!connFlagInactive)) {
                    sendInfoToServer();
                }
                File ffffff = new File(CommUtils.appworkpath + "heartt");
                if (!ffffff.exists()) {
                    ffffff.mkdir();
                }
                if (configupdate) {
                    configupdate = false;
                    planPlay();
                }
            }
        }
    }

    /**
     * 立即播放和计划播放时间完毕之后回调
     */
    public void onEvent(SecondActivityEvent event) {
        if (event != null) {
            String flag = event.getFlag();
            String text = event.getText();
            if (flag != null) {
                recordWindowLog("alarm id:" + flag + ",alarm url:" + text);
                PollingUtils.stopPollingServiceNoRepeat(context, Integer.parseInt(flag));
                if (Integer.parseInt(flag) == Integer.parseInt(currentPlan.getUrlId())) {
                    recordWindowLog("currentUrlType:" + currentUrlType);
                    recordWindowLog("waitingPlan3Tag:" + waitingPlan3Tag);
                    recordWindowLog("waitingPlan2Tag:" + waitingPlan2Tag);
                    try {
                        endPlaySomethingfunc();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (currentUrlType == 1) {
                        if (isOrderListPlay) {
                            //如果为垫片播的顺播逻辑，播放完成后逻辑
                            PlanModel planModel = listOrderSavePlanModel.get(orderListIndex);
                            currentUrl = planModel.getUrl();
                            videoUrl = planModel.getVideo();
                            currentUrlType = Integer.valueOf(planModel.getUrlType());
                            currentPlan.setUrlId(planModel.getUrlId());
                            currentPlan.setUrlType(planModel.getUrlType());
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(planModel.getBeginTime());
                            currentPlan.setEndTime(planModel.getEndTime());
                            currentPlan.setSize(planModel.getSize());
                            int lenInnn = planModel.getRemainlen();
//                            waitingPlan3Tag = 0;
                            loadPlanUrl(currentUrl, videoUrl, currentPlan.getSize());
                            if (orderListIndex < listOrderSavePlanModel.size() - 1)
                                orderListIndex++;
                            else
                                orderListIndex = 0;
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(planModel.getUrlId()), lenInnn, currentUrl, videoUrl, planModel.getSize());
                        }
                    } else if (currentUrlType == 4) {//如果当前播放等级为4，播放完成后的逻辑
                        sp.edit().putString("rn_cron_url", "").apply();
                        recordWindowLog("clean rn play!");
                        if (waitingPlan3Tag == 1) {
                            currentUrl = waitingPlan3.getUrl();
                            videoUrl = waitingPlan3.getVideo();
                            currentUrlType = Integer.valueOf(waitingPlan3.getUrlType());
                            currentPlan.setUrlId(waitingPlan3.getUrlId());
                            currentPlan.setUrlType(waitingPlan3.getUrlType());
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(waitingPlan3.getBeginTime());
                            currentPlan.setEndTime(waitingPlan3.getEndTime());
                            currentPlan.setSize(waitingPlan3.getSize());
                            int lenInnn = waitingPlan3.getRemainlen();
                            waitingPlan3Tag = 0;
                            loadPlanUrl(currentUrl, videoUrl, currentPlan.getSize());
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan3.getUrlId()), lenInnn, currentUrl, videoUrl, waitingPlan3.getSize());
                        } else if (waitingPlan2Tag == 1) {
                            currentUrl = waitingPlan2.getUrl();
                            videoUrl = waitingPlan2.getVideo();
                            currentUrlType = Integer.valueOf(waitingPlan2.getUrlType());
                            currentPlan.setUrlId(waitingPlan2.getUrlId());
                            currentPlan.setUrlType(waitingPlan2.getUrlType());
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(waitingPlan2.getBeginTime());
                            currentPlan.setEndTime(waitingPlan2.getEndTime());
                            currentPlan.setSize(waitingPlan2.getSize());
                            int lenInnn = waitingPlan2.getRemainlen();
                            waitingPlan2Tag = 0;
                            loadPlanUrl(currentUrl, videoUrl, currentPlan.getSize());
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan2.getUrlId()), lenInnn, currentUrl, videoUrl, waitingPlan2.getSize());
                        } else if (waitingPlan1Tag==1){
                            PlanModel planModel=listOrderSavePlanModel.get(orderListIndex);
                            currentUrl = planModel.getUrl();
                            videoUrl = planModel.getVideo();
                            currentUrlType = Integer.valueOf(planModel.getUrlType());
                            currentPlan.setUrlId(planModel.getUrlId());
                            currentPlan.setUrlType(planModel.getUrlType());
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(planModel.getBeginTime());
                            currentPlan.setEndTime(planModel.getEndTime());
                            currentPlan.setSize(planModel.getSize());
                            int lenInnn = planModel.getRemainlen();
//                            waitingPlan3Tag = 0;
                            loadPlanUrl(currentUrl, videoUrl, currentPlan.getSize());
                            if(orderListIndex<listOrderSavePlanModel.size()-1)
                                orderListIndex++;
                            else
                                orderListIndex=0;
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(planModel.getUrlId()), lenInnn, currentUrl, videoUrl, planModel.getSize());

                        } else {
                            recordWindowLog("sss1 url " + text + " play done");
                            //startLoad();

                            currentUrl = defaultUrl;
                            currentUrlType = 1;
                            currentPlan.setUrlId(defaultUrlId);
                            currentPlan.setUrlType(String.valueOf(1));
                            currentPlan.setSize(defaulVideoSize);
                            loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                        }
                    } else if (currentUrlType == 3) {//如果当前播放等级为3
                        sp.edit().putString("cb_cron_url", "").apply();
                        recordWindowLog("删除插播!");
                        recordWindowLog("waitingPlan2Tag:" + waitingPlan2Tag);
                        if (waitingPlan2Tag == 1) {
                            currentUrl = waitingPlan2.getUrl();
                            videoUrl = waitingPlan2.getVideo();
                            currentUrlType = Integer.valueOf(waitingPlan2.getUrlType());
                            currentPlan.setUrlId(waitingPlan2.getUrlId());
                            currentPlan.setUrlType(waitingPlan2.getUrlType());
                            currentPlan.setStartPlayTime(System.currentTimeMillis());
                            currentPlan.setBeginTime(waitingPlan2.getBeginTime());
                            currentPlan.setEndTime(waitingPlan2.getEndTime());
                            currentPlan.setSize(waitingPlan2.getSize());
                            int lenInnn = waitingPlan2.getRemainlen();
                            waitingPlan2Tag = 0;
                            loadPlanUrl(currentUrl, videoUrl, currentPlan.getSize());
                            PollingUtils.startPollingServiceNoRepeat(context, Integer.valueOf(waitingPlan2.getUrlId()), lenInnn, currentUrl, videoUrl, waitingPlan2.getSize());
                        } else {
                            recordWindowLog("sss url " + text + " play done");
                            //startLoad();

                            currentUrl = defaultUrl;
                            currentUrlType = 1;
                            currentPlan.setUrlId(defaultUrlId);
                            currentPlan.setUrlType(String.valueOf(1));
                            currentPlan.setSize(defaulVideoSize);

                            loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);
                        }
                    } else if (currentUrlType == 2) {//如果当前播放等级为2
                        recordWindowLog("sss0 url " + text + " play done");
                        //startLoad();

                        currentUrl = defaultUrl;
                        currentUrlType = 1;
                        currentPlan.setUrlId(defaultUrlId);
                        currentPlan.setUrlType(String.valueOf(currentUrlType));
                        currentPlan.setSize(defaulVideoSize);

                        loadPlanUrl(defaultUrl, defaulVideoUrl, defaulVideoSize);

                    }
                }
            }
        }
    }

    //装载删除播出单等业务
    private void loadplaylistdelplaylist() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String localCrons = sp.getString("id_buffer", "");
                    recordWindowLog("id_buffer:" + localCrons);
                    if (localCrons.length() > 0) {
                        String[] cronIdArr = localCrons.split(";");
                        for (String cronIdsig : cronIdArr) {
                            //if(s.length() == 0 || !s.contains("_")){
                            if (cronIdsig.length() == 0) {
                                continue;
                            }
                            String jsonText = sp.getString(cronIdsig, "");
                            recordWindowLog("cronidsig:" + cronIdsig);
                            if (jsonText.length() == 0) {
                                String cronIdsig1 = cronIdsig + ";";
                                localCrons.replace(cronIdsig1, "");
                                sp.edit().putString("id_buffer", localCrons).apply();
                                recordWindowLog("replace:" + cronIdsig1);
                                sp.edit().remove(cronIdsig).apply();
                                continue;
                            }
                            int currentUrlTypeTmp;
                            String currentUrlTmp;
                            JSONObject jsonObject = new JSONObject(jsonText);
                            String type = jsonObject.getString("type");
                            if (!type.equals("playlist")) {
                                recordWindowLog("is not playlist delete:" + cronIdsig);
                                String cronIdsig1 = cronIdsig + ";";
                                localCrons.replace(cronIdsig1, "");
                                sp.edit().putString("id_buffer", localCrons).apply();
                                sp.edit().remove(cronIdsig).apply();
                                continue;
                            }
                            currentUrlTypeTmp = jsonObject.getInt("plisttype");
                            currentUrlTmp = jsonObject.getString("url");
                            String tmpVideoUrl = "";
                            String videoSize = "";
                            String playlistOP = jsonObject.getString("op");
                            if (jsonObject.has("videourl")) {
                                tmpVideoUrl = jsonObject.getString("videourl");
                                int videoW = jsonObject.getInt("videow");
                                int videoH = jsonObject.getInt("videoh");
                                int videoX = jsonObject.getInt("videox");
                                int videoY = jsonObject.getInt("videoy");
                                videoSize = videoW + defaulVideoSplit + videoH + defaulVideoSplit + videoX + defaulVideoSplit + videoY;
                            }
                            int playSec = jsonObject.getInt("playtimelength");
                            String cron = jsonObject.getString("plancron");
                            String startdate0 = jsonObject.getString("startdate");
                            String enddate0 = jsonObject.getString("enddate");
                            String starttime = jsonObject.getString("starttime");
                            String endtime = jsonObject.getString("endtime");

                            String startdate = startdate0.replaceAll("-", ":");
                            String enddate = enddate0.replaceAll("-", ":");

                            String startdatetime = startdate + "-" + starttime;
                            String enddatetime = enddate + "-" + endtime;

                            recordWindowLog("before startdatetime:" + startdatetime);
                            recordWindowLog("before enddatetime:" + enddatetime);

                            long startdatetimewhen;
                            long enddatetimewhen;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                            startdatetimewhen = sdf.parse(startdatetime).getTime();
                            enddatetimewhen = sdf.parse(enddatetime).getTime();
                            long timnnn = System.currentTimeMillis();
                            if (timnnn > enddatetimewhen) { //删除无用策略
                                recordWindowLog("sp.edit().remove:" + cronIdsig);
                                sp.edit().remove(cronIdsig).apply();
                                String jsonTextttt = sp.getString(cronIdsig, "");
                                recordWindowLog("sp.getString:" + jsonTextttt);
                                continue;
                            }
                            timnnn += 28 * 3600 * 1000;
                            if (startdatetimewhen > timnnn) { //28小时外的策略不启动
                                continue;
                            }
                            if ((startdatetimewhen + playSec) > enddatetimewhen)    //结束时间超过时间段，则重新写播放时长
                            {
                                playSec = (int) (enddatetimewhen - startdatetimewhen);
                                recordWindowLog("receive re lenInnn:" + playSec);
                            }
                            startdatetime = String.valueOf(startdatetimewhen);
                            enddatetime = String.valueOf(enddatetimewhen);
                            recordWindowLog("after startdatetime:" + startdatetime);
                            recordWindowLog("after enddatetime:" + enddatetime);
                            int urlId = jsonObject.getInt("id");
                            recordWindowLog("playSec:" + playSec);
                            recordWindowLog("cron:" + cron);
                            recordWindowLog("urlId:" + urlId);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("url", currentUrlTmp);
                            params.put("len", String.valueOf(playSec));
                            params.put("vid", tmpVideoUrl);
                            params.put("size", videoSize);
                            params.put("urlId", String.valueOf(urlId));
                            params.put("urlType", String.valueOf(currentUrlTypeTmp));
                            params.put("startTime", startdatetime);
                            params.put("endTime", enddatetime);
                            String urlIdStr = "@" + String.valueOf(urlId) + "@";
                            String jobName = CommUtils.get32MD5(urlIdStr + currentUrlTmp + "_" + cron.replaceAll(" ", "--"));
                            String localCron = sp.getString("cron_url", "");
                            String newCron = localCron + ";" + urlIdStr + "_" + currentUrlTypeTmp + "_" + startdatetime + "_" + enddatetime + "_" + currentUrlTmp + "_" + cron.replaceAll(" ", "--") + "_" + playSec + "_" + tmpVideoUrl + "_" + videoSize + urlIdStr;
                            recordWindowLog("jobName:" + jobName);
                            QuartzManager.addJob(jobName, "com.zr.webview.util.CronJob", cron, params);
                        }
                    }
                    //装载控制计划
                    String localControlCrons = sp.getString("control_plan_id_buffer", "");
                    recordWindowLog("control_plan_id_buffer:" + localControlCrons);
                    if (localControlCrons.length() > 0) {
                        String[] ctrolcronIdArr = localControlCrons.split(";");
                        for (String ctrolcronIdsig : ctrolcronIdArr) {
                            if (ctrolcronIdsig.length() == 0) {
                                continue;
                            }
                            ctrolcronIdsig = "CPName:" + ctrolcronIdsig;
                            String jsonText = sp.getString(ctrolcronIdsig, "");
                            recordWindowLog("ctrolcronIdsig:" + ctrolcronIdsig);
                            if (jsonText.length() == 0) {
                                String cronIdsig1 = ctrolcronIdsig + ";";
                                localControlCrons.replace(cronIdsig1, "");
                                sp.edit().putString("control_plan_id_buffer", localControlCrons).apply();
                                recordWindowLog("replace:" + cronIdsig1);
                                sp.edit().remove(ctrolcronIdsig).apply();
                                continue;
                            }
                            int currentUrlTypeTmp;
                            String currentUrlTmp;
                            JSONObject jsonObject = new JSONObject(jsonText);
                            String type = jsonObject.getString("type");
                            if (!type.equals("controlplan")) {
                                recordWindowLog("is not controlplan delete:" + ctrolcronIdsig);
                                String cronIdsig1 = ctrolcronIdsig + ";";
                                localControlCrons.replace(cronIdsig1, "");
                                sp.edit().putString("control_plan_id_buffer", localControlCrons).apply();
                                sp.edit().remove(ctrolcronIdsig).apply();
                                continue;
                            }
                            int id = jsonObject.getInt("id");
                            //int id = 1234;
                            int cmd = jsonObject.getInt("cmd"); //1停止命令，2播放命令，3音量设置，4时间设置，5 gjno设置，6 app名称修改
                            String playlistOP = jsonObject.getString("op");

                            String value = jsonObject.getString("value");
                            recordWindowLog("id:" + id + ",cmd:" + cmd + ",value:" + value);
                            String cron = jsonObject.getString("plancron");

                            String startdate0 = jsonObject.getString("startdate");
                            //String enddate0 = jsonObject.getString("enddate");
                            String starttime = jsonObject.getString("starttime");
                            //String endtime = jsonObject.getString("endtime");

                            String startdate = startdate0.replaceAll("-", ":");
                            //String enddate = enddate0.replaceAll("-", ":");

                            String startdatetime = startdate + "-" + starttime;
                            String enddatetime;// = enddate+"-"+endtime;

                            recordWindowLog("before startdatetime:" + startdatetime);
                            //recordWindowLog("before enddatetime:" + enddatetime);

                            long startdatetimewhen;
                            long enddatetimewhen;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.getDefault());
                            startdatetimewhen = sdf.parse(startdatetime).getTime();
                            enddatetimewhen = startdatetimewhen + 24 * 3600 * 1000;
                            startdatetime = String.valueOf(startdatetimewhen);
                            enddatetime = String.valueOf(enddatetimewhen);

                            long timnnn = System.currentTimeMillis();
                            timnnn += 28 * 3600 * 1000;
                            if (startdatetimewhen > timnnn) { //28小时外的策略不启动
                                continue;
                            }
                            recordWindowLog("after startdatetime:" + startdatetime + ",after enddatetime:" + enddatetime);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("value", value);
                            params.put("id", String.valueOf(id));
                            params.put("cmd", String.valueOf(cmd));
                            params.put("startTime", startdatetime);
                            params.put("endTime", enddatetime);
                            String urlIdStr = "@" + String.valueOf(id) + "@";
                            String jobName = CommUtils.get32MD5(urlIdStr + String.valueOf(cmd));
                            recordWindowLog("jobName:" + jobName);
                            String IdBuffer = sp.getString("control_plan_id_buffer", "");
                            String NewIdBuffer = IdBuffer + ";" + String.valueOf(id);
                            String controlPlanName = "CPName:" + id;
                            //sp.edit().putString(controlPlanName, text).commit();
                            //sp.edit().putString("control_plan_id_buffer", NewIdBuffer).commit();
                            QuartzManager.addJob(jobName, "com.zr.webview.util.ControlCronJob", cron, params);
                        }
                    }
                } catch (Exception e) {
                    recordWindowLog("loadplaylistdelplaylist err!!");
                }
            }
        }.start();
    }

    //装载删除播出单等业务
    private void cleanplaylist() {
        try {
            String localCrons = sp.getString("id_buffer", "");
            recordWindowLog("id_buffer:" + localCrons);
            if (localCrons.length() > 0) {
                String[] cronIdArr = localCrons.split(";");
                for (String cronIdsig : cronIdArr) {
                    if (cronIdsig.length() == 0) {
                        continue;
                    }
                    sp.edit().remove(cronIdsig).apply();
                }
            }
            sp.edit().remove("id_buffer").apply();
            //装载控制计划
            String localControlCrons = sp.getString("control_plan_id_buffer", "");
            recordWindowLog("control_plan_id_buffer:" + localControlCrons);
            if (localControlCrons.length() > 0) {
                String[] ctrolcronIdArr = localControlCrons.split(";");
                for (String ctrolcronIdsig : ctrolcronIdArr) {
                    if (ctrolcronIdsig.length() == 0) {
                        continue;
                    }
                    ctrolcronIdsig = "CPName:" + ctrolcronIdsig;
                    sp.edit().remove(ctrolcronIdsig).apply();
                }
            }
            sp.edit().remove("control_plan_id_buffer").apply();
        } catch (Exception e) {
            recordWindowLog("loadplaylistdelplaylist err!!");
        }
    }

    public boolean pingConnectTest(String ip) {
        String result = null;
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping1次
            recordWindowLog(getLineNumber() + ":ip: " + ip);
            // 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuffer = new StringBuilder();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            //Log.i("TTT", "result content : " + stringBuffer.toString());
            recordWindowLog(getLineNumber() + ":result content : " + stringBuffer.toString());
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                //recordWindowLog(getLineNumber()+":result = " + result);
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
                //recordWindowLog(getLineNumber()+":result = " + result);
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            //Log.i("TTT", "result = " + result);
            recordWindowLog(getLineNumber() + ":result = " + result + ":ip: " + ip);
        }
        return false;
    }

    //netty方式连接服务端
    private void connectServer() {
        new Thread() {
            @Override
            public void run() {
                if (CommUtils.isNetworkAvailable(context)) {
                    //recordWindowLog("network available !!");
                    try {
                        if (!bootstrapinit) {
                            group = new NioEventLoopGroup();
                            bootstrap = new Bootstrap();
                            // 指定channel类型
                            bootstrap.channel(NioSocketChannel.class);
                            // 指定Handler
                            bootstrap.handler(new MyClientInitializer(context));
                            // 指定EventLoopGroup
                            bootstrap.group(group);
                            bootstrapinit = true;
                            recordWindowLog("bootstrapinit !!");
                        }
//						group = new NioEventLoopGroup();
//						Bootstrap bootstrap = new Bootstrap();
//						// 指定channel类型
//						bootstrap.channel(NioSocketChannel.class);
//						// 指定Handler
//						bootstrap.handler(new MyClientInitializer(context));
//						// 指定EventLoopGroup
//						bootstrap.group(group);
                        //ChannelFuture channelFuture   = bootstrap.connect(new InetSocketAddress(CommUtils.Server_Url,
                        //		CommUtils.Server_Port)).sync();
                        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(CommUtils.Server_Url, 15525)).sync();
                        if (channelFuture.isSuccess()) {
                            connectServerCount = 0;
                            channel = channelFuture.channel();
                            socketConNum = 0;
                            connFlag = false;
                            connFlagInactive = false;
                            //QuartzManager.shutdownJobs();
                            //g_sync_time_once=0;
                            String registTosend = CommUtils.getRegistToServer(appName, gj_no, currentStatus, getVolume(), currentUrl, currentUrlType, deviceSn, videoUrl, CommUtils.Server_RegistCode, CommUtils.AppVersion);
                            recordWindowLog("registTosend:" + registTosend);//server ["+CommUtils.Server_Url+"]port["+CommUtils.Server_Port+"] connected");
                            CommUtils.sendNettyData(channel, registTosend);
                            if (currentUrl.equals(defaultAdUrl)) {
                                planPlay();
                            }
                            pingConnectTest("www.sohu.com");
                            pingConnectTest("120.24.238.78");
                            pingConnectTest("120.76.135.227");
                            pingConnectTest("118.190.44.117");
                            pingConnectTest("124.207.66.52");
                            pingConnectTest("www.guangjiaoyun.com");
                            pingConnectTest("www.baidu.com");
                        } else {
                            if (connectServerCount++ % 10 == 0) {
                                recordWindowLog("server[" + CommUtils.Server_Url + "]port[" + CommUtils.Server_Port + "] can't isSuccess", true);
                                pingConnectTest("www.sohu.com");
                                pingConnectTest("120.24.238.78");
                                pingConnectTest("120.76.135.227");
                                pingConnectTest("118.190.44.117");
                                pingConnectTest("124.207.66.52");
                                pingConnectTest("www.guangjiaoyun.com");
                                pingConnectTest("www.baidu.com");
                            }
                            update_tip.sendEmptyMessage(500);
                        }
                    } catch (Exception e) {
                        if (connectServerCount++ % 10 == 0) {
                            recordWindowLog("server[" + CommUtils.Server_Url + "]port[" + CommUtils.Server_Port + "] can't connect", true);
                            pingConnectTest("www.sohu.com");
                            pingConnectTest("120.24.238.78");
                            pingConnectTest("120.76.135.227");
                            pingConnectTest("118.190.44.117");
                            pingConnectTest("124.207.66.52");
                            pingConnectTest("www.guangjiaoyun.com");
                            pingConnectTest("www.baidu.com");
                        }
                        update_tip.sendEmptyMessage(500);
                    }
                } else {
                    if (connectServerCount++ % 10 == 0) {
                        recordWindowLog("network unavailable !!" + defaultUrl, true);
                        recordWindowLog("server[" + CommUtils.Server_Url + "]port[" + CommUtils.Server_Port + "] network err reconect", true);
                        pingConnectTest("www.sohu.com");
                        pingConnectTest("120.24.238.78");
                        pingConnectTest("120.76.135.227");
                        pingConnectTest("118.190.44.117");
                        pingConnectTest("124.207.66.52");
                        pingConnectTest("www.guangjiaoyun.com");
                        pingConnectTest("www.baidu.com");
                    }
                    update_tip.sendEmptyMessage(500);
                }
            }
        }.start();
    }

    private void downloadtest() {
        new Thread() {
            @Override
            public void run() {
                try {
                    downloadJob downloader = new downloadJob();
                    downloader.downFile("http://www.fengfly.com/plus/view-215104-1.html", "/new", "/view-215104-1.html");
                    //recordWindowLog("server ["+CommUtils.Ser
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void downloadUpdateXml() {
        new Thread() {
            @Override
            public void run() {
                try {
                    downloadJob downloader = new downloadJob();
                    downloader.downFile("http://www.fengfly.com/plus/view-215104-1.html", "/update", "/update.xml");
                    //recordWindowLog("server ["+CommUtils.Ser
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (myView == null) {
            super.onBackPressed();
        } else {
            chromeClient.onHideCustomView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityOnPause = false;
        if (map_view != null) {
            map_view.onResume();
            map_view.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityOnPause = true;
        if (videoView != null) {
            recordWindowLog(getLineNumber() + ":old onPause 1!!!");
            //videoView.pause();
        }
        if (map_view != null) {
            map_view.onPause();
            map_view.pauseTimers();
            ((AudioManager) getSystemService(
                    Context.AUDIO_SERVICE)).requestAudioFocus(
                    new OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                        }
                    }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    private void destroyAll() {
        recordWindowLog("begin to exit");
        QuartzManager.shutdownJobs();
//		if(group!=null){
//			group.shutdownGracefully();
//		}
        if (videoView != null) {
            videoView.stop();
        }
        if (handlerDown != null) {
            handlerDown.stop();
        }
        if (debugOverlay != null) {
            debugOverlay.destroyView();
        }
        EventBus.getDefault().unregister(this);
        CrashApplication.getInstance().finishActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private class MyWebviewCient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            recordWindowLog("111page load start  " + url);
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            //startLoad();
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse response = null;
            response = super.shouldInterceptRequest(view, url);

            if (url != null && url.contains("**injection**/")) {

                //String assertPath = url.replace("**injection**/", "");
                String assertPath = url.substring(url.indexOf("**injection**/") + "**injection**/".length(), url.length());
                try {

                    response = new WebResourceResponse("application/x-font-ttf", "UTF8", getAssets().open(assertPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);//在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();  //接受所有证书
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //view.loadUrl("javascript:!function(){" + "s=document.createElement('style');s.innerHTML=" + "\"@font-face{font-family:myhyqh;src:url('**injection**/hyqh.ttf');}*{font-family:myhyqh !important;}\";" + "document.getElementsByTagName('head')[0].appendChild(s);" + "document.getElementsByTagName('body')[0].style.fontFamily = \"myhyqh\";}()");
            super.onPageFinished(view, url);
            view.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video');for(var i=0;i<videos.length;i++){videos[i].style.display=\"none\";videos[i].stop();}})()");
            //view.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video');for(var i=0;i<videos.length;i++){videos[i].play();}})()");
            //view.loadUrl("javascript:!function(){" + "s=document.createElement('style');s.innerHTML=" + "\"@font-face{font-family:myhyqh;src:url('**injection**/hyqh.ttf');}*{font-family:myhyqh !important;}\";" + "document.getElementsByTagName('head')[0].appendChild(s);" + "document.getElementsByTagName('body')[0].style.fontFamily = \"myhyqh\";}()");
            recordWindowLog("111page load finished " + url);
            endLoad();
            if (videoFlag && videoUrl.length() > 0) {
                update_tip.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        videoView.setVisibility(View.VISIBLE);
                        videoView.start();
                    }
                }, 2000);

            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            String printeurl;
            printeurl = "errcode:" + Integer.toString(errorCode) + ",des:" + description + ",furl:" + failingUrl;
            recordWindowLog("111page load onReceivedError " + printeurl);
            int tmpindex = failingUrl.lastIndexOf("?t=");
            String realUrl;
            if (tmpindex != -1) {
                int tmpindex1111 = failingUrl.lastIndexOf("/?t=");
                if (tmpindex1111 != -1) {
                    realUrl = failingUrl.substring(0, tmpindex1111);
                    recordWindowLog("realUrl000:" + realUrl);
                } else {
                    realUrl = failingUrl.substring(0, tmpindex);
                    recordWindowLog("realUrl:" + realUrl);
                }
            } else {
                realUrl = failingUrl;
            }
            int id1 = realUrl.indexOf("http://");
            String urlshorter1111;
            if (id1 == -1) {
                urlshorter1111 = realUrl.substring(0, realUrl.length());
            } else {
                urlshorter1111 = realUrl.substring(7, realUrl.length());
            }
            pingConnectTest(urlshorter1111);
            boolean c = defaultUrl.startsWith("~@W@~");
            String urlnew;
            if (c)//整理默认地址，去掉前缀
            {
                urlnew = defaultUrl.replace("~@W@~", "");
                urlnew.trim();
            } else {
                urlnew = defaultUrl.trim();
            }
            recordWindowLog("urlnew:" + urlnew);
            String url;
            if (realUrl.intern().equals(urlnew.intern()))//默认地址出错，播apk中的地址
            {
                url = defaultAdUrl;//"file:///android_asset/ad.html";
                //map_view.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
            } else {
                url = urlnew;
                if (c) {
                    //map_view.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
                } else {
                    //map_view.getSettings().setUserAgentString(USER_AGENT_STRINGdef);
                }
            }
            recordWindowLog("url:" + url);
            map_view.loadUrl(getRefreshUrl(url));
            currentUrl = url;
        }

    }

    private class MyChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (myView != null) {
                callback.onCustomViewHidden();
                return;
            }
            myView = view;
            myCallBack = callback;
        }

        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded,
                                             long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            quotaUpdater.updateQuota(spaceNeeded * 10);
        }

        @Override
        public void onHideCustomView() {
            if (myView == null) {
                return;
            }
            myView = null;
            myCallBack.onCustomViewHidden();
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ProgressBar progressbar = map_view.getProgressbar();
            ImageView imageView = map_view.getImageView();
            LoadingView loadingView=map_view.getLoadingView();
            if (newProgress == 100) {
                imageView.setVisibility(View.GONE);
                progressbar.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
            } else {
                if (progressbar.getVisibility() == View.GONE)
                    progressbar.setVisibility(View.VISIBLE);
                progressbar.setProgress(newProgress);
                imageView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.VISIBLE);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    /**
     * 显示进度界面
     */
    private void startLoad() {
        if (!progress_dialog.isShowing()) {
            progress_dialog.show();
        }
    }

    /**
     * 关闭进度界面
     */
    private void endLoad() {
        if (progress_dialog.isShowing()) {
            progress_dialog.cancel();
            progress_dialog.dismiss();
        }
    }

    /*
     * 获取当前程序的版本号
	 */
    private String getVersionName() throws Exception {
        //获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;
    }

    /*
     * 获取当前程序的数字版本号
	 */
    private int getVersionCode() throws Exception {
        //获取getVersionCode的实例
        PackageManager packageManager = getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionCode;
    }

    /*
     * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
	 */
    public UpdataInfo getUpdataInfo(InputStream is) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        recordWindowLog(getLineNumber() + ":");
        parser.setInput(is, "utf-8");//设置解析的数据源
        recordWindowLog(getLineNumber() + ":");
        int type = parser.getEventType();
        recordWindowLog(getLineNumber() + ":");
        UpdataInfo info = new UpdataInfo();//实体
        recordWindowLog(getLineNumber() + ":");
        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                case XmlPullParser.START_TAG:
                    if ("version".equals(parser.getName())) {
                        info.setVersion(parser.nextText()); //获取版本号
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText()); //获取要升级的APK文件
                        //}else if ("description".equals(parser.getName())){
                        //    info.setDescription(parser.nextText()); //获取该文件的信息
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }

    /*
     * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
	 */
    public UpdataInfo getUpdataInfoAll(InputStream is) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        //recordWindowLog(getLineNumber()+":");
        parser.setInput(is, "utf-8");//设置解析的数据源
        //recordWindowLog(getLineNumber()+":");
        int type = parser.getEventType();
        //recordWindowLog(getLineNumber()+":");
        UpdataInfo info = new UpdataInfo();//实体
        recordWindowLog(getLineNumber() + ":");
        while (type != XmlPullParser.END_DOCUMENT) {
            switch (type) {
                case XmlPullParser.START_TAG:
                    if ("version".equals(parser.getName())) {
                        info.setVersion(parser.nextText()); //获取版本号
                        recordWindowLog(getLineNumber() + ":" + info.getVersion());
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText()); //获取要升级的APK文件
                        recordWindowLog(getLineNumber() + ":" + info.getUrl());
                        //}else if ("description".equals(parser.getName())){
                        //    info.setDescription(parser.nextText()); //获取该文件的信息
                        //    recordWindowLog(getLineNumber()+":"+info.getDescription());
                    } else if ("serverurl".equals(parser.getName())) {
                        info.setServerUrl(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getServerUrl());
                    } else if ("serverregcode".equals(parser.getName())) {
                        info.setServerRegcode(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getServerRegcode());
                    } else if ("updatexml".equals(parser.getName())) {
                        info.setUpdatexml(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getUpdatexml());
                    } else if ("playlisthtmladdrhead".equals(parser.getName())) {
                        info.setPlaylisthtmladdrhead(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getPlaylisthtmladdrhead());
                    } else if ("playlisthtmladdrheadTag".equals(parser.getName())) {
                        info.setPlaylisthtmladdrheadTag(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getPlaylisthtmladdrheadTag());
                    } else if ("playlisthtmladdr".equals(parser.getName())) {
                        info.setPlaylisthtmladdr(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getPlaylisthtmladdr());
                    } else if ("playlisthtmladdrEnd".equals(parser.getName())) {
                        info.setPlaylisthtmladdrEnd(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getPlaylisthtmladdrEnd());
                    } else if ("playlisthtmlvideoftpaddr".equals(parser.getName())) {
                        info.setPlaylisthtmlvideoftpaddr(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getPlaylisthtmlvideoftpaddr());
                    } else if ("ftpdisksmallsize".equals(parser.getName())) {
                        info.setftpdisksmallsize(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getftpdisksmallsize());
                    } else if ("ftpdiskcleansize".equals(parser.getName())) {
                        info.setftpdiskcleansize(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getftpdiskcleansize());
                    } else if ("ftpusr".equals(parser.getName())) {
                        info.setftpusr(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getftpusr());
                    } else if ("ftppasswd".equals(parser.getName())) {
                        info.setftppasswd(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getftppasswd());
                    } else if ("appworkpath".equals(parser.getName())) {
                        info.setappworkpath(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getappworkpath());
                    } else if ("syslogserverdir".equals(parser.getName())) {
                        info.setsyslogserverdir(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getsyslogserverdir());
                    } else if ("playerName".equals(parser.getName())) {
                        info.setplayerName(parser.nextText()); //获取该文件的信息
                        recordWindowLog(getLineNumber() + ":" + info.getplayerName());
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }

    public static File getFileFromServer(String path, ProgressDialog pd) throws Exception {
        //如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            //获取到文件的大小
            pd.setMax(conn.getContentLength());
            InputStream is = conn.getInputStream();
            //File file = new File(Environment.getExternalStorageDirectory(), "updata.apk");CommUtils.appworkpath
            File file = new File(CommUtils.appworkpath, "updata.apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                //获取当前下载量
                pd.setProgress(total);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }

    /*
     * 从服务器获取xml解析并进行比对版本号
	 */
    public class CheckVersionTask implements Runnable {

        public void run() {
            try {
                //从资源文件获取服务器 地址
                String path = "http://o2o.99we.cn/1.update.xml";
                //包装成url的对象
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                InputStream is = conn.getInputStream();
                //info =  UpdataInfoParser.getUpdataInfo(is);
                updataxmlinfo = getUpdataInfo(is);
//	            String xmldata;
//	            downloadJob downloader = new downloadJob();
//	            xmldata=downloader.download(path);
                String versionname = getVersionName();
                if (updataxmlinfo.getVersion().equals(versionname)) {
                    Log.i(TAG, "版本号相同无需升级");
                    LoginMain();
                } else {
                    Log.i(TAG, "版本号不同 ,提示用户升级 ");
                    Message msg = new Message();
                    msg.what = 801;//UPDATA_CLIENT;
                    //handler.sendMessage(msg);
                    update_tip.sendMessage(msg);
                }
            } catch (Exception e) {
                // 待处理
                Message msg = new Message();
                msg.what = 802;//GET_UNDATAINFO_ERROR;
                //handler.sendMessage(msg);
                update_tip.sendMessage(msg);
                e.printStackTrace();
            }
        }
    }

    /**
     * 弹出对话框通知用户更新程序
     * <p>
     * 弹出对话框的步骤：
     * 1.创建alertDialog的builder.
     * 2.要给builder设置属性, 对话框的内容,样式,按钮
     * 3.通过builder 创建一个对话框
     * 4.对话框show()出来
     */
    protected void showUpdataDialog() {
        logger.debug("showUpdataDialog in!!");
        AlertDialog.Builder builer = new Builder(this);
        builer.setTitle("版本升级");
        builer.setMessage(updataxmlinfo.getDescription());
        logger.debug("showUpdataDialog getDescription:" + updataxmlinfo.getDescription());
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "下载apk,更新");
                downLoadApk();
            }
        });
        //当点取消按钮时进行登录
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LoginMain();
            }
        });
        logger.debug("showUpdataDialog before builer create!!");
        AlertDialog dialog = builer.create();
        dialog.show();
        logger.debug("showUpdataDialog after builer create!!");
    }

    private void downFileManage(final String url, final ProgressDialog m_progressDlg) {
        m_progressDlg.show();
        new Thread() {
            public void run() {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url);
                HttpResponse response;
                try {
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();

                    m_progressDlg.setMax((int) length);//设置进度条的最大值

                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                //Environment.getExternalStorageDirectory(),
                                CommUtils.appworkpath,
                                updateapkname);
                        // m_appNameStr);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int count = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            count += ch;
                            if (length > 0) {
                                m_progressDlg.setProgress(count);
                            }
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    //down();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void httpuploadhtml() {
        recordWindowLog("httpuploadhtml in !!");
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet("http://h50.99view.com/Application/Admin/View/webRoot/web4m2ggvlywm7/index.html");
                    recordWindowLog(getLineNumber() + ":" + "begin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
                    HttpResponse response;
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();
                    recordWindowLog(getLineNumber() + ":" + "begin!!!!!");
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                "index.html");
                        // m_appNameStr);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int count = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            count += ch;
                            if (length > 0) {
                                Log.i("","length > 0");
                            }
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    //pd.dismiss(); //结束掉进度条对话框
                } catch (Exception e) {


                    recordWindowLog(getLineNumber() + " !!!!:" + e.getMessage());
                }
            }
        }.start();
    }

    /*
     * 从服务器中下载APK
	 */
    protected void downLoadApk() {
        //final ProgressDialog pd;    //进度条对话框  updatePB
        //updatePB.setMessage("正在下载更新");

        //logger.debug("downLoadApk in !!");
        recordWindowLog("downLoadApk in !!");
        new Thread() {
            @Override
            public void run() {
                try {
                    //File file = DownLoadManager.getFileFromServer(updataxmlinfo.getUrl(), pd);
                    //File file = downFileManage(updataxmlinfo.getUrl(), pd);
                    // File file = new File(
                    //         Environment.getExternalStorageDirectory(),
                    //         updateapkname);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(updataxmlinfo.getUrl());
                    //logger.debug("updataxmlinfo.url:"+updataxmlinfo.getUrl());
                    recordWindowLog("updataxmlinfo.url:" + updataxmlinfo.getUrl());
                    HttpResponse response;
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();
                    Message msg = new Message();
                    String lenS = "" + length;
                    msg.what = 805;//
                    msg.obj = lenS;
                    update_tip.sendMessage(msg);
//	                MainActivity.this.updatePB.setVisibility(View.VISIBLE);
//	                MainActivity.this.updatePB.setMax((int)length);//设置进度条的最大值
//	                MainActivity.this.updatePB.setProgress(0);

                    //logger.debug("正在下载更新!!");
                    recordWindowLog("正在下载更新!!");
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                updateapkname);
                        // m_appNameStr);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int count = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            count += ch;
                            if (length > 0) {
                                //MainActivity.this.updatePB.setProgress(count);
                                String lenS1 = "" + count;
                                Message msg1 = new Message();
                                msg1.what = 806;//
                                msg1.obj = lenS1;//
                                update_tip.sendMessage(msg1);
                                //logger.debug("count:"+count);
                                //sleep(10);
                            }
                        }
                        //logger.debug("update finish!!");
                        recordWindowLog("下载完毕!!");
                        //MainActivity.this.updatePB.setVisibility(View.GONE);
                        Message msge = new Message();
                        msge.what = 807;// /
                        update_tip.sendMessage(msge);
                        sleep(1000);
                        installApk(file);
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    //pd.dismiss(); //结束掉进度条对话框
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = 803;//DOWN_ERROR;
                    update_tip.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /*
     * 从服务器中下载APK
	 */
    protected void downLoadApkwithoutProcessDialog() {
        logger.debug("正在下载更新!!");
        new Thread() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(updataxmlinfo.getUrl());
                    logger.debug("updataxmlinfo.url:" + updataxmlinfo.getUrl());
                    HttpResponse response;
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                updateapkname);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int count = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            count += ch;
                            if (length > 0) {
                                Log.i(TAG,"length > 0");
                            }
                        }
                        logger.debug("下载完毕，开始安装!!");
                        sleep(1000);
                        installApk(file);
                        logger.debug("安装完毕!!");
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    LoginMain();
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = 803;//DOWN_ERROR;
                    update_tip.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //安装apk
    protected void installApk(File file) {
        //logger.debug("installApk in!!");
        recordWindowLog("开始安装!!");
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
        recordWindowLog("安装完毕!!");
        new Thread() {
            public void run() {
                update_tip.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recordWindowLog("开始恢复!!");
                        LoginMain();
                        recordWindowLog("恢复完毕!!");
                    }
                }, 5 * 1000);
            }
        }.start();
    }

    /*
     * 进入程序的主界面
	 */
    private void LoginMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //结束掉当前的activity
        this.finish();
    }

    private void checkifautoupdate() {

        new Thread() {
            public void run() {
                try {
                    String updateftpserver, updateftppath;
                    String allsssss = CommUtils.updatexml + CommUtils.updatexmlrightnowtag;
                    int sid = allsssss.trim().indexOf("ftp://");
                    if (sid == -1) {
                        int end = allsssss.trim().indexOf("/");    //"119.255.38.236/media/updatefile/99view1x__update.tar"
                        updateftpserver = allsssss.trim().substring(0, end);
                        updateftppath = allsssss.trim().substring(end, allsssss.trim().length());
                    } else {
                        int end = allsssss.trim().indexOf("/", (sid + 6));    //"ftp://119.255.38.236/media/updatefile/99view1x__update.tar"
                        updateftpserver = allsssss.trim().substring((sid + 6), end);
                        updateftppath = allsssss.trim().substring(end, allsssss.trim().length());
                    }
                    recordWindowLog(getLineNumber() + ":updateftpserver:" + updateftpserver + ":updateftppath:" + updateftppath);
                    FTP fc = new FTP(updateftpserver);
                    //FTP fc=new FTP(updateftpserver,"admin","admin");
                    FTPClient ftpclint = fc.getftpClientHandel();
                    // 打开FTP服务
                    try {
                        fc.openConnect();
                        ///listener.onDownLoadProgress(MainActivity.FTP_CONNECT_SUCCESSS, 0, null);
                        //recordWindowLog("FTP_CONNECT_SUCCESSS!!");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        //listener.onDownLoadProgress(MainActivity.FTP_CONNECT_FAIL, 0, null);
                        return;
                    }

                    // 先判断服务器文件是否存在
                    FTPFile[] files = ftpclint.listFiles(updateftppath);
                    if (files.length == 0) {
                        //listener.onDownLoadProgress(MainActivity.FTP_FILE_NOTEXISTS, 0, null);
                        return;
                    }

                    // 接着判断下载的文件是否能断点下载
                    long serverSize = files[0].getSize(); // 获取远程文件的长度
                    //logger.debug("serverSize:"+serverSize);
                    recordWindowLog(getLineNumber() + ":serverFileSize:" + serverSize);
                    if (serverSize == 1) {
                        long playerut = sp.getLong("playerUpdateTime", 0);
                        long templ = System.currentTimeMillis();
                        recordWindowLog(getLineNumber() + ":playerut:" + playerut);
                        recordWindowLog(getLineNumber() + ":templ:" + templ);
                        if ((playerut != 0) && ((playerut + 60000) < templ)) {
                            recordWindowLog(getLineNumber() + ":autoupdating!!!");
                            Message msg = Message.obtain();
                            msg.what = 804;
                            update_tip.sendMessage(msg);
                        }
                    } else if (serverSize > 0) {

                        File configfile1 = new File(CommUtils.appworkpath + updateapkversionname);
                        configfile1.delete();
                        recordWindowLog(getLineNumber() + ":!!!");
                        String updateftpserver1, updateftppath1;
                        String allsssss1 = CommUtils.updatexml + CommUtils.updatexmlrightnowtag;
                        int sid1 = allsssss.trim().indexOf("ftp://");
                        if (sid1 == -1) {
                            int end1 = allsssss1.trim().indexOf("/");    //"119.255.38.236/media/updatefile/99view1x__update.tar"
                            updateftpserver1 = allsssss1.trim().substring(0, end1);
                            updateftppath1 = allsssss1.trim().substring(end1, allsssss1.trim().length());
                        } else {
                            int end1 = allsssss1.trim().indexOf("/", (sid + 6));    //"ftp://119.255.38.236/media/updatefile/99view1x__update.tar"
                            updateftpserver1 = allsssss1.trim().substring((sid1 + 6), end1);
                            updateftppath1 = allsssss1.trim().substring(end1, allsssss1.trim().length());
                        }
                        recordWindowLog(getLineNumber() + ":updateftppath:" + updateftppath1);
                        recordWindowLog(getLineNumber() + ":updateftppath:" + CommUtils.appworkpath);
                        recordWindowLog(getLineNumber() + ":updateftppath:" + updateapkversionname);
                        FTP fc11111 = new FTP(updateftpserver1);
                        //int rt=0;
                        //fc11111.downloadSingleFile1(updateftppath1,CommUtils.appworkpath,updateapkversionname);
                        int rt = fc11111.downloadSingleFile2(updateftppath1, CommUtils.appworkpath, updateapkversionname);
                        recordWindowLog(getLineNumber() + ":rt:" + rt);
                        if (rt >= 0) {
                            //recordWindowLog(getLineNumber()+":"+CommUtils.hackconfigfileserver);
                            File configfile = new File(CommUtils.appworkpath + updateapkversionname);
                            //recordWindowLog(getLineNumber()+":"+CommUtils.appworkpath+CommUtils.hackconfigfilename);
                            String res = "";
                            FileInputStream fin = new FileInputStream(configfile);
                            int aaasss = Integer.valueOf(String.valueOf(serverSize));
                            byte[] buffer = new byte[aaasss];
                            fin.read(buffer);
                            res = new String(buffer);
                            recordWindowLog(getLineNumber() + ":res:" + res);
                            fin.close();
                            if (!res.contains("test")) {
                                if (!res.equals(CommUtils.AppVersion)) {
                                    recordWindowLog(getLineNumber() + ":autoupdating!!!000");
                                    Message msg = Message.obtain();
                                    msg.what = 804;
                                    update_tip.sendMessage(msg);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":checkifautoupdate  errr!!:" + e.getMessage());
                }
            }
        }.start();

    }

    private void beginFtpUpdata111() {
        new Thread() {
            public void run() {
                try {
                    File configfile1 = new File(CommUtils.appworkpath + updateapkversionname);
                    configfile1.delete();
                    //hack server read
                    String updateftpserver, updateftppath;
                    String allsssss = CommUtils.updatexml;//+CommUtils.updatexmlrightnowtag;
                    int sid = allsssss.trim().indexOf("ftp://");
                    if (sid == -1) {
                        int end = allsssss.trim().indexOf("/");
                        updateftpserver = allsssss.trim().substring(0, end);
                        updateftppath = allsssss.trim().substring(end, allsssss.trim().length());
                    } else {
                        int end = allsssss.trim().indexOf("/", (sid + 6));    //"ftp://119.255.38.236/media/updatefile/99view1x__update.tar"
                        updateftpserver = allsssss.trim().substring((sid + 6), end);
                        updateftppath = allsssss.trim().substring(end, allsssss.trim().length());
                    }
                    recordWindowLog(getLineNumber() + ":updateftpserver:" + updateftpserver + ":updateftppath:" + updateftppath);
                    FTP fc = new FTP(updateftpserver);
                    recordWindowLog(getLineNumber() + "::");
                    FTPClient ftpclint = fc.getftpClientHandel();
                    recordWindowLog(getLineNumber() + "::");
                    // 打开FTP服务
                    try {
                        fc.openConnect();
                        ///listener.onDownLoadProgress(MainActivity.FTP_CONNECT_SUCCESSS, 0, null);
                        //recordWindowLog("FTP_CONNECT_SUCCESSS!!");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        //listener.onDownLoadProgress(MainActivity.FTP_CONNECT_FAIL, 0, null);
                        return;
                    }

                    // 先判断服务器文件是否存在
                    FTPFile[] files = ftpclint.listFiles(updateftppath);
                    if (files.length == 0) {
                        //listener.onDownLoadProgress(MainActivity.FTP_FILE_NOTEXISTS, 0, null);
                        return;
                    }

                    //创建本地文件夹
                    File mkFile = new File(CommUtils.appworkpath);
                    if (!mkFile.exists()) {
                        mkFile.mkdirs();
                    }

                    String localPath = CommUtils.appworkpath + updateapkversionname;
                    // 接着判断下载的文件是否能断点下载
                    long serverSize = files[0].getSize(); // 获取远程文件的长度
                    logger.debug("serverSize:" + serverSize);
                    File localFile = new File(localPath);
                    long localSize = 0;
                    if (localFile.exists()) {
                        localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
                        if (localSize > serverSize) {
                            File file = new File(localPath);
                            file.delete();
                            localSize = 0;
                        } else if (localSize == serverSize) {
                            return;
                        }
                        logger.debug("localSize:" + localSize);
                    }
                    Message msg = new Message();
                    String lenS = "" + serverSize;
                    msg.what = 805;//
                    msg.obj = lenS;
                    update_tip.sendMessage(msg);
                    recordWindowLog(getLineNumber() + ":开始下载!!!");
                    // 进度
                    long step = serverSize / 100;
                    long process = 0;
                    long currentSize = 0;
                    // 开始准备下载文件
                    OutputStream out = new FileOutputStream(localFile, true);
                    recordWindowLog(getLineNumber() + "::");
                    ftpclint.setRestartOffset(localSize);
                    InputStream input = ftpclint.retrieveFileStream(updateftppath);
                    byte[] b = new byte[1024];
                    int length = 0;
                    recordWindowLog(getLineNumber() + "::");
                    while ((length = input.read(b)) != -1) {
                        recordWindowLog(getLineNumber() + ":length:" + length);
                        out.write(b, 0, length);
                        currentSize = currentSize + length;
//						recordWindowLog(getLineNumber()+":currentSize:"+currentSize);
//						recordWindowLog(getLineNumber()+":step:"+step);
//						recordWindowLog(getLineNumber()+":process:"+process);
                        if (step == 0) {
                            String lenS1 = "" + currentSize;
                            Message msg1 = new Message();
                            msg1.what = 806;//
                            msg1.obj = lenS1;//
                            update_tip.sendMessage(msg1);
                        } else {
                            if (currentSize / step != process) {
                                process = currentSize / step;
                                if (process % 5 == 0) {  //每隔%5的进度返回一次
                                    //listener.onDownLoadProgress(MainActivity.FTP_DOWN_LOADING, process, null);
                                    String lenS1 = "" + currentSize;
                                    Message msg1 = new Message();
                                    msg1.what = 806;//
                                    msg1.obj = lenS1;//
                                    update_tip.sendMessage(msg1);
                                }
                            }
                        }
                    }
                    //recordWindowLog(getLineNumber()+":process:"+process);
                    out.flush();
                    //recordWindowLog(getLineNumber()+":process:"+process);
                    out.close();
                    //recordWindowLog(getLineNumber()+":process:"+process);
                    input.close();
                    //recordWindowLog(getLineNumber()+":process:"+process);
                    // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
                    if (ftpclint.completePendingCommand()) {
                        recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_SUCCESS);                        //listener.onDownLoadProgress(MainActivity.FTP_DOWN_SUCCESS, 0, new File(localPath));
                        // 下载完成之后关闭连接
                        fc.closeConnect();
                        Message msge = new Message();
                        msge.what = 807;// /
                        update_tip.sendMessage(msge);
                        sleep(1000);
                        recordWindowLog(getLineNumber() + ":下载完毕!!!");

                    } else {
                        recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_FAIL);
                    }

                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    private void beginFtpUpdata() {
        new Thread() {
            public void run() {
                try {
                    File configfile1 = new File(CommUtils.appworkpath + updateapkname);
                    configfile1.delete();
                    //hack server read
                    String updateftpserver, updateftppath;
                    int sid = CommUtils.updatexml.trim().indexOf("ftp://");
                    if (sid == -1) {
                        int end = CommUtils.updatexml.trim().indexOf("/");    //"119.255.38.236/media/updatefile/99view1x__update.tar"
                        updateftpserver = CommUtils.updatexml.trim().substring(0, end);
                        updateftppath = CommUtils.updatexml.trim().substring(end, CommUtils.updatexml.trim().length());
                    } else {
                        int end = CommUtils.updatexml.trim().indexOf("/", (sid + 6));    //"ftp://119.255.38.236/media/updatefile/99view1x__update.tar"
                        updateftpserver = CommUtils.updatexml.trim().substring((sid + 6), end);
                        updateftppath = CommUtils.updatexml.trim().substring(end, CommUtils.updatexml.trim().length());
                    }
                    recordWindowLog(getLineNumber() + ":" + CommUtils.updatexml + ":updateftpserver:" + updateftpserver + ":updateftppath:" + updateftppath);
                    FTP fc = new FTP(updateftpserver);
                    //FTP fc=new FTP(updateftpserver,"admin","admin");
                    FTPClient ftpclint = fc.getftpClientHandel();
                    // 打开FTP服务
                    try {
                        fc.openConnect();
                        ///listener.onDownLoadProgress(MainActivity.FTP_CONNECT_SUCCESSS, 0, null);
                        //recordWindowLog("FTP_CONNECT_SUCCESSS!!");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        //listener.onDownLoadProgress(MainActivity.FTP_CONNECT_FAIL, 0, null);
                        return;
                    }

                    // 先判断服务器文件是否存在
                    FTPFile[] files = ftpclint.listFiles(updateftppath);
                    if (files.length == 0) {
                        //listener.onDownLoadProgress(MainActivity.FTP_FILE_NOTEXISTS, 0, null);
                        return;
                    }

                    //创建本地文件夹
                    File mkFile = new File(CommUtils.appworkpath);
                    if (!mkFile.exists()) {
                        mkFile.mkdirs();
                    }

                    String localPath = CommUtils.appworkpath + updateapkname;
                    // 接着判断下载的文件是否能断点下载
                    long serverSize = files[0].getSize(); // 获取远程文件的长度
                    logger.debug("serverSize:" + serverSize);
                    File localFile = new File(localPath);
                    long localSize = 0;
                    if (localFile.exists()) {
                        localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
                        if (localSize > serverSize) {
                            File file = new File(localPath);
                            file.delete();
                            localSize = 0;
                        } else if (localSize == serverSize) {
                            return;
                        }
                        logger.debug("localSize:" + localSize);
                    }
                    Message msg = new Message();
                    String lenS = "" + serverSize;
                    msg.what = 805;//
                    msg.obj = lenS;
                    update_tip.sendMessage(msg);
                    recordWindowLog(getLineNumber() + ":开始下载!!!");
                    // 进度
                    long step = serverSize / 100;
                    long process = 0;
                    long currentSize = 0;
                    // 开始准备下载文件
                    OutputStream out = new FileOutputStream(localFile, true);
                    ftpclint.setRestartOffset(localSize);
                    InputStream input = ftpclint.retrieveFileStream(updateftppath);
                    byte[] b = new byte[1024];
                    int length = 0;
                    while ((length = input.read(b)) != -1) {
                        out.write(b, 0, length);
                        currentSize = currentSize + length;
                        if (currentSize / step != process) {
                            process = currentSize / step;
                            if (process % 5 == 0) {  //每隔%5的进度返回一次
                                //listener.onDownLoadProgress(MainActivity.FTP_DOWN_LOADING, process, null);
                                String lenS1 = "" + currentSize;
                                Message msg1 = new Message();
                                msg1.what = 806;//
                                msg1.obj = lenS1;//
                                update_tip.sendMessage(msg1);
                            }
                        }
                    }
                    out.flush();
                    out.close();
                    input.close();

                    // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
                    if (ftpclint.completePendingCommand()) {
                        recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_SUCCESS);                        //listener.onDownLoadProgress(MainActivity.FTP_DOWN_SUCCESS, 0, new File(localPath));
                        // 下载完成之后关闭连接
                        fc.closeConnect();
                        Message msge = new Message();
                        msge.what = 807;// /
                        update_tip.sendMessage(msge);
                        sleep(1000);
                        recordWindowLog(getLineNumber() + ":下载完毕，开始安装!!!");
                        long templ = System.currentTimeMillis();
                        recordWindowLog(getLineNumber() + ":playerUpdateTime:" + templ);
                        sp.edit().putLong("playerUpdateTime", templ).apply();
                        installApk(localFile);
                    } else {
                        recordWindowLog(getLineNumber() + ":" + MainActivity.FTP_DOWN_FAIL);
                    }

                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":beginHttpUpdata  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    private void beginHttpUpdata() {
        new Thread() {
            public void run() {
                try {
                    //从资源文件获取服务器 地址
                    recordWindowLog(getLineNumber() + ":beginHttpUpdata!!");
                    //String path = "http://119.255.38.236/update.xml";
                    //String path = "http://o2o4.99we.net/appupdate/update_com.xml";
                    String path = CommUtils.updatexml;
                    recordWindowLog("updatexml:" + path);
                    URL urlttt = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) urlttt.openConnection();
                    conn.setConnectTimeout(5000);
                    InputStream is = conn.getInputStream();
                    recordWindowLog(getLineNumber() + ":");
                    updataxmlinfo = getUpdataInfo(is);
                    recordWindowLog(getLineNumber() + ":" + updataxmlinfo.getServerUrl());
                    String versionname = getVersionName();
                    recordWindowLog(getLineNumber() + ":" + versionname);
                    int versioncode = getVersionCode();
                    recordWindowLog(getLineNumber() + ":" + versioncode);
                    //logger.debug("versionname:"+versionname);
                    //logger.debug("updataxmlinfo:"+updataxmlinfo.getVersion());
                    recordWindowLog("nowapp versionString:" + versionname + ",versionCode:" + versioncode);
                    recordWindowLog("info  version:" + updataxmlinfo.getVersion() + ",url:" + updataxmlinfo.getUrl());
//		            if(updataxmlinfo.getVersion().equals(versionname)){
//		            	logger.debug("版本号相同无需升级!!");
//		                LoginMain();
//		            }else{
//		                logger.debug("版本号不同 ,提示用户升级!!");
//		        		//////showUpdataDialog();
                    downLoadApk();
                    /////downLoadApkwithoutProcessDialog();
//		            }
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":beginHttpUpdata  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    private void beginFtpUpload() {
        new Thread() {
            public void run() {
                try {
                    //从资源文件获取服务器 地址
                    recordWindowLog(getLineNumber() + ":beginFtpUpload!!");
                    String local = Environment.getExternalStorageDirectory().getPath() +"/99view/Service99view.log";
                    //Environment.getExternalStorageDirectory() + File.separator +"99view"+ File.separator + "Service99viewclass.log";
                    recordWindowLog(getLineNumber() + ":!!");
                    FTP fc = new FTP("119.255.38.236");
                    recordWindowLog(getLineNumber() + ":!!");
                    fc.uploadSingleFile1("1111.txt", local, "/test/");
                    recordWindowLog(getLineNumber() + ":!!");
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":beginFtpUpload  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    private void checksystemfile() {
        new Thread() {
            public void run() {
                try {

                    //recordWindowLog(getLineNumber()+":!!");
                    String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    StatFs fsss = new StatFs(storage);
                    //可用的blocks的数量
                    long availableBolocks = fsss.getAvailableBlocksLong();
                    //单个block的大小
                    long blockSize = fsss.getBlockSizeLong();
                    long available = availableBolocks * blockSize;
                    long avmb = available / 1024 / 1024;
                    recordWindowLog(getLineNumber() + ":!!available:" + avmb);
                    if (avmb < CommUtils.Ftpdiskcleansize) {
                        recordWindowLog(getLineNumber() + ":updatesystemlog!!");
                        FTP fc = new FTP(CommUtils.hackconfigfileserver, "admin", "admin");
                        File dirfile = new File(Environment.getExternalStorageDirectory() + File.separator + "99view" + File.separator + "log" + File.separator);
                        File[] fs = dirfile.listFiles();
                        for (File f : fs) {
                            String local = f.getCanonicalPath();
                            String remotefilename = f.getName();
                            recordWindowLog(getLineNumber() + ":local:" + local + ":remote:" + remotefilename + "!!!");
                            if ((remotefilename.length() != 0) && (local.length() != 0)) {
                                if (remotefilename.contains(".log.")) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
                                    String ends = sdf.format(new Date());
                                    remotefilename = remotefilename + ends;
                                    String addfodername = "_projone_" + CommUtils.PlayerName;
                                    fc.uploadSingleFile1(remotefilename, local, CommUtils.syslogserverdir + CommUtils.PlayerDeviceSn + addfodername + "/");
                                    f.delete();
                                } else {
                                    String addfodername = "_projone_" + CommUtils.PlayerName;
                                    fc.uploadSingleFile1(remotefilename, local, CommUtils.syslogserverdir + CommUtils.PlayerDeviceSn + addfodername + "/");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":updatesystemlog  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    private void updatesystemlog() {
        new Thread() {
            public void run() {
                try {
                    //从资源文件获取服务器 地址
                    recordWindowLog(getLineNumber() + ":updatesystemlog!!");
                    FTP fc = new FTP(CommUtils.hackconfigfileserver, "admin", "admin");
                    File dirfile = new File(Environment.getExternalStorageDirectory() + File.separator + "99view" + File.separator + "log" + File.separator);
                    File[] fs = dirfile.listFiles();
                    for (File f : fs) {
                        String local = f.getCanonicalPath();
                        String remotefilename = f.getName();
                        recordWindowLog(getLineNumber() + ":local:" + local + ":remote:" + remotefilename + "!!!");
                        if ((remotefilename.length() != 0) && (local.length() != 0)) {
                            if (remotefilename.contains(".log.")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
                                String ends = sdf.format(new Date());
                                remotefilename = remotefilename + ends;
                                String addfodername = "_projone_" + CommUtils.PlayerName;
                                fc.uploadSingleFile1(remotefilename, local, CommUtils.syslogserverdir + CommUtils.PlayerDeviceSn + addfodername + "/");
                                f.delete();
                            } else {
                                String addfodername = "_projone_" + CommUtils.PlayerName;
                                fc.uploadSingleFile1(remotefilename, local, CommUtils.syslogserverdir + CommUtils.PlayerDeviceSn + addfodername + "/");
                            }
                        }
                    }
                    //recordWindowLog(getLineNumber()+":!!");
                    String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    StatFs fsss = new StatFs(storage);
                    //可用的blocks的数量
                    long availableBolocks = fsss.getAvailableBlocksLong();
                    //单个block的大小
                    long blockSize = fsss.getBlockSizeLong();
                    long available = availableBolocks * blockSize;
                    long avmb = available / 1024 / 1024;
                    recordWindowLog(getLineNumber() + ":!!available:" + avmb);
                    if (avmb < CommUtils.Ftpdiskcleansize) {
                        delvideofile("");
                    }
                } catch (Exception e) {
                    recordWindowLog(getLineNumber() + ":updatesystemlog  errr!!:" + e.getMessage());
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //从设置界面返回时进行的操作
        if (requestCode == 100 && 150 == resultCode) {
            boolean changeset = false;
            String serviceNewIp = data.getExtras().getString("serviceIp_yj");
            if (serviceNewIp != null && serviceNewIp.length() > 0) {
                if (!CommUtils.Server_Url.intern().equals(serviceNewIp.intern())) {
                    CommUtils.Server_Url = serviceNewIp;
                    String allsssss = CommUtils.updatexml;
                    int end = allsssss.trim().indexOf("/");//"119.255.38.236/media/updatefile/99view1x__update.tar"
                    CommUtils.updatexml = CommUtils.Server_Url + allsssss.trim().substring(end, allsssss.trim().length());
                    sp.edit().putString("updatexml", CommUtils.updatexml).apply();
                    changeset = true;
                }
            }
            String serviceNewPort = data.getExtras().getString("servicePort_yj");
            if (serviceNewPort != null && serviceNewPort.length() > 0) {
                if (CommUtils.Server_Port != Integer.parseInt(serviceNewPort)) {
                    CommUtils.Server_Port = Integer.parseInt(serviceNewPort);
                    changeset = true;
                }
            }
            String serviceNewRegisterCode = data.getExtras().getString("serverRegistCode_yj");
            if (serviceNewRegisterCode != null && serviceNewRegisterCode.length() == 10) {
                CommUtils.Server_RegistCode = serviceNewRegisterCode;
                changeset = true;
            }
            String serviceNewPlayerName = data.getExtras().getString("playerName_yj");
            if (serviceNewPlayerName != null && serviceNewPlayerName.length() > 0) {
                CommUtils.PlayerName = serviceNewPlayerName;
                appName = serviceNewPlayerName;
            }
            recordWindowLog("back from setting ui");
            if (changeset) {
                recordWindowLog("resetting!!!");
                connFlag = true;
                connFlagInactive = true;
                if (group != null) {
                    group.shutdownGracefully();
                }

                connectServer();
            }
        }
        //从设置界面清空节目单返回时进行的操作
        if (requestCode == 100 && 160 == resultCode) {
            //writeSysfs("/sys/class/graphics/fb0/blank","0");
            //writeSysfs("/sys/class/graphics/fb1/blank","0");

            defaultUrl = "file:///android_asset/ad.html";
            currentUrl = defaultUrl;
            QuartzManager.shutdownJobs();
            sp.edit().putString("default_url", "").apply();
            sp.edit().putString("cron_url", "").apply();
            cleanplaylist();
            sp.edit().remove("rn_id_buffer").apply();//删掉立即播
            map_view.loadUrl(getRefreshUrl(defaultUrl));
            tipToastUtil.showTipsSuccess("清空节目单完成");
            recordWindowLog("clear playlist");

//        	Message msg = Message.obtain();
//    		msg.what = 820;
//    		update_tip.sendMessage(msg);

            //recordWindowLog("ftpdownload");
            //ftpdownload("/media/video/1.avi");
            //ftpdownload("/media/video/1111.mp4");

            //updateconfigfile();
            //recordWindowLog("updateconfigfile");
        }

        //从设置界面打开调试框返回时进行的操作
        if (requestCode == 100 && 170 == resultCode) {

            if (!debugOverViState) {
//        		map_view.setVisibility(View.VISIBLE);
//        		blankView.setVisibility(View.GONE);
                debugOverlay.setDebugVisible(true);
                debugOverViState = true;
                recordWindowLog("open debug!");
                //map_view.loadUrl(getRefreshUrl(defaultUrl));
            } else {
//        		map_view.setVisibility(View.GONE);
//        		blankView.setVisibility(View.VISIBLE);
//        		try {
//        			 Bitmap blankbitmap;
//        			 InputStream fis = getAssets().open("blank.bmp");
//        			 blankbitmap = BitmapFactory.decodeStream(fis);
//        			 blankView.setImageBitmap(blankbitmap);
//           	     } catch (Exception e) {
//           	          e.printStackTrace();
//           	     }
                debugOverlay.setDebugVisible(false);
                debugOverViState = false;
                recordWindowLog("close debug!");
                //map_view.loadUrl("http://h50.99view.com/index.php?s=Admin/Index/index/showType/2");

            }
        }
        if (requestCode == 100 && 180 == resultCode) {
            logger.debug("before send 804");
            Message msg = Message.obtain();
            msg.what = 804;
            update_tip.sendMessage(msg);
            logger.debug("after send 804");
        }
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(this,"not granted",Toast.LENGTH_SHORT);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            destroyAll();
//	    	logger.debug("before send 804");
//        	Message msg = Message.obtain();
//    		msg.what = 804;
//    		update_tip.sendMessage(msg);
//    		logger.debug("after send 804");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SelfInfoActivity.class);
            MainActivity.this.startActivityForResult(intent, 100);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAll();
    }
}

