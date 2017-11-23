package com.zr.webview.util;

public class SecondActivityEvent {
	private String flag;
    private String text;	///url
    private String video;
    private String size;

    private String urlType;
    private String urlId;
    private String startTime;
    private String endTime;
    
    private int alllen;
	private int remainlen;
	private long startPlayTime;
	
    public SecondActivityEvent(String flag , String text , String video , String size)
    {
    	this.flag = flag;
        this.text = text;
        this.video = video;
        this.size = size;
    }
    
    public SecondActivityEvent(String flag , String text , String video , String size, String urlType, String urlId, String startTime, String endTime, int alllen, int remainlen, long startPlayTime)
    {
    	this.flag = flag;
        this.text = text;
        this.video = video;
        this.size = size;
        
        this.urlType = urlType;
        this.urlId = urlId;
        this.startTime = startTime;
        this.endTime = endTime;
        
        this.alllen = alllen;
        this.remainlen = remainlen;
        this.startPlayTime = startPlayTime;
    }
    
    public String getText(){
        return text;
    }
    public String getFlag(){
        return flag;
    }
    public String getVideo(){
        return video;
    }
    public String getSize(){
        return size;
    }
    
    public String getUrlType(){
        return urlType;
    }
    public String getUrlId(){
        return urlId;
    }
    public String getStartTime(){
        return startTime;
    }
    public String getEndTime(){
        return endTime;
    }
    
    public int getAllLen(){
        return alllen;
    }
    public int getRemainLen(){
        return remainlen;
    }
    public long getStartPlayTime(){
        return startPlayTime;
    }
}
