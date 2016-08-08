package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import stcdribbble.shituocheng.com.qribbble.Adapter.DetailViewPagerAdapter;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;

/**
 * Created by shituocheng on 27/07/2016.
 */

public class ShotsDetailActivity extends AppCompatActivity {
    private boolean state = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shots_detail);

        Intent intent = getIntent();
        final String imageString = intent.getStringExtra("fullImageUrl");
        final boolean isGif = intent.getBooleanExtra("isGif",false);
        final int id = intent.getIntExtra("id",0);

        Log.d("isGif",String.valueOf(isGif));

        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(" ");

        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = getSharedPreferences("user_login_data",MODE_PRIVATE);
                String access_token = sharedPreferences.getString("access_token","");

                if (access_token.isEmpty()){
                    //Toast.makeText(ShotsDetailActivity.this,"Please login your Dribble account",Toast.LENGTH_SHORT).show();
                    Snackbar.make(view,"Please login your Dribble account",Snackbar.LENGTH_SHORT).show();
                }else {
                    if (state) {
                        state = false;
                        fab.setImageResource(R.drawable.ic_favorite_white_36dp);
                        postShotsLike(access_token,String.valueOf(id),state);
                        Snackbar.make(view,"You Like the shot!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        state = true;
                        fab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                        postShotsLike(access_token,String.valueOf(id),state);
                        Snackbar.make(view,"You dislike the shot!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        loadBackdrop(imageString, isGif);

        setUpView();
    }

    private void loadBackdrop(String imageString, boolean isGif) {

        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        if (isGif){
            Glide.with(this).load(imageString).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(imageView);
        }else {
            Glide.with(this).load(imageString).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(imageView);
        }
    }

    private void postShotsLike(final String access_token, final String shots_id, final boolean isLiked){

        new Thread(new Runnable() {
            HttpURLConnection connection;
            InputStream inputStream;
            String api = API.generic_api+"shots/"+String.valueOf(shots_id)+"/like"+ "?access_token="+access_token;
            @Override
            public void run() {

                try {
                    connection = (HttpURLConnection)new URL(api).openConnection();
                    if (!isLiked){
                        connection.setRequestMethod("POST");
                    }else {
                        connection.setRequestMethod("DELETE");
                    }
                    connection.connect();

                    inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine())!=null){
                        stringBuilder.append(line);
                    }

                    inputStream.close();
                    connection.disconnect();
                    Log.d("like",stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    private void setUpView(){
        final ViewPager viewPager = (ViewPager)findViewById(R.id.shots_detail_viewpager);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_info_outline_white_36dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_favorite_white_36dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_chat_bubble_white_36dp));

        DetailViewPagerAdapter detailViewPagerAdapter = new DetailViewPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(detailViewPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.shots_detail_share){

        }
        return true;
    }
}
