package com.zr.webview.util;

public class DataActivityEvent {
	private String flag;
    private String text;
    public DataActivityEvent(String flag , String text)
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
