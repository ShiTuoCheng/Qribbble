package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.UserDetailArtworkAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.SearchResultActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

import static android.content.Context.MODE_PRIVATE;
import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

public class MyFavoriteFragment extends BaseFragment {

    private String category_string = null;
    private RecyclerView my_recyclerView;
    private Bundle bundle;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private List<ShotsModel> shotsModels = new ArrayList<>();

    private GridLayoutManager gridLayoutManager;
    private UserDetailArtworkAdapter userDetailArtworkAdapter;
    private ProgressBar progressBar;

    private int current_page = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        category_string = bundle.getString("my_category");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_favorite, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data", MODE_PRIVATE);

        String user_name = sharedPreferences.getString("user_name", "");

        setUpView(v);

        if (Utils.networkConnected(getActivity().getApplicationContext())){

            threadPool.execute(fetchData(user_name, category_string));

        }else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), getResources().getText(R.string.fail_to_connect), Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    @Override
    public void setUpView(View view) {
        my_recyclerView = (RecyclerView) view.findViewById(R.id.favorite_recyclerView);
        progressBar = (ProgressBar)view.findViewById(R.id.favorite_recyclerView_loader);
    }


    private Runnable fetchData(final String name, final String category) {

        return new Runnable() {
            @Override
            public void run() {

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data",MODE_PRIVATE);
                final String access_token = sharedPreferences.getString("access_token","");

                String api;

                if (category.equals("likes")){

                    api = API.generic_api + "users/" + name + "/" + category + "?" +"access_token=" + access_token;
                }else {
                    api = API.generic_api + "user/following/shots?access_token=" + access_token;
                }


                Log.d("api", api);

                try {
                    JSONArray jsonArray = new JSONArray(GetHttpString.getHttpDataString(api, "GET"));

                    if (jsonArray.length() != 0) {

                        if (category.equals("likes")) {
                            for (int i = 0; i < jsonArray.length(); i++) {

                                ShotsModel shotsModel = new ShotsModel();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                JSONObject shotJsonObj = jsonObject.getJSONObject("shot");
                                JSONObject imageJsonObj = shotJsonObj.getJSONObject("images");

                                if (imageJsonObj.getString("hidpi").equals("null")) {
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                } else {
                                    shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                }
                                shotsModel.setShots_like_count(shotJsonObj.getInt("likes_count"));
                                shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                shotsModel.setShots_review_count(shotJsonObj.getInt("comments_count"));
                                shotsModel.setShots_view_count(shotJsonObj.getInt("views_count"));
                                shotsModel.setAnimated(shotJsonObj.getBoolean("animated"));
                                shotsModel.setShots_id(shotJsonObj.getInt("id"));

                                shotsModels.add(shotsModel);

                                Log.w("size_shots_model", String.valueOf(shotsModels.size()));
                            }
                        }else {
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

                        if (getActivity() == null) {
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                progressBar.setVisibility(View.INVISIBLE);

                                my_recyclerView.setVisibility(View.VISIBLE);

                                gridLayoutManager = new GridLayoutManager(getActivity(), 2);

                                my_recyclerView.setLayoutManager(gridLayoutManager);

                                userDetailArtworkAdapter = new UserDetailArtworkAdapter(shotsModels, my_recyclerView);

                                my_recyclerView.setAdapter(userDetailArtworkAdapter);

                                userDetailArtworkAdapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                    @Override
                                    public void OnItemClick(View v, int position) {
                                        Intent intent = new Intent(getActivity(), ShotsDetailActivity.class);
                                        ShotsModel shotsModel = shotsModels.get(position);
                                        String imageUrl = shotsModel.getShots_thumbnail_url();
                                        String imageName = shotsModel.getTitle();
                                        int id = shotsModel.getShots_id();
                                        boolean isGif = shotsModel.isAnimated();
                                        intent.putExtra("imageName",imageName);
                                        intent.putExtra("imageURL",imageUrl);
                                        intent.putExtra("isGif",isGif);
                                        intent.putExtra("id",id);


                                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    }
                                });

                                userDetailArtworkAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                    @Override
                                    public void onLoadMore() {
                                        shotsModels.add(null);
                                        userDetailArtworkAdapter.notifyItemInserted(shotsModels.size()-1);
                                        current_page += 1;
                                        final String load_more_api;

                                        if (category.equals("likes")){

                                            load_more_api = API.generic_api + "users/" + name + "/" + category + "?" +"page="+current_page+"&access_token=" + access_token;
                                        }else {
                                            load_more_api = API.generic_api + "user/following/shots?"+"page="+current_page+"&access_token=" + access_token;

                                        }

                                        Log.d("loadmore", load_more_api);

                                        threadPool.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                final JSONArray more_jsonArray;

                                                try {
                                                    more_jsonArray = new JSONArray(GetHttpString.getHttpDataString(load_more_api, "GET"));

                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            shotsModels.remove(shotsModels.size()-1);
                                                            userDetailArtworkAdapter.notifyItemRemoved(shotsModels.size());
                                                            if (category.equals("likes")) {
                                                                for (int i = 0; i < more_jsonArray.length(); i++) {

                                                                    ShotsModel shotsModel = new ShotsModel();
                                                                    JSONObject jsonObject = null;
                                                                    try {
                                                                        jsonObject = more_jsonArray.getJSONObject(i);
                                                                        JSONObject shotJsonObj = jsonObject.getJSONObject("shot");
                                                                        JSONObject imageJsonObj = shotJsonObj.getJSONObject("images");

                                                                        if (imageJsonObj.getString("hidpi").equals("null")) {
                                                                            shotsModel.setShots_full_imageUrl(imageJsonObj.getString("normal"));
                                                                        } else {
                                                                            shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));
                                                                        }
                                                                        shotsModel.setShots_like_count(shotJsonObj.getInt("likes_count"));
                                                                        shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                                                        shotsModel.setShots_review_count(shotJsonObj.getInt("comments_count"));
                                                                        shotsModel.setShots_view_count(shotJsonObj.getInt("views_count"));
                                                                        shotsModel.setAnimated(shotJsonObj.getBoolean("animated"));
                                                                        shotsModel.setShots_id(shotJsonObj.getInt("id"));

                                                                        shotsModels.add(shotsModel);

                                                                        Log.w("size_shots_model", String.valueOf(shotsModels.size()));
                                                                        try {
                                                                            userDetailArtworkAdapter.notifyItemInserted(shotsModels.size());
                                                                        } catch (Exception e) {
                                                                            Log.w(TAG, "notifyItemChanged failure");
                                                                            e.printStackTrace();
                                                                            userDetailArtworkAdapter.notifyDataSetChanged();
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    userDetailArtworkAdapter.setLoaded();

                                                                }
                                                            }else {
                                                                for (int i = 0; i < more_jsonArray.length(); i++) {

                                                                    ShotsModel shotsModel = new ShotsModel();
                                                                    JSONObject jsonObject = null;
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
                                                                        try {
                                                                            userDetailArtworkAdapter.notifyItemInserted(shotsModels.size());
                                                                        } catch (Exception e) {
                                                                            Log.w(TAG, "notifyItemChanged failure");
                                                                            e.printStackTrace();
                                                                            userDetailArtworkAdapter.notifyDataSetChanged();
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    userDetailArtworkAdapter.setLoaded();
                                                                }

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

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ;
        };
    }
}
