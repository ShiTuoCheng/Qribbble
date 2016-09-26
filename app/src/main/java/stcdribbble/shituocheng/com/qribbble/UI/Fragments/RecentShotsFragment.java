package stcdribbble.shituocheng.com.qribbble.UI.Fragments;



import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentShotsFragment extends BaseFragment {

    private int pages;
    private ArrayList<String> title = new ArrayList<>();
    private ProgressDialog loadingMoreProgressDialog;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final String ARGS_PAGE = "args_page";

    private List<ShotsModel> shotsModels = new ArrayList<>();
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
        final ExecutorService pool = Executors.newCachedThreadPool();
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
                        pool.execute(fechData(true));
                    }
                }, 2000);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // fetchData(true);
                pool.execute(fechData(true));
            }
        });

        pool.execute(fechData(true));
        //fetchData(true);

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

                        //fetchData(false);
                        pool.execute(fechData(false));
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

    public Runnable fechData(final boolean isFirstLoading){
        final HttpURLConnection[] connection = new HttpURLConnection[1];
        final InputStream[] inputStream = new InputStream[1];
        final String api = "https://api.dribbble.com/v1/"+"shots"+"?"+title.get(pages -1 )+"&"+ "access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isFirstLoading){
                    shotsModels.clear();
                    try {
                        connection[0] = (HttpURLConnection)new URL(api).openConnection();
                        connection[0].setRequestMethod("GET");
                        connection[0].connect();

                        inputStream[0] = connection[0].getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream[0]));
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

                        if (getActivity() == null){
                            return;
                        }else {

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

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }else {
                    current_page +=1;
                    HttpURLConnection connection;
                    InputStream inputStream;
                    String api = "https://api.dribbble.com/v1/"+"shots"+"?"+title.get(pages -1 )+"&"+ "access_token=" + "aef92385e190422a5f27496da51e9e95f47a18391b002bf6b1473e9b601e6216";

                    try {
                        connection = (HttpURLConnection)new URL(api+"&page="+String.valueOf(current_page)).openConnection();
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
                        if (getActivity() == null){
                            return;
                        }else {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity());
                                    shotsRecyclerViewAdapter.notifyDataSetChanged();

                                }
                            });

                        }

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
