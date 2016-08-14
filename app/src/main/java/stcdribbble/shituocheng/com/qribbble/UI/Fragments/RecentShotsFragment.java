package stcdribbble.shituocheng.com.qribbble.UI.Fragments;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import java.util.List;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentShotsFragment extends BaseFragment {

    private ProgressDialog loadingMoreProgressDialog;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<ShotsModel> shotsModels = new ArrayList<>();
    private Handler handler = new Handler();
    private int current_page = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_shots, container, false);
        setUpView(view);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchData(true);
                    }
                }, 2000);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData(true);
            }
        });
        fetchData(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingToLast = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager)recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();
                    if (lastItemPosition == (totalItemCount - 1) && isSlidingToLast) {

                        fetchData(false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingToLast = dy > 0;
            }
        });

        return view;
    }

    @Override
    public void setUpView(View view){

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);

    }

    @Override
    public void fetchData(boolean isFirstLoading){

        if (isFirstLoading){
            new Thread(new Runnable() {
                HttpURLConnection connection;
                InputStream inputStream;
                @Override
                public void run() {
                    shotsModels.clear();
                    try {
                        connection = (HttpURLConnection)new URL(API.getRecentShotsAPI()).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null){
                            stringBuilder.append(line);
                        }

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

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
                            shotsModel.setShots_id(jsonObject.getInt("id"));
                            shotsModel.setAnimated(jsonObject.getBoolean("animated"));

                            JSONObject userJsonObj = jsonObject.getJSONObject("user");
                            shotsModel.setShots_author_name(userJsonObj.getString("username"));
                            shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                            shotsModels.add(shotsModel);

                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity().getApplicationContext());
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                shotsRecyclerViewAdapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
                                mRecyclerView.setAdapter(shotsRecyclerViewAdapter);
                                mRecyclerView.setLayoutManager(linearLayoutManager);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                shotsRecyclerViewAdapter.setOnClickListener(new ShotsRecyclerViewAdapter.ClickListener() {
                                    @Override
                                    public void onItemClick(int position, View v) {
                                        Intent intent = new Intent(getActivity(), ShotsDetailActivity.class);
                                        ShotsModel shotsModel = shotsModels.get(position);
                                        String imageUrl = shotsModel.getShots_thumbnail_url();
                                        String fullImageUrl = shotsModel.getShots_full_imageUrl();
                                        String imageName = shotsModel.getTitle();
                                        int id = shotsModel.getShots_id();
                                        boolean isGif = shotsModel.isAnimated();
                                        intent.putExtra("imageName",imageName);
                                        intent.putExtra("imageURL",imageUrl);
                                        intent.putExtra("isGif",isGif);
                                        intent.putExtra("fullImageUrl",fullImageUrl);
                                        intent.putExtra("id",id);

                                        AnimationUtils.show(v);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onLongItemClick(int position, View v) {

                                    }
                                });
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
            }).start();

        }else {

            new Thread(new Runnable() {
                HttpURLConnection connection;
                InputStream inputStream;
                @Override
                public void run() {
                    current_page +=1;
                    try {
                        connection = (HttpURLConnection)new URL(API.getRecentShotsAPI()+"&page="+String.valueOf(current_page)).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null){
                            stringBuilder.append(line);
                        }

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

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

                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity().getApplicationContext());
                                shotsRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }


}
