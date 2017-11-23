package com.zr.webview.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.netty.channel.Channel;
public class CommUtils {
    public static int Ftpdisksmallsize = 100; //单位MB
    public static int Ftpdiskcleansize = 500; //单位MB
    public static int Ftpport = 21; //ftp端口
    public static String Ftpusr = "admin";//"admin2"; //
    public static String Ftppasswd = "admin";//"admin135"; //
    public static String Ftpserver = "yscene.99we.cn";//"h50.haoping360.com"; //"h50.99view.com";//

    public static String PlayerDeviceSn="";
    public static String PlayerName = "gj_"; //0.1
    public static int PlayerGJNO = 12345; //

    public static String hackconfigfileserver = "124.207.66.53";
    public static String hackconfigfilepath = "/media/app/";
    public static String hackconfigfilename = "99view1x__updateconfig_proj_demo_201705.gjapx";

    public static String AppVersion = "0511 V1";
    public static String Server_RegistCode = "";//"2106647651";
    public static String Server_Url = "yk.99we.cn";
    public static String updatexml = "yscene.99we.cn/updatefile/99view1x__update_proj_demo_201705.tar";
    public static String updatexml1 = "118.190.44.117/updatefile/99view1x__update_proj_201703.txt";
    public static String updatexmlrightnowtag = "_uRN";
    public static String playlisthtmladdrhead = "http://yscene.99we.cn/";
    public static String playlisthtmladdrheadTag = ".99we.cn";
    public static String playlisthtmlvideoftpaddr = "yscene.99we.cn";

    public static String playlisthtmlvideoftpaddryscene = "yscene.99we.cn";
    public static String playlisthtmlvideoftpaddrh50 = "h50.99view.com";

    public static String playlisthtmladdr = "Application/Admin/View/webRoot/";
    public static String playlisthtmladdrEnd = "/index.html";

    public static String appworkpath = "/sdcard/99view/";
    public static String syslogserverdir = "/media/logs/1x/";

    public static int     Server_Port = 15525; //8888
    public static String Socket_Flag_Data = "100000";
    public static String Socket_Flag_Inactive = "100001";
    public static String Socket_Flag_Heart = "000000";
    public static String Socket_Flag_Plan = "200000";
    public static String Socket_Flag_NewPlan = "200001";
    public static String Socket_Flag_NewControl = "300000";
    public static String Socket_Flag_Reboot = "400000";
    public static int    Server_Reconn_Period = 10 * 1000;
    public static int    Video_Rel_Size = 1000;
    public static String Video_Default_FName = "default.mp4";
    public static List<String> planAlarmIds = new ArrayList<String>();
    private static final Logger logger = LoggerFactory.getLogger();
    public static float    parsevideox;
    public static float    parsevideoy;
    public static float    parsevideow;
    public static float    parsevideoh;
    public static String   parsedataurl;
    public static String   parsedatatype;
    public static String[] downloadaddr;
    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */

