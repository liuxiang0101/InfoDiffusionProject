package com.zr.webview.util;

public class PlanActivityEvent {
	private String flag;
    private String url;
    private String length; //playSeconds
    private String videoUrl;
    private String videoSize;
    private String urlType;
    private String urlId;
    private String startTime;
    private String endTime;
    
	private int remainlen;
	private long startPlayTime;
	
    public PlanActivityEvent(String flag , String url , String length , String videoUrl , String videoSize)
    {
    	this.flag = flag;
        this.url = url;
        this.length = length;
        this.videoUrl = videoUrl;
        this.videoSize = videoSize;
    }
    public PlanActivityEvent(String flag ,String urlType, String urlId,  String url , String length , String videoUrl , String videoSize)
    {
    	this.urlType = urlType;
        this.urlId = urlId;
    	this.flag = flag;
        this.url = url;
        this.length = length;
        this.videoUrl = videoUrl;
        this.videoSize = videoSize;
    }
    public PlanActivityEvent(String flag , String startTime, String endTime, String urlType, String urlId,  String url , String length , String videoUrl , String videoSize)
    {
    	this.urlType = urlType;
        this.urlId = urlId;
    	this.flag = flag;
        this.url = url;
        this.length = length;
        this.videoUrl = videoUrl;
        this.videoSize = videoSize;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public PlanActivityEvent(String flag , String startTime, String endTime, String urlType, String urlId,  String url)
    {
    	this.flag = flag;
    	this.startTime = startTime;
        this.endTime = endTime;
        
    	this.urlType = urlType;
        this.urlId = urlId;
        this.url = url;
    }
    public PlanActivityEvent(String flag , String url)
    {
    	this.flag = flag;
        this.url = url;
    }
    public int getRemainlen(){
        return remainlen;
    }
    public long startPlayTime(){
        return startPlayTime;
    }
    public String getStartTime(){
        return startTime;
    }
    public String getEndTIme(){
        return endTime;
    }
    public String getUrlType(){
        return urlType;
    }
    public String getUrlId(){
        return urlId;
    }
    public String getUrl(){
        return url;
    }
    public String getFlag(){
        return flag;
    }
    public String getLength(){
        return length;
    }
    public String getVideoUrl(){
        return videoUrl;
    }
    public String getVideoSize(){
        return videoSize;
    }
}
