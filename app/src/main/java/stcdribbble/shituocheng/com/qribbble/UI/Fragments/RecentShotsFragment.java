package stcdribbble.shituocheng.com.qribbble.UI.Fragments;



import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Animator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentShotsFragment extends BaseFragment {

    private int pages;
    private ArrayList<String> title = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShotsRecyclerViewAdapter shotsRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    public static final String ARGS_PAGE = "args_page";
    private ExecutorService pool = Executors.newCachedThreadPool();

    private List<ShotsModel> shotsModels;
    private Handler handler = new Handler();
    private int current_page = 1;


    public static RecentShotsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARGS_PAGE,page);
        RecentShotsFragment recentShotsFragment = new RecentShotsFragment();
        recentShotsFragment.setArguments(args);
        return recentShotsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pages = getArguments().getInt(ARGS_PAGE);
        title.add("list=debuts");
        title.add("sort=recent");
        title.add("sort=views");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_shots, container, false);
        shotsModels = new ArrayList<>();
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
                        //fetchData(true);
                        pool.execute(loadData());
                    }
                }, 2000);
            }
        });

        pool.execute(loadData());
        //fetchData(true);



        return view;
    }

    @Override
    public void setUpView(View view){

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);

    }

    public Runnable loadData(){
        return new Runnable() {
            @Override
            public void run() {
                final String api = "https://api.dribbble.com/v1/"+"shots"+"?"+title.get(pages -1 )+"&"+ "access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";
                shotsModels.clear();
                Log.d("api", api);

                try {
                    HttpURLConnection connection;
                    InputStream inputStream;
                        connection = (HttpURLConnection)new URL(api).openConnection();
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


                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                    for (int i = 0; i < jsonArray.length(); i++){
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
                        shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                        shotsModel.setShots_like_count(jsonObject.getInt("likes_count"));
                        shotsModel.setShots_review_count(jsonObject.getInt("comments_count"));
                        shotsModel.setShots_view_count(jsonObject.getInt("views_count"));
                        shotsModel.setShots_id(jsonObject.getInt("id"));
                        shotsModel.setAnimated(jsonObject.getBoolean("animated"));
                        shotsModel.setShots_share_url(jsonObject.getString("html_url"));

                        JSONObject userJsonObj = jsonObject.getJSONObject("user");
                        shotsModel.setShots_author_name(userJsonObj.getString("username"));
                        shotsModel.setShots_author_avatar(userJsonObj.getString("avatar_url"));

                        shotsModels.add(shotsModel);

                    }

                    if (getActivity() == null){
                        return;
                    }else {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                swipeRefreshLayout.setRefreshing(false);

                                mRecyclerView.setVisibility(View.VISIBLE);

                                linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());

                                mRecyclerView.setLayoutManager(linearLayoutManager);

                                shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,mRecyclerView);

                                mRecyclerView.setAdapter(shotsRecyclerViewAdapter);
                                shotsRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                    @Override
                                    public void onLoadMore() {
                                        shotsModels.add(null);
                                        shotsRecyclerViewAdapter.notifyItemInserted(shotsModels.size() - 1);
                                        current_page += 1;

                                        pool.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                String more_api = "https://api.dribbble.com/v1/"+"shots"+"?"+title.get(pages -1 )+"&page="+current_page+ "&access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";
                                                final JSONArray more_jsonArray;
                                                try {
                                                    more_jsonArray = new JSONArray(GetHttpString.getHttpDataString(more_api, "GET"));

                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            shotsModels.remove(shotsModels.size() - 1);
                                                            shotsRecyclerViewAdapter.notifyItemRemoved(shotsModels.size());
                                                            for (int i = 0; i < more_jsonArray.length(); i++){
                                                                ShotsModel shotsModel = new ShotsModel();

                                                                JSONObject jsonObject;
                                                                try {
                                                                    jsonObject = more_jsonArray.getJSONObject(i);
                                                                    shotsModel.setTitle(jsonObject.getString("title"));
                                                                    JSONObject imageJsonObj = jsonObject.getJSONObject("images");

                                                                    if (imageJsonObj.getString("hidpi").equals("null")){
                                                                        shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                                                    }else {
                                                                        shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                                                    }
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
                                                                    try {
                                                                         shotsRecyclerViewAdapter.notifyItemInserted(shotsModels.size());
                                                                    } catch (Exception e) {
                                                                        Log.w(TAG, "notifyItemChanged failure");
                                                                        e.printStackTrace();
                                                                         shotsRecyclerViewAdapter.notifyDataSetChanged();
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                                shotsRecyclerViewAdapter.setLoaded();
                                                            }

                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });

                                /**
                                 * RecyclerView Animation Set Up
                                 */

                                shotsRecyclerViewAdapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                    @Override
                                    public void OnItemClick(View v, int position) {
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

                                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    }
                                });
                            }
                        });

                    }

                }catch (JSONException e) {

                    e.printStackTrace();
                }catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
