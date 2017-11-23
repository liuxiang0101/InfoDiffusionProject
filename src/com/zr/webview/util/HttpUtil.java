package com.zr.webview.util;

import java.util.Iterator;
import java.util.Map;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpUtil {
	private static AsyncHttpClient client = new AsyncHttpClient(); // 实例话对象
	static {
		client.setTimeout(11000);
		// 设置链接超时，如果不设置，默认为10s
	}

	public static void get(String urlString, Map<String,String> headerParam, AsyncHttpResponseHandler res)
	// 用一个完整url获取一个string对象
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		client.get(urlString, res);
	}

	public static void get(String urlString, Map<String,String> headerParam, RequestParams params,
			AsyncHttpResponseHandler res)
	// url里面带参数
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.get(urlString, params, res);
	}

	public static void get(String urlString, Map<String,String> headerParam, JsonHttpResponseHandler res)
	// 不带参数，获取json对象或者数组
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.get(urlString, res);
	}

	public static void get(String urlString, Map<String,String> headerParam, RequestParams params,
			JsonHttpResponseHandler res)
	// 带参数，获取json对象或者数组
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.get(urlString, params, res);
	}

	public static void get(String uString, Map<String,String> headerParam, RequestParams params, BinaryHttpResponseHandler res)
	// 下载数据使用，带参数，会返回byte数据
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.get(uString, params, res);
	}
	
	public static void get(String uString, Map<String,String> headerParam, BinaryHttpResponseHandler res)
	// 下载数据使用，会返回byte数据
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.get(uString, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, AsyncHttpResponseHandler res)
	// post数据，不带参数
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, RequestParams params, AsyncHttpResponseHandler res)
	// // post数据，带参数
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, params, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, JsonHttpResponseHandler res)
	// post数据，不带参数，返回json对象
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, RequestParams params, JsonHttpResponseHandler res)
	// // post数据，带参数，返回json对象
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, params, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, BinaryHttpResponseHandler res)
	// post数据，不带参数，返回byte数据
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, res);
	}
	
	public static void post(String uString, Map<String,String> headerParam, RequestParams params, BinaryHttpResponseHandler res)
	// // post数据，带参数，返回byte数据
	{
		if ( null != headerParam )
		{
			Iterator<String> iter = headerParam.keySet().iterator();
			while( iter.hasNext() )
			{
				String entry = iter.next();  
	            client.addHeader(entry, headerParam.get(entry));
			}
		}
		
		client.post(uString, params, res);
	}

	public static AsyncHttpClient getClient() {
		return client;
	}
}
