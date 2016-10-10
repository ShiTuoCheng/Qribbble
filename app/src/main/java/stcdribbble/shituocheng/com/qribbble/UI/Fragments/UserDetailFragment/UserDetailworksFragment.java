package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Adapter.UserDetailArtworkAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailworksFragment extends Fragment {

    private RecyclerView user_detail_works_recyclerView;
    private List<ShotsModel> shotsModels = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();
    private GridLayoutManager gridLayoutManager;
    private UserDetailArtworkAdapter userDetailArtworkAdapter;
    private TextView nothing_textView;
    private OnRecyclerViewOnClickListener mListener;
    private int current_page = 1;


    public UserDetailworksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_detailworks, container, false);
        Log.d("test", "test");
        setUpView(v);
        /*
        if (intent !=null){
            String name = intent.getStringExtra("user_name");
            Log.d("name_", name);

            pool.execute(fetchData(name, false));
        }
        */

        pool.execute(fetchData());
        return v;
    }

    public void setUpView(View view){

        user_detail_works_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_works_recyclerView);
        nothing_textView = (TextView)view.findViewById(R.id.nothing_textView);

    }

    public Runnable fetchData(){
        Intent intent = getActivity().getIntent();
        final String name = intent.getStringExtra("user_name");
        final String api= API.generic_api+"/users/"+name+"/shots?access_token="+ Access_Token.access_token;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    try {
                        HttpURLConnection connection;
                        InputStream inputStream;
                        connection = (HttpURLConnection) new URL(api).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                        Log.d("json_size", String.valueOf(jsonArray.length()));

                        if (jsonArray.length() != 0){

                            for (int i = 0; i < jsonArray.length(); i++){
                                ShotsModel shotsModel = new ShotsModel();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
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

                                shotsModels.add(shotsModel);

                                Log.w("size_shots_model", String.valueOf(shotsModels.size()));

                            }

                            if (getActivity() == null){
                                return;
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    user_detail_works_recyclerView.setVisibility(View.VISIBLE);

                                    nothing_textView.setVisibility(View.GONE);

                                    gridLayoutManager = new GridLayoutManager(getActivity(), 2);

                                    user_detail_works_recyclerView.setLayoutManager(gridLayoutManager);

                                    userDetailArtworkAdapter = new UserDetailArtworkAdapter(shotsModels, user_detail_works_recyclerView);

                                    user_detail_works_recyclerView.setAdapter(userDetailArtworkAdapter);

                                    userDetailArtworkAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                        @Override
                                        public void onLoadMore() {

                                            shotsModels.add(null);
                                            userDetailArtworkAdapter.notifyItemInserted(shotsModels.size() - 1);
                                            current_page += 1;

                                            pool.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String more_api= API.generic_api+"/users/"+name+"/shots?"+"page="+String.valueOf(current_page)+"&access_token="+ Access_Token.access_token;
                                                    Log.d("user_load_more",more_api);
                                                    try {
                                                        final JSONArray more_jsonArray = new JSONArray(GetHttpString.getHttpDataString(more_api,"GET"));
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                shotsModels.remove(shotsModels.size() - 1);
                                                                userDetailArtworkAdapter.notifyItemRemoved(shotsModels.size());
                                                                for (int i = 0; i < more_jsonArray.length(); i++){
                                                                    ShotsModel shotsModel = new ShotsModel();
                                                                    JSONObject jsonObject = null;
                                                                    try {
                                                                        jsonObject = more_jsonArray.getJSONObject(i);

                                                                        shotsModel.setShots_id(jsonObject.getInt("id"));

                                                                        JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                                                                        shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                                                                        shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));

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
                                                            }
                                                        });
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    userDetailArtworkAdapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
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
                        }else {

                        }


                    }catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };

        return runnable;

    }


}
