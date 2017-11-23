package com.zr.webview.util;

public class HeartActivityEvent {
	private String flag;
    private String text;
    public HeartActivityEvent(String flag , String text)
    {
    	this.flag = flag;
        this.text = text;
    }
    public String getText(){
        return text;
    }
    public String getFlag(){
        return flag;
    }
}
