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

    private static int screenWidth = 0;
    private static int screenHeight = 0;

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

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}