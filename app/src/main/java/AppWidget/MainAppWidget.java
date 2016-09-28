package appWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;

/**
 * Implementation of App Widget functionality.
 */
public class MainAppWidget extends AppWidgetProvider {

    void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        ComponentName componentName = new ComponentName(context, MainAppWidget.class);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Intent intent = new Intent(context, MainAppWidget.class);
        new FetchFreshData(appWidgetManager, views, componentName).execute();

        // Instruct the widget manager to update the widget
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ComponentName componentName = new ComponentName(context, MainAppWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        new FetchFreshData(appWidgetManager, views, componentName).execute();

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private class FetchFreshData extends AsyncTask<Void, Void, String>{

        private AppWidgetManager appWidgetManager;
        private ComponentName componentName;
        private RemoteViews remoteViews;

        public FetchFreshData(AppWidgetManager appWidgetManager, RemoteViews remoteViews, ComponentName componentName) {
            this.appWidgetManager = appWidgetManager;
            this.remoteViews = remoteViews;
            this.componentName = componentName;
        }

        @Override
        protected String doInBackground(Void... params) {
            String stream;
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

                stream = stringBuilder.toString();

                return stream;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String stream) {
            super.onPostExecute(stream);

            if (stream != null){

                try {

                    JSONArray jsonArray = new JSONArray(stream);
                    final ShotsModel shotsModel = new ShotsModel();
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    shotsModel.setTitle(jsonObject.getString("title"));
                    JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                    shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));


                    JSONObject userJsonObj = jsonObject.getJSONObject("user");
                    shotsModel.setShots_author_name(userJsonObj.getString("username"));
                    shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                    remoteViews.setTextViewText(R.id.appWidget_title, shotsModel.getTitle());
                    remoteViews.setTextViewText(R.id.appWidget_author_textView, shotsModel.getShots_author_name());

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }

    }
}

