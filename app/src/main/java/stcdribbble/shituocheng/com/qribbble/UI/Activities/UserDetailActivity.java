package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;

public class UserDetailActivity extends AppCompatActivity {

    private Button follow_button;
    private TextView name_textView;
    private TextView user_name_textView;
    private CircularNetworkImageView name_avatar_imageView;
    private NetworkImageView networkImageView;
    private WebView user_bio_webView;


    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private boolean isFollow = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpView();
        final String user_name = initData();
        Intent intent = getIntent();
        if (intent != null){
            final String name = intent.getStringExtra("user_name");
            Log.d("name", name);
            //Log.d("user_name", user_name);

            if (user_name != null){
                if (name.equals(user_name)){
                    follow_button.setText("My Profile");
                    follow_button.setEnabled(false);
                }else {
                    threadPool.execute(isFollowUser(name));
                    follow_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isFollow){
                                isFollow = false;
                                follow_button.setText("UnFollow");
                                threadPool.execute(followUser(name, isFollow));
                                Snackbar.make(v,"Follow successfully!!",Snackbar.LENGTH_SHORT).show();

                            }else {
                                isFollow = true;
                                follow_button.setText("Follow");
                                threadPool.execute(followUser(name, isFollow));
                                Snackbar.make(v,"UnFollow successfully!!",Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }else {
                threadPool.execute(isFollowUser(name));
                follow_button.setText("Follow");
                follow_button.setEnabled(true);
                follow_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(v,"Please login your Dribble account",Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            threadPool.execute(fetchData(name));
        }
    }

    private void setUpView(){
        follow_button = (Button)findViewById(R.id.follow_button);
        name_textView = (TextView)findViewById(R.id.name_textView);
        user_name_textView = (TextView)findViewById(R.id.user_name_textView);
        name_avatar_imageView = (CircularNetworkImageView)findViewById(R.id.user_detail_avatar);
        networkImageView = (NetworkImageView)findViewById(R.id.user_detail_backdrop);
        user_bio_webView = (WebView)findViewById(R.id.user_detail_bio);
    }

    private String initData(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
        String user_name = sharedPreferences.getString("user_name",null);
        return user_name;
    }

    private Runnable fetchData(final String user_name){
        return new Runnable() {
            HttpURLConnection connection;
            InputStream inputStream;
            String api = API.generic_api+"users/"+user_name+"?access_token="+ Access_Token.access_token;
            @Override
            public void run() {
                try {
                    connection = (HttpURLConnection)new URL(api).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    final String user_name = jsonObject.getString("username");
                    final String name = jsonObject.getString("name");
                    final String user_avatar = jsonObject.getString("avatar_url");
                    final String user_bio = jsonObject.getString("bio");

                    Log.d("user_name", user_name);
                    Log.d("name",name);
                    Log.d("user_avatar",user_avatar);

                    //webView Loading SetUp

                    String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/BMitra.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;color: #fff; }a{color:#ff4091; text-decoration:none}</style></head><body>";
                    String pas = "</body></html>";

                    final String WebView_Data = pish + user_bio + pas;

                    final String no_introduce = pish + "(no introducing)" + pas;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            user_name_textView.setText(user_name);
                            name_textView.setText(name);
                            name_avatar_imageView.setImageUrl(user_avatar, imageLoader);
                            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                            networkImageView.setImageUrl(user_avatar, imageLoader);
                            user_bio_webView.setBackgroundColor(Color.TRANSPARENT);
                            user_bio_webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
                            if (!user_bio.isEmpty()){
                                user_bio_webView.loadData(WebView_Data, "text/html;charset=UTF-8",null);
                            }else {
                                user_bio_webView.loadData(no_introduce, "text/html;charset=UTF-8",null);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Runnable isFollowUser(final String user_name){

        return new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                SharedPreferences sharedPreferences = getSharedPreferences("user_login_data",MODE_PRIVATE);
                String access_token = sharedPreferences.getString("access_token",null);
                String api = API.generic_api + "user/following/" + user_name + "?access_token=" + access_token;

                try {
                    connection = (HttpURLConnection)new URL(api).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    final int code = connection.getResponseCode();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 204){
                                follow_button.setEnabled(true);
                                follow_button.setText("Unfollow");
                                isFollow = false;
                            }else if (code == 404){
                                follow_button.setEnabled(true);
                                follow_button.setText("follow");
                                isFollow = true;
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        };
    }

    private Runnable followUser(final String user_name, final boolean isFollow){
        return new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                SharedPreferences sharedPreferences = getSharedPreferences("user_login_data",MODE_PRIVATE);
                String access_token = sharedPreferences.getString("access_token",null);
                Log.d("access",access_token);
                String api = API.generic_api + "users/"+user_name+"/follow?access_token="+access_token;
                Log.d("api",api);

                try {
                    connection = (HttpURLConnection)new URL(api).openConnection();

                    if (!isFollow){
                        connection.setRequestMethod("PUT");
                    }else {
                        connection.setRequestMethod("DELETE");
                    }
                    connection.connect();

                    int code = connection.getResponseCode();
                    Log.d("code", String.valueOf(code));

                    connection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