    public static boolean isNetworkAvailable(Context context)
    {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     *
     * @param gjno 设备广角号
     * @param status 1 play 2 stop  3 shutdown	播放状态
     * @param volume 音量
     * @param url 当前播出url
     * @param urlType 当前播出url类型  1 默认，2 立即，3 计划
     * @param sn 设备硬件号
     * @return
     */
    public static String getHeartProtocol(String appName , int gjno , int status , int volume , String url , int urlType , String sn , String videoUrl){
        //String time = getCurrentTime();
        String time = getCurrentTimeNew();
        String result = "{\"type\":\"heartbeat\",\"name\":\""+appName+"\",\"gjno\":"+gjno+",\"status\":"+status+",\"volume\":"+volume+",\"url\":\""+url+"\",\"videoUrl\":\""+videoUrl+"\",\"urltype\":"+urlType+",\"devicesn\":\""+sn+"\", \"time\":\""+time+"\"}";
        return result;
    }
    /**
     *
     * @param gjno 设备广角号
     * @param status 1 play 2 stop  3 shutdown	播放状态
     * @param volume 音量
     * @param url 当前播出url
     * @param urlType 当前播出url类型  1 默认，2 立即，3 计划
     * @param sn 设备硬件号
     * @return
     */
    public static String getHeartProtocolNew(String appName , int gjno , int status , int volume , String url , int urlType , String sn , String videoUrl, String registerCode, String versionStr){
        //String time = getCurrentTime();
        String time = getCurrentTimeNew();
        String result = "{\"type\":\"heartbeat\",\"name\":\""+appName+"\",\"gjno\":"+gjno+",\"status\":"+status+",\"volume\":"+volume+",\"url\":\""+url+"\",\"videoUrl\":\""+videoUrl+"\",\"urltype\":"+urlType+",\"devicesn\":\""+sn+"\",\"registerCode\":\""+registerCode+"\",\"versionString\":\""+versionStr+"\",\"time\":\""+time+"\"}";
        return result;
    }
    public static String getRegistToServer(String appName , int gjno , int status , int volume , String url , int urlType , String sn , String videoUrl, String registerCode, String versionStr){
        //String time = getCurrentTime();
        String time = getCurrentTimeNew();
        String result = "{\"type\":\"register\",\"name\":\""+appName+"\",\"gjno\":"+gjno+",\"status\":"+status+",\"volume\":"+volume+",\"url\":\""+url+"\",\"videoUrl\":\""+videoUrl+"\",\"urltype\":"+urlType+",\"devicesn\":\""+sn+"\",\"registerCode\":\""+registerCode+"\",\"versionString\":\""+versionStr+"\",\"time\":\""+time+"\"}";
        return result;
    }
    public static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss");
        return sdf.format(new Date());
    }
    public static String getCurrentTimeNew(){
        long templ= System.currentTimeMillis();
        return ""+templ;
    }
    public static void sendNettyData(Channel channel ,String val){
        channel.writeAndFlush(val + "\n");
    }
    public static int getRandomInt(){
        int max=Integer.MAX_VALUE;
        int min=10;
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
    }
    /**
     * MD5 32位加密方法一 小写
     *
     * @param str
     * @return
     */

    public final static String get32MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] strTemp = s.getBytes();
            // 使用MD5创建MessageDigest对象
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte b = md[i];
                str[k++] = hexDigits[b >> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * MD5 32位加密方法二 小写
     *
     * @param str
     * @return
     */

    public final static String get32MD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    /**
     * Md5 32位 or 16位 加密
     *
     * @param plainText
     * @return 32位加密
     */
    public static String Md5(String plainText) {
        StringBuffer buf = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
        }
        return buf.toString();
    }
    public static void setDateTime(String dateval ,String pattern)throws IOException, InterruptedException{
        requestPermission();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        long when = 0;
        try {
            when = sdf.parse(dateval).getTime();
        } catch (Exception e) {
            // TODO: handle exception
            logger.error(":"+e.getMessage());
        }
        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
        long now = Calendar.getInstance().getTimeInMillis();
        if(now - when > 1000)
            throw new IOException("failed to set Date.");
    }
    public static void setDateTimeNew(String dateval ,String pattern)throws IOException, InterruptedException{
        //requestPermission();
        long when=0;
        try {
            when = Long.parseLong(dateval);
            if (when / 1000 < Integer.MAX_VALUE) {
                SystemClock.setCurrentTimeMillis(when);
            }
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("setDateTimeNew err:"+e.getMessage());
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if(now - when > 1000)
            throw new IOException("failed to set Date.");
    }
    public static void setDateTime(int year, int month, int day, int hour, int minute) throws IOException, InterruptedException {
        requestPermission();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month-1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
        long now = Calendar.getInstance().getTimeInMillis();
        if(now - when > 1000)
            throw new IOException("failed to set Date.");
    }

    public static void setDate(int year, int month, int day) throws IOException, InterruptedException {
        requestPermission();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
        long now = Calendar.getInstance().getTimeInMillis();
        if(now - when > 1000)
            throw new IOException("failed to set Date.");
    }

    public static void setTime(int hour, int minute) throws IOException, InterruptedException {
        requestPermission();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            SystemClock.setCurrentTimeMillis(when);
        }
        long now = Calendar.getInstance().getTimeInMillis();
        if(now - when > 1000)
            throw new IOException("failed to set Time.");
    }

    static void requestPermission() throws InterruptedException, IOException {
        //createSuProcess("chmod 666 /dev/alarm").waitFor(); 
        Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c", "chmod 666 /dev/alarm"});
    }

    static Process createSuProcess() throws IOException  {
        File rootUser = new File("/system/xbin/ru");
        logger.debug("rootUser:"+rootUser.getAbsolutePath());
        if(rootUser.exists()) {
            return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
        } else {
            return Runtime.getRuntime().exec("su");
        }
    }

    static Process createSuProcess(String cmd) throws IOException {
        DataOutputStream os = null;
        Process process = createSuProcess();
        try {
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
        return process;
    }

}
