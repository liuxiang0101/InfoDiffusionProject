package com.zr.webview.util;

import java.io.Serializable;

public class PlanModel implements Serializable{
	private String url;
	private String video; 
	private String size;
	private String beginTime;
	private String endTime;
	private String urlId;
	private String urlType;
	private int alllen;
	private int remainlen;
	private long startPlayTime;
	
	public long getStartPlayTime() {
		return startPlayTime;
	}
	public void setStartPlayTime(long startPlayTime) {
		this.startPlayTime = startPlayTime;
	}
	
	public int getAllLen() {
		return alllen;
	}
	public void setAllLen(int alllen) {
		this.alllen = alllen;
	}
	
	public int getRemainlen() {
		return remainlen;
	}
	
	public void setRemainlen(int remainlen) {
		this.remainlen = remainlen;
	}
	
	public String getUrlType() {
		return urlType;
	}
	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}
	
	public String getUrlId() {
		return urlId;
	}
	public void setUrlId(String urlId) {
		this.urlId = urlId;
	}
	
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getVideo() {
		return video;
	}
	public void setVideo(String video) {
		this.video = video;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	
}
