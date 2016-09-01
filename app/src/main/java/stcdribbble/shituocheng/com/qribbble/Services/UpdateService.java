package stcdribbble.shituocheng.com.qribbble.Services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class UpdateService extends IntentService {

    private static final String TAG="UPDATE_SERVICE";

    private static final int POLL_INTERVAL=1000 * 15;

    public UpdateService(String name) {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = connectivityManager.getBackgroundDataSetting() && connectivityManager.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable)return;

    }

    public String[] updateShots(){

        HttpURLConnection connection;
        InputStream inputStream;
        String api =
    };

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
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
