package com.zr.webview.netty;

public interface IMsgCallback {
	
	public void clientReceiveMsg(String msg);
	public void serverReceiveMsg(String msg);
}
