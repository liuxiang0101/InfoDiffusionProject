/**
 * 
 */
package com.zr.webview.play;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;

public class PlayControlUtil {
	private static String TAG = "PlayControlUtil";
	public static void initWebViewSet(WebView webView , WebViewClient myWebviewCient , WebChromeClient chromeClient , Context context){
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setWebViewClient(myWebviewCient);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(chromeClient);
		webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setVerticalScrollBarEnabled(false);	
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		final String USER_AGENT_STRING = webView.getSettings().getUserAgentString();
		webView.getSettings().setUserAgentString( USER_AGENT_STRING );
		//final String USER_AGENT_STRING = webView.getSettings().getUserAgentString() + " Rong/2.0";
		//
		//webView.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setSaveFormData(false);
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setPluginState(WebSettings.PluginState.ON);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setDatabaseEnabled(true);
		String databasePath = context.getDir("database", 0).getPath();
		webView.getSettings().setDatabasePath(databasePath);
		webView.getSettings().setGeolocationEnabled(true);
		webView.getSettings().setAppCacheMaxSize(5242880L);
	    webView.getSettings().setAppCachePath(databasePath);
	    webView.getSettings().setAppCacheEnabled(true);
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true); 
		WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.FAR ;   
		//WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.CLOSE ;
		webView.getSettings().setDefaultZoom(zoomDensity);
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.setInitialScale(50);
	}
	/**
	 * 初始化时将assets目录文件拷贝到sd卡
	 * @param context
	 * @param assetDir
	 */
	public static void dataLog(Context context,  String assetDir) {
        String[] subFileDirs;   
        int fileNums = 0;
        int fileNums_F = 0;
        File outFile = null;
        InputStream in =null;
            try    
            {    
            	context.getApplicationContext().getFilesDir().getAbsolutePath();
            	subFileDirs = context.getResources().getAssets().list(assetDir);
                for(String subDir : subFileDirs){
                	Log.i(TAG, "获取assets下子目录【"+assetDir+"/"+subDir+"】");
                	String[] subFiles =  context.getAssets().list(assetDir+"/"+subDir);
                	fileNums_F += subFiles.length;
                	for(String subFile : subFiles){
                		Log.i(TAG, "获取assets下子文件【"+assetDir+"/"+subDir+"/"+subFile+"】");
                		in = context.getAssets().open(assetDir+"/"+subDir+"/"+subFile); 
                		int fileSize = in.available();
                		File outDir = new File(Utils.getExBaseDir()+"/"+subDir);
                		if(outDir.exists()&&outDir.isDirectory()){
                			outFile = new File(Utils.getExBaseDir()+"/"+subDir+"/"+subFile);
//                			if(outFile.exists()){
//                				Log.i(TAG, "文件【"+subFile+"】已经存在");
//                				fileNums++;
//                				continue;
//                			}
                			OutputStream out = new FileOutputStream(outFile);    
                            byte[] buf = new byte[1024];    
                            int len;
                            int count = 0;
                            int nStartPos=0;
                            while ((len = in.read(buf)) > 0)    
                            {   
                                out.write(buf, 0, len);    
                                nStartPos += len; 
                            	if(fileSize>0){
                            		count = nStartPos*100/fileSize;
                            	}
                            	if(count >= 100){
                            		fileNums++;
                            	}
                            }    
                            in.close();    
                            out.close();
                		}
                	}
                }
                if(fileNums == fileNums_F){
                	Log.i(TAG, "共复制【"+fileNums+"】个文件");
                }
            }    
            catch (IOException e1)    
            {   Log.e(TAG, "列举Assets文件异常:"+e1.getMessage()); 
                return;    
            }    
}
	public static void copyAssets(Context context,  String assetDir) {
        String[] subFileDirs;   
        int fileNums = 0;
        int fileNums_F = 0;
        File outFile = null;
        InputStream in =null;
            try    
            {    
            	subFileDirs = context.getResources().getAssets().list(assetDir);
                for(String subDir : subFileDirs){
                	Log.i(TAG, "获取assets下子目录【"+assetDir+"/"+subDir+"】");
                	String[] subFiles =  context.getAssets().list(assetDir+"/"+subDir);
                	fileNums_F += subFiles.length;
                	for(String subFile : subFiles){
                		Log.i(TAG, "获取assets下子文件【"+assetDir+"/"+subDir+"/"+subFile+"】");
                		in = context.getAssets().open(assetDir+"/"+subDir+"/"+subFile); 
                		int fileSize = in.available();
                		File outDir = new File(Utils.getExBaseDir()+"/"+subDir);
                		if(outDir.exists()&&outDir.isDirectory()){
                			outFile = new File(Utils.getExBaseDir()+"/"+subDir+"/"+subFile);
//                			if(outFile.exists()){
//                				Log.i(TAG, "文件【"+subFile+"】已经存在");
//                				fileNums++;
//                				continue;
//                			}
                			OutputStream out = new FileOutputStream(outFile);    
                            byte[] buf = new byte[1024];    
                            int len;
                            int count = 0;
                            int nStartPos=0;
                            while ((len = in.read(buf)) > 0)    
                            {   
                                out.write(buf, 0, len);    
                                nStartPos += len; 
                            	if(fileSize>0){
                            		count = nStartPos*100/fileSize;
                            	}
                            	if(count >= 100){
                            		fileNums++;
                            	}
                            }    
                            in.close();    
                            out.close();
                		}
                	}
                }
                if(fileNums == fileNums_F){
                	Log.i(TAG, "共复制【"+fileNums+"】个文件");
                }
            }    
            catch (IOException e1)    
            {   Log.e(TAG, "列举Assets文件异常:"+e1.getMessage()); 
                return;    
            }    
}
	public static String getFileName(String path) {
		String filename = "";
		String[] pathContents = path.split("[\\\\/]");
		if(pathContents != null){
			int pathContentsLength = pathContents.length;
			String lastPart = pathContents[pathContentsLength-1];
			String[] lastPartContents = lastPart.split("\\.");
			if(lastPartContents != null && lastPartContents.length > 1){
				int lastPartContentLength = lastPartContents.length;
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					if(i < (lastPartContents.length -1)){
						name += lastPartContents[i] ;
						if(i < (lastPartContentLength -2)){
							name += ".";
						}
					}
				}
				String extension = lastPartContents[lastPartContentLength -1];
				filename = name + "." +extension;
			}
		}
		return filename;
	}
	public static File getAdFile(String url) {
		//ftp://adupload:12345678@172.16.42.39/video/res-4-2012-04-10-13-51-53-652.mov
		if (url != null && url.trim().length() > 0 && url.lastIndexOf("/") != -1) {
			String fileName = Utils.getFilenameFromUrl(url);
			
			return new File(new File(Utils.getExBaseDir() + File.separator + "ad"), fileName);
		} else {
			return null;
		}
	}
}
