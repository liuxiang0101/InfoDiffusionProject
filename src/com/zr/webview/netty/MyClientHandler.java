package com.zr.webview.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import android.content.Context;
import android.util.Log;

import com.zr.webview.util.CommUtils;
import com.zr.webview.util.DataActivityEvent;
import com.zr.webview.util.HeartActivityEvent;

import de.greenrobot.event.EventBus;

public class MyClientHandler extends SimpleChannelInboundHandler<String> {
	private Context ctx;

	public MyClientHandler(Context context) {
		this.ctx = context;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		Log.i("MyClientHandler", "channelRead0->msg="+msg);
		EventBus.getDefault().post(new DataActivityEvent(CommUtils.Socket_Flag_Data , msg));
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		EventBus.getDefault().post(new DataActivityEvent(CommUtils.Socket_Flag_Inactive , null));
	}
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                /*写超时，心跳时间到了*/   
            	EventBus.getDefault().post(new HeartActivityEvent(CommUtils.Socket_Flag_Heart , null));
            }
        }
    }
}
