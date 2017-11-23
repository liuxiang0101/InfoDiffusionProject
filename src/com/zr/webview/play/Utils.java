package com.zr.webview.play;

import java.io.File;

import android.os.Environment;
public class Utils {
	public static int screen_Width = 0;
	public static int screen_Height = 0;
	public static double width_Factor  = 1;
	public static double height_Factor = 1;
	public static String getExBaseDir(){
		return Environment.getExternalStorageDirectory() + "/ad";
	}
	
	public static String getFilenameFromUrl(String url){
		return url.substring(url.lastIndexOf("/") + 1);
	}
	
	public static void initDir(){		
		File dir = new File(getExBaseDir());
		if (!dir.exists()){
			dir.mkdir();
		}
		String[] dirs = new String[]{
				 "ad","css","img","js","lib","appcache"
		};
		for (String s : dirs){
			dir = new File(getExBaseDir() + "/" + s);
			if (!dir.exists()){
				dir.mkdir();
			}			
		}		
		
	}

}
