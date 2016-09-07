package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

import stcdribbble.shituocheng.com.qribbble.Model.LoginUser;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.LoginInActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.MainActivity;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment {

    /**
     * setUp preference
     */
    private Preference preference;
    private SwitchPreference switchPreference;
    private ListPreference timePreference;
    private boolean isLogin;
    private ProgressDialog progressDialog;
    private Preference cacheClearPreference;

    private boolean isNotificationCheck = false;

    private static final int MESSAGE_WHAT_NAME=0;
    private static final int MESSAGE_WHAT_USER_NAME=1;

    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    HandlerThread handlerThread;
    Handler threadHandler;
    Handler uiHandler;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        initPreference();
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data",MODE_PRIVATE);
        String user_name = sharedPreferences.getString("user_name","");
        String name = sharedPreferences.getString("name","");

        Log.d("sved_data",name);

        /**
         * Preference setUp
         */
        isLogin = initData();
        if (!isLogin){
            preference.setTitle(getString(R.string.tap_to_login));
            preference.setSummary(getString(R.string.login_your_account));
        }else {

            //String user_avatar = sharedPreferences.getString("user_avatar","");
            preference.setTitle(user_name);
            preference.setSummary(name);
        }
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                if (isLogin){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Do you confirm to quit your account?");
                    dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().remove("user_name").apply();
                            sharedPreferences.edit().remove("name").apply();
                            sharedPreferences.edit().remove("access_token").apply();
                            sharedPreferences.edit().remove("user_avatar").apply();
                            preference.setTitle(getString(R.string.tap_to_login));
                            preference.setSummary(getString(R.string.login_your_account));

                            isLogin = false;
                        }
                    });
                    dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                }else {
                    String url = "https://dribbble.com/oauth/authorize?client_id=18163f14877c483e440804ad5e0ce54c53b09f41ff87bdce332b3c734f312583&scope=public+write+comment+upload";
                    Intent intent = new Intent(getActivity(), LoginInActivity.class);
                    intent.putExtra("url",url);
                    startActivityForResult(intent,1);

                }
                return true;
            }
        });
        /**
         * Switch Preference setUp
         */

        final SharedPreferences.Editor settingEditor = getActivity().getSharedPreferences("setting", MODE_PRIVATE).edit();

        SharedPreferences getSetting = getActivity().getSharedPreferences("setting", MODE_PRIVATE);
        boolean isChecked = getSetting.getBoolean("notification_setting", false);
        switchPreference.setChecked(isChecked);
        timePreference.setEnabled(isChecked);
        timePreference.setSelectable(isChecked);
        switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                boolean isCheck = ((SwitchPreference)preference).isChecked();
                if (isCheck){
                    isNotificationCheck = true;
                    settingEditor.putBoolean("notification_setting",true);
                    settingEditor.apply();
                    timePreference.setEnabled(true);
                    timePreference.setSelectable(true);
                }else {
                    isNotificationCheck = false;
                    settingEditor.putBoolean("notification_setting",false);
                    settingEditor.apply();
                    timePreference.setEnabled(false);
                    timePreference.setSelectable(false);
                }
                return true;
            }
        });

        /**
         * Clear Cache Preference setUp
         */

        cacheClearPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                threadPool.execute(clearDiskCache());
                uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(clearMemory());
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode==1){

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.success_login));
            progressDialog.setCancelable(false);
            progressDialog.show();
            final String result = data.getBundleExtra("bundle").getString("code");
            //threadPool.execute(fetchUserData(result));
            handlerThread = new HandlerThread("fetchUserData",Thread.NORM_PRIORITY);
            handlerThread.start();
            threadHandler = new Handler(handlerThread.getLooper());


            threadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = fetchUserData(result);

                    Message message = uiHandler.obtainMessage();
                    message.what = MESSAGE_WHAT_NAME;
                    message.obj = bundle;
                    message.sendToTarget();
                }
            });

            uiHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what){
                        case MESSAGE_WHAT_NAME:
                            Bundle bundle = (Bundle)msg.obj;

                            String name = bundle.getString("user_name");
                            String user_name = bundle.getString("name");

                            preference.setTitle(name);
                            preference.setSummary(user_name);
                            progressDialog.dismiss();
                    }
                }
            };

            Log.d("result",result);
        }else {
            Toast.makeText(getActivity(), "failed to login", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initPreference(){
        preference = findPreference(getResources().getString(R.string.login_in));
        switchPreference = (SwitchPreference)findPreference(getString(R.string.notification_setting));
        timePreference = (ListPreference)findPreference(getString(R.string.notification_setting_time));
        cacheClearPreference = findPreference(getString(R.string.clear_cache));
    }

    private boolean initData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data",MODE_PRIVATE);
        String access_token = sharedPreferences.getString("access_token","");
        Log.d("data",access_token);
        /*
        String user_name = sharedPreferences.getString("user_name","");
        String name = sharedPreferences.getString("name","");
        String user_avatar = sharedPreferences.getString("user_avatar","");
        */
        if (access_token.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    private Bundle fetchUserData(final String code){
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        InputStream inputStream;

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

            inputStream.close();
            connection.disconnect();

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
            final String user_name = user_jsonObj.getString("name");

            SharedPreferences.Editor editor = getActivity().getSharedPreferences("user_login_data", MODE_PRIVATE).edit();
            editor.putString("access_token", access_token);
            editor.putString("user_name", login_user_name);
            editor.putString("name", user_name);
            editor.putString("user_avatar", avatar_img_url);
            editor.apply();

            isLogin = true;

            Bundle bundle = new Bundle();
            bundle.putString("user_name",user_name);
            bundle.putString("name",login_user_name);

            Log.d("json", stringBuilder.toString());

            return bundle;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Glide Clear Cache
     * @return
     */
    private Runnable clearDiskCache(){

        return new Runnable() {
            @Override
            public void run() {
                Glide.get(getActivity()).clearDiskCache();
            }
        };
    }

    private Runnable clearMemory(){

        return new Runnable() {
            @Override
            public void run() {
                Glide.get(getActivity()).clearMemory();
            }
        };
    }
}
