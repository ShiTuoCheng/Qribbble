package stcdribbble.shituocheng.com.qribbble.Services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.MainActivity;

public class UpdateService extends IntentService {

    private static final String TAG="UPDATE_SERVICE";

    private static final int FIVE_MINUTES_POLL_INTERVAL=1000 * 5 * 60; //5 minutes
    private static final int FIFTEEN_MINUTES_POLL_INTERVAL=1000 * 15 * 60; //15 minutes
    private static final int ONE_HOUR_POLL_INTERVAL=1000 * 60 * 60; // 1 hour
    private static final int FIVE_HOUR_POLL_INTERVAL=1000 * 5 * 60 * 60; //5 hours

    private String user;

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = connectivityManager.getBackgroundDataSetting() && connectivityManager.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable)return;

        /**
         * init Setting
         */
        SharedPreferences settingSharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);

        boolean notificationIsOpen = settingSharedPreferences.getBoolean("notification_setting", false);
        Log.w("notificationIsOpen", String.valueOf(notificationIsOpen));

        if (!notificationIsOpen)return;

        Log.w("Service","Service has started");
        user = updateShots();


        SharedPreferences sharedPreferences = getSharedPreferences("update", MODE_PRIVATE);
        String query = sharedPreferences.getString("check",null);

        Log.w("query",query);
        if (user.isEmpty()){
            return;
        }

        if (!query.equals(user)){

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(user+" and other authors post new shots")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.notification_icon)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults=Notification.DEFAULT_SOUND;
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }else{
            return;
        }

    }

    /**
     * update new shots in the backdround.
     * @return
     */
    public String updateShots(){

        HttpURLConnection connection;
        InputStream inputStream;
        String api = "https://api.dribbble.com/v1/"+"shots"+"?"+"sort"+"="+"recent"+"&"+ "access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";

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

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONObject userJsonObj = jsonObject.getJSONObject("user");
            String user =(userJsonObj.getString("username"));

            return user;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), FIVE_MINUTES_POLL_INTERVAL, pendingIntent);
        }else{
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = new Intent(context, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent!=null;
    }
}
