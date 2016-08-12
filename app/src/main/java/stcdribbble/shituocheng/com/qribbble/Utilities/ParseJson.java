package stcdribbble.shituocheng.com.qribbble.Utilities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shituocheng on 2016/8/10.
 */

public class ParseJson {

    public static JSONObject parseJson(HttpURLConnection connection, InputStream inputStream, String api_url, String method){

        JSONObject jsonObject;

        try {
            connection = (HttpURLConnection)new URL(api_url).openConnection();
            connection.setRequestMethod(method);
            connection.connect();

            inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

            jsonObject = new JSONObject(stringBuilder.toString());

            return jsonObject;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
