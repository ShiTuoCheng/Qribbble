package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private Spinner list_spinner;
    private RecyclerView explore_recyclerView;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        setUpView(v);
        final FetchDataTask fetchDataTask = new FetchDataTask();
        String[] lists = getResources().getStringArray(R.array.list_array);
        ArrayAdapter<String> list_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, lists);
        list_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        list_spinner.setAdapter(list_spinner_adapter);
        list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String[] list = getResources().getStringArray(R.array.list_array);
                Toast.makeText(getActivity(), "你点击的是:"+list[i], 2000).show();
                fetchData(true);
                fetchDataTask.execute(list[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    @Override
    public void setUpView(View view) {
        list_spinner = (Spinner)view.findViewById(R.id.list_spinner);
        explore_recyclerView = (RecyclerView)view.findViewById(R.id.explore_recyclerView);
    }

    public String fetchData(String shots_list){

        HttpURLConnection connection = null;
        InputStream inputStream;
        String shots_api = API.getSortsShotsApi(shots_list);

        try {
            connection = (HttpURLConnection)new URL(shots_api).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

            inputStream.close();
            connection.disconnect();

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    private class FetchDataTask extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            String shots_list = strings[0];

            String jsonString = fetchData(shots_list);
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++){
                    ShotsModel shotsModel = new ShotsModel();

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    shotsModel.setTitle(jsonObject.getString("title"));
                    JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                    shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                    shotsModel.setShots_like_count(jsonObject.getInt("likes_count"));
                    shotsModel.setShots_review_count(jsonObject.getInt("comments_count"));
                    shotsModel.setShots_view_count(jsonObject.getInt("views_count"));
                    shotsModel.setAnimated(jsonObject.getBoolean("animated"));
                    shotsModel.setShots_id(jsonObject.getInt("id"));

                    JSONObject userJsonObj = jsonObject.getJSONObject("user");
                    shotsModel.setShots_author_name(userJsonObj.getString("username"));
                    shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                    shotsModels.add(shotsModel);

                    Log.d("fragment",String.valueOf(shotsModels.size()));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }
}
