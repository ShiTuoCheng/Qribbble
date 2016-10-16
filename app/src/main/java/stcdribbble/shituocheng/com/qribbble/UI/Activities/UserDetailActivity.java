package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.logging.Handler;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment.UserDetailFollowerFragment;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment.UserDetailFollowingFragment;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment.UserDetailworksFragment;
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
    private TextView user_bio_textView;
    private TabLayout user_detail_tabLayout;
    private ViewPager user_detail_viewPager;


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
        getSupportActionBar().setTitle("");

        setUpView();

        /**
         * follow or unfollow functions logic
         */
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
                                ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX", 0.0f, 300.0f, 0.0f);
                                animator.setDuration(1000).start();
                                Snackbar.make(v,"Follow successfully!!",Snackbar.LENGTH_SHORT).show();

                            }else {
                                isFollow = true;
                                follow_button.setText("Follow");
                                threadPool.execute(followUser(name, isFollow));
                                ObjectAnimator animator = ObjectAnimator.ofFloat(v, "rotationX", 0.0f, 90.0f, 0.0f);
                                animator.setDuration(1000).start();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }
        return true;
    }

    private void setUpView(){

        follow_button = (Button)findViewById(R.id.follow_button);
        name_textView = (TextView)findViewById(R.id.name_textView);
        user_name_textView = (TextView)findViewById(R.id.user_name_textView);
        name_avatar_imageView = (CircularNetworkImageView)findViewById(R.id.user_detail_avatar);
        networkImageView = (NetworkImageView)findViewById(R.id.user_detail_backdrop);
        user_bio_textView = (TextView)findViewById(R.id.user_detail_bio);
        user_detail_tabLayout = (TabLayout)findViewById(R.id.tabs);
        user_detail_viewPager = (ViewPager)findViewById(R.id.user_detail_viewpager);


        user_detail_tabLayout.addTab(user_detail_tabLayout.newTab().setText("shots"));
        user_detail_tabLayout.addTab(user_detail_tabLayout.newTab().setText("followers"));
        user_detail_tabLayout.addTab(user_detail_tabLayout.newTab().setText("following"));

        UserDetailPageAdapter userDetailPageAdapter = new UserDetailPageAdapter(getSupportFragmentManager(), user_detail_tabLayout.getTabCount());

        user_detail_viewPager.setAdapter(userDetailPageAdapter);
        userDetailPageAdapter.notifyDataSetChanged();


        user_detail_viewPager.setOffscreenPageLimit(user_detail_tabLayout.getTabCount() * 10);
        user_detail_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        user_detail_viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(user_detail_tabLayout));

        user_detail_tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                user_detail_viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
                            user_name_textView.setText(name);
                            name_textView.setText("@"+user_name);
                            name_avatar_imageView.setImageUrl(user_avatar, imageLoader);
                            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                            networkImageView.setImageUrl(user_avatar, imageLoader);
                            user_bio_textView.setBackgroundColor(Color.TRANSPARENT);
                            user_bio_textView.setTextColor(getResources().getColor(R.color.user_bio_text_color));
                            if (!user_bio.isEmpty()){
                                user_bio_textView.setText(Html.fromHtml(user_bio));
                                user_bio_textView.setTextColor(R.color.user_bio_text_color);
                                user_bio_textView.setMovementMethod(LinkMovementMethod.getInstance());
                            }else {
                                user_bio_textView.setText("(No introducing)");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadPool.shutdown();
    }

    private static class UserDetailPageAdapter extends FragmentStatePagerAdapter{

        int number;

        public UserDetailPageAdapter(FragmentManager fm, int number) {
            super(fm);
            this.number = number;
        }


        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    UserDetailworksFragment userDetailworksFragment = new UserDetailworksFragment();
                    return userDetailworksFragment;
                case 1:
                    UserDetailFollowingFragment userDetailFollowingFragment = new UserDetailFollowingFragment();
                    return userDetailFollowingFragment;
                case 2:
                    UserDetailFollowerFragment userDetailFollowerFragment = new UserDetailFollowerFragment();
                    return userDetailFollowerFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return number;
        }
    }
}
