package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionInflater;
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

import stcdribbble.shituocheng.com.qribbble.Adapter.DetailViewPagerAdapter;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFragment.ShotsDetailFavoriteFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;

/**
 * Created by shituocheng on 27/07/2016.
 */

public class ShotsDetailActivity extends AppCompatActivity {
    private boolean state = true;
    private FloatingActionButton fab;
    private String imageString;
    private String imageName;
    private int id;
    private Handler workHandler;
    private Handler uiHandler;
    private HandlerThread handlerThread;
    private final static String TAG = "work_handlerThread";

    private final int MESSAGE_WHAT_IMAGE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Explode());
        setContentView(R.layout.activity_shots_detail);

        Intent intent = getIntent();
        imageString = intent.getStringExtra("fullImageUrl");
        id = intent.getIntExtra("id",0);
        if (imageString == null){
            handlerThread = new HandlerThread(TAG, Thread.NORM_PRIORITY);
            handlerThread.start();

            workHandler = new Handler(handlerThread.getLooper());
            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    String api = API.generic_api+"shots/"+String.valueOf(id)+"?access_token="+Access_Token.access_token;
                    Log.d("api", api);
                    try {
                        JSONObject jsonObject = new JSONObject(GetHttpString.getHttpDataString(api, "GET"));
                        JSONObject imageJson = jsonObject.getJSONObject("images");
                        String hidpi_image = imageJson.getString("hidpi");
                        String normal_string = imageJson.getString("normal");
                        Log.d("hidpi", hidpi_image);
                        if (!hidpi_image.isEmpty()){
                            imageString = hidpi_image;
                        }else {
                            imageString = normal_string;
                        }

                        Log.d("imageString", imageString);
                        Message message = uiHandler.obtainMessage();
                        message.what = MESSAGE_WHAT_IMAGE;
                        message.obj = imageString;
                        message.sendToTarget();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            uiHandler = new Handler(getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case MESSAGE_WHAT_IMAGE:
                            loadBackdrop(String.valueOf(msg.obj), true);
                            break;
                        default:

                    }
                }
            };
        }
        imageName = intent.getStringExtra("imageName");
        final boolean isGif = intent.getBooleanExtra("isGif",false);

        Log.d("isGif",String.valueOf(isGif));
        Log.d("imageString", String.valueOf(imageString));

        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.animate();
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_data",MODE_PRIVATE);
        final String access_token = sharedPreferences.getString("access_token","");

        if (access_token.isEmpty()){

        }else {
            pool.execute(checkLikeRun);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (access_token.isEmpty()){
                    //Toast.makeText(ShotsDetailActivity.this,"Please login your Dribble account",Toast.LENGTH_SHORT).show();
                    Snackbar.make(view,"Please login your Dribble account",Snackbar.LENGTH_SHORT).show();
                }else {
                    if (state) {
                        state = false;
                        fab.setImageResource(R.drawable.ic_favorite_white_36dp);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0.0f, 300.0f, 0.0f);
                        animator.setDuration(1000).start();
                       // postShotsLike(access_token,String.valueOf(id),state);
                        pool.execute(postShotsLike((access_token),String.valueOf(id),state));
                        //shotsDetailFavoriteFragment.fetchData(true,id);
                        //shotsDetailFavoriteFragment.update(id);
                        Snackbar.make(view,"You Like the shot!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        state = true;
                        fab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationX", 0.0f, 90.0f, 0.0f);
                        animator.setDuration(1000).start();
                        pool.execute(postShotsLike(access_token,String.valueOf(id),state));
                        //shotsDetailFavoriteFragment.fetchData(true,id);
                        //shotsDetailFavoriteFragment.update(id);
                        Snackbar.make(view,"You dislike the shot!", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        loadBackdrop(imageString, isGif);
        setUpView();
    }

    /**
     * 加载资源模块
     * @param imageString
     * @param isGif
     */

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

    /**
     * post a like
     * @param access_token
     * @param shots_id
     * @param isLiked
     * @return
     */

        private Runnable postShotsLike(final String access_token, final String shots_id, final boolean isLiked){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                InputStream inputStream;
                String api = API.generic_api + "shots/" + String.valueOf(shots_id) + "/like" + "?access_token=" + access_token;
                try {
                    connection = (HttpURLConnection) new URL(api).openConnection();
                    if (!isLiked) {
                        connection.setRequestMethod("POST");
                    } else {
                        connection.setRequestMethod("DELETE");
                    }
                    connection.connect();

                    inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    inputStream.close();
                    connection.disconnect();
                    Log.d("like", stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
            return runnable;
    }

    private void setUpView(){
        final ViewPager viewPager = (ViewPager)findViewById(R.id.shots_detail_viewpager);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_info_outline_white_36dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_favorite_white_36dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_chat_bubble_white_36dp));

        final DetailViewPagerAdapter detailViewPagerAdapter = new DetailViewPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(detailViewPagerAdapter);

        detailViewPagerAdapter.notifyDataSetChanged();

        viewPager.setOffscreenPageLimit(tabLayout.getTabCount() * 10);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        Intent intent = getIntent();
        final int shots_id = intent.getIntExtra("id", 0);
        if (id == R.id.shots_detail_share){

            pool.execute(new Runnable() {
                @Override
                public void run() {

                    HttpURLConnection connection;
                    InputStream inputStream;
                    String api = API.generic_api + "shots/"+shots_id+"?access_token="+ Access_Token.access_token;

                    try {
                        connection = (HttpURLConnection)new URL(api).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        while ((line = bufferedReader.readLine())!=null){
                            stringBuilder.append(line);
                        }

                        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                        final String shared_url = jsonObject.getString("html_url");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,shared_url);
                                startActivity(Intent.createChooser(shareIntent,"Share to"));
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

        }else if (id == R.id.shots_detail_download){

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            } else {
                downloadFile(imageString);
            }

        }else if (id == android.R.id.home){

            TransitionInflater transitionInflater = TransitionInflater.from(this);
            Transition transition = transitionInflater.inflateTransition(R.transition.slide_out);
            getWindow().setExitTransition(transition);
            this.finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.getWindow().setExitTransition(new Explode());
    }

    /**
     *  下载模块
     * @param Url
     */

    public void downloadFile(String Url) {

        Uri downloadUri = Uri.parse(Url);

        if (Url.equals(null)){
            Toast.makeText(this, "Please wait, is Loading", Toast.LENGTH_SHORT).show();
        }else{
            DownloadManager mgr = (DownloadManager)this.getSystemService(Context.DOWNLOAD_SERVICE);


            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(getResources().getString(R.string.app_name))
                    .setDescription("Downloading "+imageName+".jpg")
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,  imageName+".jpg");

            mgr.enqueue(request);
        }
    }

    /**
     * 获取下载权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Get permissions successfully, You can download resources now", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"Failed to get permissions, You can give permission in the Setting", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    ExecutorService pool = Executors.newCachedThreadPool();

    Runnable checkLikeRun = new Runnable(){

        @Override
        public void run() {
            CheckLike(String.valueOf(id));
        }
    };


    /**
     * 载入项目是否check
     * @param shot_id
     */

    private void CheckLike(String shot_id){

        HttpURLConnection connection;
        InputStream inputStream;
        String api = API.generic_api + "shots/"+shot_id+"/like"+"?access_token="+ Access_Token.access_token;

        try {
            connection = (HttpURLConnection)new URL(api).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            int id = jsonObject.getInt("id");

            Log.d("id",String.valueOf(id));

            state = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.setImageResource(R.drawable.ic_favorite_white_36dp);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            state = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                }
            });
        }
    }
}
