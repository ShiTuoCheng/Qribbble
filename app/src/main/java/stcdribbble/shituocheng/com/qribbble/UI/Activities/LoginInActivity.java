package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import stcdribbble.shituocheng.com.qribbble.R;

public class LoginInActivity extends AppCompatActivity {
    private WebView webView;

    public static final int requestCode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);
        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheMaxSize(1);
        webView.getSettings().setJavaScriptEnabled(true);
        /*
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);
        WebSettings ws = webView.getSettings();
        ws.setSaveFormData(false);
        */
        Map<String, String> noCacheHeaders = new HashMap<String, String>(2);
        noCacheHeaders.put("Pragma", "no-cache");
        noCacheHeaders.put("Cache-Control", "no-cache");

        webView.loadUrl(getIntent().getStringExtra("url"),noCacheHeaders);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("url",url);

                view.loadUrl(url);

                if (url.contains("code")){

                    String code = getCodeFromUrl(url);

                    Log.d("redicted_url",code);

                    if (!code.isEmpty()){
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("code",code);
                        intent.putExtra("bundle",bundle);
                        setResult(RESULT_OK,intent);
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(),"Log in failed. Please retry",Toast.LENGTH_SHORT).show();
                    }

                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.clearCache(true);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.setAcceptCookie(false);
    }

    private String getCodeFromUrl(String url) {
        int startIndex = url.indexOf("code=") + "code=".length();
        String code = url.substring(startIndex);
        Log.i("code", "code=" + code);
        return code;
    }

}
