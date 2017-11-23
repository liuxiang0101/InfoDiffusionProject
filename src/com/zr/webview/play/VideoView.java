/**
 * 
 */
package com.zr.webview.play;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;

import com.zr.webview.MainActivity;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class VideoView extends android.widget.VideoView implements AdPlayView, OnPreparedListener,
		OnCompletionListener, OnErrorListener {
		//OnErrorListener {
	private static final String TAG = MainActivity.class.getName();
	/**
	 * 播放延时时长（秒）
	 */
	private int _playDelayLength = 5;
	private Handler _handler;
	private Context _context;
	private String videoFilePath;
	private String firstVideoFile;
	//private String[] videoFilePathList;
	private List<String> videoFilePathList;
	private int videoPlaylistNowid;
	private static Logger logger1111;
	
	
	public VideoView(Context context) {
		this(context, null);
		this._context = context;
		this.setOnPreparedListener(this);
		this.setOnCompletionListener(this);
		this.setOnErrorListener(this);
		this.videoFilePathList = new ArrayList<String>();
		configLog2();
		logger1111.debug("logger11 videoview  1111!!!");
	}
    public void setVideoFile(String videoFilePath){
    	this.videoFilePath = videoFilePath;
    }
	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this._context = context;
		this.setOnPreparedListener(this);
		this.setOnCompletionListener(this);
		this.setOnErrorListener(this);
		this.videoFilePathList = new ArrayList<String>();
		configLog2();
		logger1111.debug("logger11 videoview  2222!!!");
	}
	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this._context = context;
		this._handler = new Handler();
		this.setOnPreparedListener(this);
		this.setOnCompletionListener(this);
		this.setOnErrorListener(this);
		this.videoFilePathList = new ArrayList<String>();
		configLog2();
		logger1111.debug("logger11 videoview  3333!!!");
	}
	
	public void clearPlaylist(){
		this.videoFilePathList.clear();
		this.firstVideoFile="";
    }
	public void addToPlaylist(String str){
		this.videoFilePathList.add(str);
    }
	public void setFristVideoFile(String firstVideoFile){
    	this.firstVideoFile = firstVideoFile;
    }
	public void playfromvideotime(int baktime){
		logger1111.debug("logger11 playfromvideotime:"+baktime);
		this.seekTo(baktime);
		this.start();
    }
	public int getvideotime(){
		int ret;
		ret=this.getCurrentPosition();
		logger1111.debug("logger11 id:"+videoPlaylistNowid+":time:"+ret);
		return ret;
    }
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		
		setMeasuredDimension(width, height); // 默认不保持
	}

	@Override
	public void onPrepared(final MediaPlayer mp) {
		mp.start();
		//mp.setLooping(true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						Log.i(TAG, "尝试重绘防止视屏区域黑掉");
						invalidate();
					}
				});
			}
		}).start();
		
	}

	@Override
	public void play() {
		logger1111.debug("logger11 play!!!");
		//1.节目重组编制
		Log.i(TAG, "进行节目重组编制");
		// 2.假设节目单中无可播放的节目，并从第一个节目开始尝试播放
		playCurrentPlaylistItem();
	}
	@Override
    public void pause() {
		logger1111.debug("logger11 pause replay it!!!");
		this.start();
    }
	/*
	 * 
	 */
	@Override
	public void stop() {
		Log.i(TAG, "停止节目播放");
		logger1111.debug("logger11 stop!!!");
		this.stopPlayback();
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		logger1111.debug("onCompletion:"+this.videoFilePathList.size());
		if(this.videoFilePathList.size()==0){
			Log.i(TAG, "onCompletion");
			this.start();
		}else{
			if(videoPlaylistNowid>=(this.videoFilePathList.size()-1)){
				String videoFilePathtmp=this.videoFilePathList.get(0);
				videoPlaylistNowid = 0;
				this.setVideoURI(Uri.parse(videoFilePathtmp));
				this.seekTo(0);
				this.start();
				logger1111.debug("onCompletion:1:id:"+videoPlaylistNowid);
			}else{
				videoPlaylistNowid+=1;
				String videoFilePathtmp=this.videoFilePathList.get(videoPlaylistNowid);
				this.setVideoURI(Uri.parse(videoFilePathtmp));
				this.seekTo(0);
				this.start();
				logger1111.debug("onCompletion:2:id:"+videoPlaylistNowid);
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer, int, int)
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, String.format("onError:what %d,extra %d", what, extra));
		logger1111.debug("logger11 onError:"+String.format("onError:what %d,extra %d", what, extra));
		this._handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// 继续尝试从头播放节目单
				logger1111.debug("logger11 onError:"+":id:"+videoPlaylistNowid);
				playCurrentPlaylistItem();
			}
		}, this._playDelayLength * 1000);
		return true;
	}

	/**
	 * 播放当前节目单播放位置指向的节目
	 */
	protected void playCurrentPlaylistItem() {
		logger1111.debug("playCurrentPlaylistItem:"+this.videoFilePathList.size());
		if(this.videoFilePathList.size()==0){
			this.setVideoURI(Uri.parse(videoFilePath));
			this.seekTo(0);
			int volumn = 0;
			int brightness = 100;
			logger1111.debug("logger11 test configLog2!!!");
			AudioManager mAudioManager = (AudioManager) this._context.getSystemService(Context.AUDIO_SERVICE);
			
			//系统音量-56~56
	//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*(volumn+56)/112, AudioManager.FLAG_SHOW_UI);
	      
			//亮度-128~127  不能为0  为0就锁屏了
			WindowManager.LayoutParams lp = ((Activity)this._context).getWindow().getAttributes();
			lp.screenBrightness = (brightness+128)==0?0.1f:(brightness+128)/255f; 
			((Activity)this._context).getWindow().setAttributes(lp);
			this.start();
		}else{
			//firstVideoFile
			
			int idddd;
			if(firstVideoFile.length()!=0){
				idddd=this.videoFilePathList.indexOf(firstVideoFile);
				if(idddd==-1) idddd = 0;
			}else{
				idddd = 0;
			}
			
			String videoFilePathtmp=this.videoFilePathList.get(idddd);
			videoPlaylistNowid = idddd;
			this.setVideoURI(Uri.parse(videoFilePathtmp));
			this.seekTo(0);
			int volumn = 0;
			int brightness = 100;
			logger1111.debug("logger11 videoFilePathtmp:"+videoFilePathtmp+":id:"+videoPlaylistNowid);
			AudioManager mAudioManager = (AudioManager) this._context.getSystemService(Context.AUDIO_SERVICE);
			
			//系统音量-56~56
//			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*(volumn+56)/112, AudioManager.FLAG_SHOW_UI);
	      
			//亮度-128~127  不能为0  为0就锁屏了
			WindowManager.LayoutParams lp = ((Activity)this._context).getWindow().getAttributes();
			lp.screenBrightness = (brightness+128)==0?0.1f:(brightness+128)/255f; 
			((Activity)this._context).getWindow().setAttributes(lp);
			this.start();
		}
	}
	
	public void configLog2()  
    {  
        final LogConfigurator logConfigurator = new LogConfigurator();  
          
        logConfigurator.setFileName(Environment.getExternalStorageDirectory().getPath() + File.separator +"99view" + File.separator +"log"+ File.separator + "99viewvideoview.log");
        // Set the root log level  
        logConfigurator.setRootLevel(Level.ALL);  
        // Set log level of a specific logger  
        logConfigurator.setLevel("org.apache", Level.ERROR);  
        
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        //logConfigurator.setFilePattern("%d %p\#%L\#%c\.%M\#%t\#%m%n");
        logConfigurator.setMaxFileSize(1024*5*1024);  
        logConfigurator.setImmediateFlush(true); 
        
        logConfigurator.configure();  
  
        logger1111 = Logger.getLogger(VideoView.class);  
        logger1111.debug("logger11 test configLog2!!!");
        
    }

}
