package com.zr.webview.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.zr.webview.R;
import com.zr.webview.loading.LoadingView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 仿微信带进度条的webview
 */
public class ProgressWebView extends WebView {

    private ProgressBar progressbar;
    private ImageView imageView;
    private LoadingView loadingView;
    private RelativeLayout relativeLayout;

    public ProgressBar getProgressbar() {
        return progressbar;
    }

    public LoadingView getLoadingView() {
        return loadingView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        imageView = new ImageView(context);
        relativeLayout = new RelativeLayout(context);

        XmlPullParser parser = getResources().getXml(R.layout.loading_layout);
        AttributeSet attributes = Xml.asAttributeSet(parser);
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.START_TAG &&
                    type != XmlPullParser.END_DOCUMENT) {
                // Empty
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        loadingView=new LoadingView(context,attributes);
        progressbar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        relativeLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        relativeLayout.addView(loadingView);
        relativeLayout.addView(progressbar);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(300, 300);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        loadingView.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams1=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,10);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        progressbar.setLayoutParams(layoutParams1);

        Drawable drawable = context.getResources().getDrawable(R.drawable.progress_bar_states);
        progressbar.setProgressDrawable(drawable);
        imageView.setImageResource(R.drawable.webview_back);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView);
        addView(relativeLayout);

        // setWebViewClient(new WebViewClient(){});
        setWebChromeClient(new WebChromeClient());
        //是否可以缩放
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
                imageView.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
                imageView.setVisibility(VISIBLE);
            }
            super.onProgressChanged(view, newProgress);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
