package stcdribbble.shituocheng.com.qribbble.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by shituocheng on 2016/10/1.
 */

public class GetHttpString {

    public static String getHttpDataString(String api_url, String requestMethod){
        HttpURLConnection connection;
        InputStream inputStream;
        try {
            connection = (HttpURLConnection)new URL(api_url).openConnection();
            connection.setRequestMethod(requestMethod);
            connection.connect();

            inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
