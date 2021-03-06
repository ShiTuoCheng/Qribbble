package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Model.LoginUser;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Services.UpdateService;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.ExploreFragment;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.MyFavoriteFragment;
import stcdribbble.shituocheng.com.qribbble.UI.TabFragments.MainTabFragment;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CircularNetworkImageView circularNetworkImageView;
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private TextView login_in_textView;
    private TextView user_name_textView;
    public static boolean isLogin;

    private String user_name_string;

    public static final String CHECK_SERVICE = "checkUpdate";

    private ExecutorService threadPool = Executors.newCachedThreadPool();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();
        login_in_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://dribbble.com/oauth/authorize?client_id=18163f14877c483e440804ad5e0ce54c53b09f41ff87bdce332b3c734f312583&scope=public+write+comment+upload";
                Intent intent = new Intent(MainActivity.this, LoginInActivity.class);
                intent.putExtra("url", url);
                startActivityForResult(intent, 1);
            }
        });

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token", "");
        String user_name = sharedPreferences.getString("user_name", "");
        String name = sharedPreferences.getString("name", "");
        String user_avatar = sharedPreferences.getString("user_avatar", "");
        user_name_string = sharedPreferences.getString("user_name", "");
        if (!access_token.isEmpty()) {
            isLogin = true;
            login_in_textView.setText(user_name);
            user_name_textView.setText(name);
            circularNetworkImageView.setImageUrl(user_avatar, imageLoader);
            login_in_textView.setClickable(!isLogin);
        } else {
            isLogin = false;
        }
        /**
         * service setUp
         */

        threadPool.execute(updateShots());
        boolean shouldStartAlarm = !UpdateService.isServiceAlarmOn(this);
        UpdateService.setServiceAlarm(this, shouldStartAlarm);

        /**
         * Intent 跳转页面配置
         */

        Intent intent = getIntent();
        int mainId = intent.getIntExtra("homeFragment", 111);
        int exploreId = intent.getIntExtra("exploreFragment", 111);

        if (mainId == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new MainTabFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (exploreId == 2) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new ExploreFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }

        setUpShortcuts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token", "");
        String user_name = sharedPreferences.getString("user_name", "");
        String name = sharedPreferences.getString("name", "");
        String user_avatar = sharedPreferences.getString("user_avatar", "");
        if (access_token.isEmpty() && user_avatar.isEmpty() && user_name.isEmpty() && name.isEmpty()) {
            isLogin = false;
            login_in_textView.setText(R.string.login_in);
            user_name_textView.setText("");
            circularNetworkImageView.setImageUrl(null, imageLoader);
            login_in_textView.setClickable(!isLogin);
        } else {
            circularNetworkImageView.setImageUrl(user_avatar, imageLoader);
            login_in_textView.setText(user_name);
            user_name_textView.setText(name);
            login_in_textView.setClickable(false);
        }
        Intent intent = getIntent();
        int mainId = intent.getIntExtra("homeFragment", 0);
        int exploreId = intent.getIntExtra("exploreFragment", 1);

        if (mainId == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new MainTabFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (exploreId == 2) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new ExploreFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        searchView.setQueryHint(getResources().getString(R.string.query_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.main_home) {
            MainTabFragment mainTabFragment = new MainTabFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainTabFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        } else if (id == R.id.explore) {
            ExploreFragment exploreFragment = new ExploreFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, exploreFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.explore));

        } else if (id == R.id.profile) {

            SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");

            if (access_token.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.login_in), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
                intent.putExtra("user_name", user_name_string);
                startActivity(intent);
            }

        } else if (id == R.id.like) {

            SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");

            if (access_token.isEmpty()) {
                item.setCheckable(false);
                item.setChecked(false);
                Toast.makeText(MainActivity.this, getString(R.string.login_in), Toast.LENGTH_SHORT).show();
            } else {

                item.setChecked(true);
                item.setCheckable(true);
                Bundle bundle = new Bundle();
                bundle.putString("my_category", "likes");
                MyFavoriteFragment myFavoriteFragment = new MyFavoriteFragment();
                myFavoriteFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, myFavoriteFragment).setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                getSupportActionBar().setTitle(getResources().getString(R.string.my_favorite));
            }

        } else if (id == R.id.follow) {

            SharedPreferences sharedPreferences = getSharedPreferences("user_login_data", MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");

            if (access_token.isEmpty()) {
                item.setCheckable(false);
                item.setChecked(false);
                Toast.makeText(MainActivity.this, getString(R.string.login_in), Toast.LENGTH_SHORT).show();
            } else {

                item.setChecked(true);
                item.setCheckable(true);
                Bundle bundle = new Bundle();
                bundle.putString("my_category", "following/shots");
                MyFavoriteFragment myFavoriteFragment = new MyFavoriteFragment();
                myFavoriteFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, myFavoriteFragment).setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                getSupportActionBar().setTitle(getResources().getString(R.string.my_following));
            }
        } else if (id == R.id.setting) {


            Intent intent = new Intent(this, SettingActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
        setSupportActionBar(toolbar);

        toolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        login_in_textView = (TextView) headerView.findViewById(R.id.login_in_textView);
        user_name_textView = (TextView) headerView.findViewById(R.id.user_name_textView);
        circularNetworkImageView = (CircularNetworkImageView) headerView.findViewById(R.id.user_login_avatar);
        navigationView.setCheckedItem(R.id.main_home);
        navigationView.setNavigationItemSelectedListener(this);


        MainTabFragment mainTabFragment = new MainTabFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, mainTabFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage(getString(R.string.success_login));
                progressDialog.setCancelable(false);
                progressDialog.show();
                String result = data.getBundleExtra("bundle").getString("code");
                threadPool.execute(fetchUserData(result));
                Log.d("result", result);
            } else {
                Toast.makeText(getApplicationContext(), "failed to login", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Runnable fetchUserData(final String code) {

        return new Runnable() {
            HttpURLConnection connection;
            BufferedReader bufferedReader;
            InputStream inputStream;

            @Override
            public void run() {
                String url = "https://dribbble.com/oauth/token" + "?" + "client_id=18163f14877c483e440804ad5e0ce54c53b09f41ff87bdce332b3c734f312583" + "&" + "client_secret=f7efc3be1a475673a5377116ac9f100454a9bcc4cb805ac29ddf303ac0ac2301" + "&" + "code=" + code;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("POST");
                    connection.connect();

                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }


                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    String access_token = jsonObject.getString("access_token");

                    LoginUser loginUser = new LoginUser();
                    loginUser.setAcess_token(access_token);

                    connection = (HttpURLConnection) new URL("https://api.dribbble.com/v1/user?access_token=" + access_token).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String user_line;
                    StringBuilder user_stringBuilder = new StringBuilder();

                    while ((user_line = bufferedReader.readLine()) != null) {
                        user_stringBuilder.append(user_line);
                    }

                    JSONObject user_jsonObj = new JSONObject(user_stringBuilder.toString());
                    final String avatar_img_url = user_jsonObj.getString("avatar_url");
                    final String login_user_name = user_jsonObj.getString("username");
                    user_name_string = user_jsonObj.getString("username");
                    final String user_name = user_jsonObj.getString("name");

                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("user_login_data", MODE_PRIVATE).edit();
                    editor.putString("access_token", access_token);
                    editor.putString("user_name", login_user_name);
                    editor.putString("name", user_name);
                    editor.putString("user_avatar", avatar_img_url);
                    editor.apply();

                    Log.d("json", stringBuilder.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                            circularNetworkImageView.setImageUrl(avatar_img_url, imageLoader);
                            login_in_textView.setText(login_user_name);
                            user_name_textView.setText(user_name);
                            login_in_textView.setClickable(false);
                            progressDialog.dismiss();
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

    private Runnable updateShots() {

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                InputStream inputStream;
                String api = "https://api.dribbble.com/v1/" + "shots" + "?" + "sort" + "=" + "recent" + "&" + "access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";

                try {
                    connection = (HttpURLConnection) new URL(api).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    JSONObject userJsonObj = jsonObject.getJSONObject("user");
                    String user = (userJsonObj.getString("username"));

                    SharedPreferences preference = getApplicationContext().getSharedPreferences("update", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putString("check", user);

                    editor.apply();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        return updateRunnable;
    }

    private void setUpShortcuts() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
            mainIntent.putExtra("homeFragment", 1);
            mainIntent.setAction(Intent.ACTION_DEFAULT);

            Intent exploreIntent = new Intent(MainActivity.this, MainActivity.class);
            exploreIntent.putExtra("exploreFragment", 2);
            exploreIntent.setAction(Intent.ACTION_DEFAULT);

            ShortcutInfo homeShortcutInfo = new ShortcutInfo.Builder(this, "home")
                    .setShortLabel("Home")
                    .setLongLabel("Home Screen")
                    .setIcon(Icon.createWithResource(this, R.drawable.home_touch))
                    .setIntent(mainIntent)
                    .build();

            ShortcutInfo exploreShortcutInfo = new ShortcutInfo.Builder(this, "explore")
                    .setShortLabel("Explore")
                    .setLongLabel("Explore Screen")
                    .setIcon(Icon.createWithResource(this, R.drawable.explore_touch))
                    .setIntent(exploreIntent)
                    .build();

            shortcutManager.addDynamicShortcuts(Arrays.asList(homeShortcutInfo, exploreShortcutInfo));
        }
    }
}
