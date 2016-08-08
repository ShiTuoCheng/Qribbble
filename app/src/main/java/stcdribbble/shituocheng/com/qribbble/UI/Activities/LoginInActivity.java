package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import stcdribbble.shituocheng.com.qribbble.R;

public class LoginInActivity extends AppCompatActivity {
    private WebView webView;

    public static final int requestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);
        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(getIntent().getStringExtra("url"));
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
        });
    }

    private String getCodeFromUrl(String url) {
        int startIndex = url.indexOf("code=") + "code=".length();
        String code = url.substring(startIndex);
        Log.i("code", "code=" + code);
        return code;
    }
}
