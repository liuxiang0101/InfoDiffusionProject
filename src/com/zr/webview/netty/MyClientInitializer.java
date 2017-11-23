package com.zr.webview.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import android.content.Context;

public class MyClientInitializer extends ChannelInitializer<SocketChannel> {
	private Context context;
	private static int WRITE_WAIT_SECONDS = 60;
	public MyClientInitializer(Context ctx){
		this.context = ctx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
//		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//		pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
//		pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast("decoder", new MyByteDecoder());
        pipeline.addLast("encoder", new MyByteEncoder());
		pipeline.addLast("ping", new IdleStateHandler(0, WRITE_WAIT_SECONDS, 0,TimeUnit.SECONDS));
		pipeline.addLast("handler",new MyClientHandler(context));
	}
}
