package com.cliqz.browser.main;

import android.util.Log;

import com.cliqz.browser.webview.SearchWebView;
import com.cliqz.utils.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import acr.browser.lightning.utils.Utils;

/**
 * @author Stefano Pacifici
 * @date 2016/07/26
 */
final class YTPageFetcher {

    private static final String TAG = YTPageFetcher.class.getSimpleName();
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; " +
            "en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    private final SearchWebView searchWebView;

    private YTPageFetcher(SearchWebView searchWebView) {
        this.searchWebView = searchWebView;
    }

    static void asyncGetYoutubeVideoUrls(SearchWebView searchWebView, String videoUrl) {
        try {
            final URL url = new URL(videoUrl);
            new YTPageFetcher(searchWebView).fetchVideoPage(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Can't fetch " + videoUrl, e);
        }
    }

    private void fetchVideoPage(final URL url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.addRequestProperty("User-Agent", USER_AGENT);
                    if (connection.getResponseCode() == 200) {
                        final String response = Utils.readResponse(connection.getInputStream());
                        callScriptWithResponse(StringUtils.escapeHTML(response));
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void callScriptWithResponse(final String escapedResponse) {
        final String javascript =
                String.format(Locale.US, "ytdownloader.findVideoLinks(\"%s\");", escapedResponse);
        searchWebView.post(new Runnable() {
            @Override
            public void run() {
                searchWebView.evaluateJavascript(javascript, null);
            }
        });
    }

}
