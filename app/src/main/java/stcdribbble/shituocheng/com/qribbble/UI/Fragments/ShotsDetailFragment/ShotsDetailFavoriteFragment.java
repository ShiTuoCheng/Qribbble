package stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFragment;


import android.content.ClipData;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

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
import stcdribbble.shituocheng.com.qribbble.Adapter.UsersAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.UserModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailFavoriteFragment extends BaseFragment {

    public  RecyclerView favorite_recyclerView;
    public  List<UserModel> users = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();
    private LinearLayoutManager linearLayoutManager;
    private UsersAdapter usersAdapter;
    private int current_page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_shots_detail_favorite, container, false);
        setUpView(v);

        int shots_id = getActivity().getIntent().getIntExtra("id",0);

        update(shots_id);
        return v;
    }


    public void setUpView(View view) {
        favorite_recyclerView = (RecyclerView)view.findViewById(R.id.shots_detail_favorite_recyclerView);
    }

    public void update(int shots_id){
        pool.execute(initData(String.valueOf(shots_id)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!pool.isShutdown()){

            pool.shutdownNow();
        }
    }

    public Runnable initData(final String shots_id){

        return new Runnable() {
            @Override
            public void run() {

                String api = API.generic_api + "shots/" + shots_id + "/likes?access_token=" + Access_Token.access_token;
                Log.d("TEST_API",api);

                try {
                    JSONArray jsonArray = new JSONArray(GetHttpString.getHttpDataString(api, "GET"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        UserModel userModel = new UserModel();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        String user_name = userJson.getString("name");
                        String name = userJson.getString("username");
                        String user_avatar = userJson.getString("avatar_url");
                        userModel.setAvatar(user_avatar);
                        userModel.setUser_name(name);
                        userModel.setName(user_name);
                        users.add(userModel);

                    }

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayoutManager = new LinearLayoutManager(getActivity());

                            favorite_recyclerView.setLayoutManager(linearLayoutManager);

                            favorite_recyclerView.setHasFixedSize(true);

                            usersAdapter = new UsersAdapter(getActivity(),users, favorite_recyclerView);

                            favorite_recyclerView.setAdapter(usersAdapter);
                            usersAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    users.add(null);
                                    usersAdapter.notifyItemInserted(users.size() - 1);
                                    current_page += 1;

                                    pool.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String more_api = API.generic_api + "shots/" + shots_id + "/likes?"+"page="+current_page+"&access_token=" + Access_Token.access_token;
                                            try {
                                                final JSONArray more_jsonArray = new JSONArray(GetHttpString.getHttpDataString(more_api, "GET"));

                                                if (getActivity()==null){
                                                    return;
                                                }
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        users.remove(users.size() - 1);
                                                        usersAdapter.notifyItemRemoved(users.size());
                                                        for (int i = 0; i < more_jsonArray.length(); i++) {
                                                            UserModel userModel = new UserModel();
                                                            JSONObject jsonObject;
                                                            try {
                                                                jsonObject = more_jsonArray.getJSONObject(i);
                                                                JSONObject userJson = jsonObject.getJSONObject("user");
                                                                String user_name = userJson.getString("name");
                                                                String name = userJson.getString("username");
                                                                String user_avatar = userJson.getString("avatar_url");
                                                                userModel.setAvatar(user_avatar);
                                                                userModel.setUser_name(name);
                                                                userModel.setName(user_name);
                                                                users.add(userModel);
                                                                try {
                                                                    usersAdapter.notifyItemInserted(users.size());
                                                                } catch (Exception e) {
                                                                    Log.w(TAG, "notifyItemChanged failure");
                                                                    e.printStackTrace();
                                                                    usersAdapter.notifyDataSetChanged();
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            usersAdapter.setLoaded();
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
    }

}
