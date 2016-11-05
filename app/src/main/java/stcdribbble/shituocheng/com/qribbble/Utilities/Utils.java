package stcdribbble.shituocheng.com.qribbble.Utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.UserDetailActivity;

/**
 * Created by shituocheng on 22/07/2016.
 */

public class Utils {

    public static void openProfile(Context context, String author_name) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra("user_name", author_name);
        context.startActivity(intent);
    }

    public static void openShotsDetail(Context context, String imageName, String imageUrl, String fullImageUrl, boolean isGif, int id){
        Intent intent = new Intent(context.getApplicationContext(), ShotsDetailActivity.class);
        intent.putExtra("imageName",imageName);
        intent.putExtra("imageURL",imageUrl);
        intent.putExtra("isGif",isGif);
        intent.putExtra("fullImageUrl",fullImageUrl);
        intent.putExtra("id",id);
        context.startActivity(intent);
    }

    public static boolean networkConnected(Context context) {

        if(context != null){
            ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null){
                return networkInfo.isAvailable();
            }
        }
        return false;
    }
}