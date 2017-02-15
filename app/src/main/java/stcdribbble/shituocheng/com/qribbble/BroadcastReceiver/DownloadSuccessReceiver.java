package stcdribbble.shituocheng.com.qribbble.BroadcastReceiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import stcdribbble.shituocheng.com.qribbble.R;

/**
 * Created by shituocheng on 2016/10/16.
 */

public class DownloadSuccessReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
            downloadSuccess(context);
        }
    }

    private void downloadSuccess(Context context){

        Toast.makeText(context, R.string.download_success, Toast.LENGTH_SHORT).show();
    }
}
