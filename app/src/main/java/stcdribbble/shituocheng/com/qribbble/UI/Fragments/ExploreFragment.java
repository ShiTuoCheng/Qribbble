package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private Spinner list_spinner;
    private Spinner sort_spinner;
    private Spinner timeframe_spinner;
    private RecyclerView explore_recyclerView;
    private ProgressBar progressBar;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();

    private String sort_string;
    private String list_string;
    private String timeframe_string;

    private int current_page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        setUpView(v);

        //list_spinner_setup
        String[] lists = getResources().getStringArray(R.array.list_array);
        ArrayAdapter<String> list_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, lists);
        list_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        list_spinner.setAdapter(list_spinner_adapter);
        list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String[] list = getResources().getStringArray(R.array.list_array);
                list_string = list[i];
                progressBar.setVisibility(View.VISIBLE);
                explore_recyclerView.setVisibility(View.GONE);
                threadPool.execute(fetchData(sort_string, list_string, timeframe_string, true));
                //execute(list[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //sort_spinner_setup
        String[] sorts = getResources().getStringArray(R.array.sort_array);
        ArrayAdapter<String> sort_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, sorts);
        sort_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        sort_spinner.setAdapter(sort_spinner_adapter);
        sort_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] sort = getResources().getStringArray(R.array.sort_array);
                sort_string = sort[i];
                progressBar.setVisibility(View.VISIBLE);
                explore_recyclerView.setVisibility(View.GONE);
                threadPool.execute(fetchData(list_string, sort_string, timeframe_string, true));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //timeframe_spinner_setup
        String[] timeframes = getResources().getStringArray(R.array.timeframe_array);
        ArrayAdapter<String> timeframe_spinner_adapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_array_list,timeframes);
        timeframe_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        timeframe_spinner.setAdapter(timeframe_spinner_adapter);
        timeframe_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] timeframe = getResources().getStringArray(R.array.timeframe_array);
                timeframe_string = timeframe[i];
                progressBar.setVisibility(View.VISIBLE);
                explore_recyclerView.setVisibility(View.GONE);
                threadPool.execute(fetchData(list_string, sort_string, timeframe_string, true));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //setUp explore_recyclerView

        explore_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingtoLast = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    if (lastItemPosition == (totalItemCount - 1) && isSlidingtoLast){
                        threadPool.execute(fetchData(list_string, sort_string, timeframe_string, false));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingtoLast = dy > 0;
            }
        });

        return v;
    }

    @Override
    public void setUpView(View view) {
        list_spinner = (Spinner)view.findViewById(R.id.list_spinner);
        sort_spinner = (Spinner)view.findViewById(R.id.sort_spinner);
        timeframe_spinner = (Spinner)view.findViewById(R.id.timeframe_spinner);
        progressBar = (ProgressBar)view.findViewById(R.id.explore_progressBar);
        explore_recyclerView = (RecyclerView)view.findViewById(R.id.explore_recyclerView);
    }

    public Runnable fetchData(final String shots_list, final String shots_sort, final String shots_timeframe, final boolean isFirstLoading){

        Runnable runnable = new Runnable() {
            HttpURLConnection connection = null;
            InputStream inputStream;
            String shots_api = API.getSortsShotsApi(shots_list, shots_sort, shots_timeframe);
            @Override
            public void run() {
                if (isFirstLoading){
                    try {
                        connection = (HttpURLConnection) new URL(shots_api).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        Log.e("fetchDataApi", shots_api);

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        inputStream.close();
                        connection.disconnect();

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                        if (shotsModels.size() == 0){
                            for (int i = 0; i < jsonArray.length(); i++) {

                                ShotsModel shotsModel = new ShotsModel();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                shotsModel.setTitle(jsonObject.getString("title"));
                                JSONObject imageJsonObj = jsonObject.getJSONObject("images");

                                if (imageJsonObj.getString("hidpi").equals("null")){
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                }else {
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                }
                                shotsModel.setShots_like_count(jsonObject.getInt("likes_count"));
                                shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                shotsModel.setShots_review_count(jsonObject.getInt("comments_count"));
                                shotsModel.setShots_view_count(jsonObject.getInt("views_count"));
                                shotsModel.setAnimated(jsonObject.getBoolean("animated"));
                                shotsModel.setShots_id(jsonObject.getInt("id"));

                                JSONObject userJsonObj = jsonObject.getJSONObject("user");
                                shotsModel.setShots_author_name(userJsonObj.getString("username"));
                                shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                                shotsModels.add(shotsModel);

                                Log.d("fragment", String.valueOf(shotsModels.size()));

                            }
                        }else {
                            shotsModels.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {

                                ShotsModel shotsModel = new ShotsModel();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                shotsModel.setTitle(jsonObject.getString("title"));
                                JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                                if (imageJsonObj.getString("hidpi").equals("null")){
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                }else {
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                }
                                shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                shotsModel.setShots_like_count(jsonObject.getInt("likes_count"));
                                shotsModel.setShots_review_count(jsonObject.getInt("comments_count"));
                                shotsModel.setShots_view_count(jsonObject.getInt("views_count"));
                                shotsModel.setAnimated(jsonObject.getBoolean("animated"));
                                shotsModel.setShots_id(jsonObject.getInt("id"));

                                JSONObject userJsonObj = jsonObject.getJSONObject("user");
                                shotsModel.setShots_author_name(userJsonObj.getString("username"));
                                shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                                shotsModels.add(shotsModel);

                                Log.d("fragment", String.valueOf(shotsModels.size()));

                            }
                        }

                        if (getActivity() == null){
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity());
                                shotsRecyclerViewAdapter.notifyDataSetChanged();
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                explore_recyclerView.setAdapter(shotsRecyclerViewAdapter);
                                explore_recyclerView.setLayoutManager(linearLayoutManager);
                                explore_recyclerView.setVisibility(View.VISIBLE);
                                shotsRecyclerViewAdapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                    @Override
                                    public void OnItemClick(View v, int position) {
                                        Intent intent = new Intent(getActivity(), ShotsDetailActivity.class);
                                        ShotsModel shotsModel = shotsModels.get(position);
                                        String imageUrl = shotsModel.getShots_thumbnail_url();
                                        String fullImageUrl = shotsModel.getShots_full_imageUrl();

                                        Log.d("fullIamgeUrl", fullImageUrl);
                                        String imageName = shotsModel.getTitle();
                                        int id = shotsModel.getShots_id();
                                        boolean isGif = shotsModel.isAnimated();
                                        intent.putExtra("imageName",imageName);
                                        intent.putExtra("imageURL",imageUrl);
                                        intent.putExtra("isGif",isGif);
                                        intent.putExtra("fullImageUrl",fullImageUrl);
                                        intent.putExtra("id",id);

                                        AnimationUtils.show(v);
                                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    }
                                });
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    current_page +=1;
                    String loadMore = API.generic_api+"shots"+"?"+"list="+shots_list+"&"+"sort="+shots_sort+"&"+"timeframe="+shots_timeframe+"&page="+current_page+"&access_token="+ Access_Token.access_token;
                    try {
                        connection = (HttpURLConnection) new URL(loadMore).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        Log.e("loadMore", loadMore);

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        inputStream.close();
                        connection.disconnect();

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                            for (int i = 0; i < jsonArray.length(); i++) {

                                ShotsModel shotsModel = new ShotsModel();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                shotsModel.setTitle(jsonObject.getString("title"));
                                JSONObject imageJsonObj = jsonObject.getJSONObject("images");

                                if (imageJsonObj.getString("hidpi").equals("null")){
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                }else {
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                }
                                shotsModel.setShots_like_count(jsonObject.getInt("likes_count"));
                                shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                shotsModel.setShots_review_count(jsonObject.getInt("comments_count"));
                                shotsModel.setShots_view_count(jsonObject.getInt("views_count"));
                                shotsModel.setAnimated(jsonObject.getBoolean("animated"));
                                shotsModel.setShots_id(jsonObject.getInt("id"));

                                JSONObject userJsonObj = jsonObject.getJSONObject("user");
                                shotsModel.setShots_author_name(userJsonObj.getString("username"));
                                shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                                shotsModels.add(shotsModel);

                                Log.d("fragment", String.valueOf(shotsModels.size()));

                            }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity());
                                shotsRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        return runnable;
    }


}
