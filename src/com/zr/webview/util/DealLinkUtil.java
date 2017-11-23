package com.zr.webview.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealLinkUtil {  
    
    public static String modifyLink(String html,String baseUri) throws MalformedURLException, IOException{  
        Document doc=Jsoup.parse(html,baseUri);  
          
          
        Elements elements=doc.select("a[href!=#]");  
        adsoluteAHref(elements , baseUri);  
        Elements jsElements=doc.select("script[src]");  
        absoluteScriptSrc(jsElements , baseUri);  
          
        Elements linkElements=doc.select("link[href]");  
        absoluteLinkHref(linkElements , baseUri);  
          
          
          
        Element  base=doc.select("base").first();  
        if(base!=null){  
        System.out.println(base.attr("href"));  
        base.attr("href", baseUri);}else{  
            Element head=doc.select("head").first();  
            head.append("<base href=\""+baseUri+"\">");  
        }  
          
        return doc.toString();  
    }  
      
    public static void modifyCssurl(String urlpath) throws IOException{  
        URL url=new URL("http://localhost:8080/novel/User/register.jsp");//  
        Document doc=Jsoup.parse(url.openStream(), "utf-8", "http://sdfsdfsddfBYSJ/");  
        Elements styleCss=doc.select("style");//获得html中的样式标签如<style type="text/css"></style>  
        IteratorStyle(styleCss);  
          
        Elements scriptJs=doc.select("script");//获得<script type="text/javascript"></script>  
        IteratorStyle(scriptJs);  
    }  
      
    //处理<script type="text/javascript">脚本  
    public static void IteratorStyle(Elements elements){  
        Iterator<Element> iterator=elements.iterator();  
        while(iterator.hasNext()){  
            Element element=iterator.next();  
                System.out.println(element.data());//获得<script type="text/javascript">中的值  
                                                    //<style type="text/css"></style>中的值  
        }  
    }  
    //从style中获得 url的值  
    public static void getURL(String url){  
        Pattern p = Pattern.compile("url\\((.*)\\)");//匹配  url(任何)  
        Matcher m = p.matcher(url);  
        if(m.find()){  
            System.out.println(m.group(1));//获取括号中的地址  
        }  
    }  
      
    //将<script src>转换为绝对地址   
    public static void absoluteScriptSrc(Elements jsElements,String baseUri) throws MalformedURLException{  
        Iterator<Element> iterator=jsElements.iterator();  
        while(iterator.hasNext()){  
            Element element=iterator.next();  
            String src=element.attr("abs:src");//将所有的相对地址换为绝对地址;  
            element.attr("src",src);//装换为  
        }  
    }  
      
    //将Img src 装换为绝对的url  
    public static void absoluteImagSrc(Elements imagElements,String baseUri) throws MalformedURLException{  
        Iterator<Element> iterator=imagElements.iterator();  
        while(iterator.hasNext()){  
            Element element=iterator.next();  
            String src=element.attr("abs:src");//将所有的相对地址换为绝对地址;  
            element.attr("src",src);//装换为  
              
        }  
    }  
      
    //将Link href 装换为绝对的url  
        public static void absoluteLinkHref(Elements linkElements,String baseUri) throws MalformedURLException{  
            Iterator<Element> iterator=linkElements.iterator();  
            while(iterator.hasNext()){  
                Element element=iterator.next();  
                String src=element.attr("abs:href");//将所有的相对地址换为绝对地址;  
                element.attr("href",src);//装换为  
            }  
        }  
    //将所有的的<a href>转换为绝对地址  
    public static void adsoluteAHref(Elements AElements , String baseUri){  
        Iterator<Element> iterator=AElements.iterator();  
        while(iterator.hasNext()){  
            Element element=iterator.next();  
            String href=element.attr("abs:href");//将所有的相对地址换为绝对地址;  
            element.attr("href",href);  
        }  
    }  
}  