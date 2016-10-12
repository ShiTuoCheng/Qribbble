package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.UsersAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.UserModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;

import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailFollowerFragment extends BaseFragment {


    private RecyclerView user_detail_following_recyclerView;
    private List<UserModel> userModels = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();
    private LinearLayoutManager linearLayoutManager;
    private UsersAdapter usersAdapter;
    private int current_page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_detail_following, container, false);
        setUpView(v);
        pool.execute(fetchData());
        return v;
    }

    @Override
    public void setUpView(View view){

        user_detail_following_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_following_recyclerView);

    }

    @Override
    public Runnable fetchData(){

        return new Runnable() {
            @Override
            public void run() {
                Intent intent = getActivity().getIntent();
                final String name = intent.getStringExtra("user_name");
                final String api= API.generic_api+"/users/"+name+"/following?access_token="+ Access_Token.access_token;

                Log.d("follow_api", api );

                try {
                    JSONArray jsonArray = new JSONArray(GetHttpString.getHttpDataString(api, "GET"));

                    for (int i = 0; i < jsonArray.length(); i++) {

                        UserModel userModel = new UserModel();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject userJson = jsonObject.getJSONObject("followee");
                        String user_name = userJson.getString("name");
                        String username = userJson.getString("username");
                        String user_avatar = userJson.getString("avatar_url");
                        userModel.setAvatar(user_avatar);
                        userModel.setUser_name(username);
                        userModel.setName(user_name);
                        userModels.add(userModel);

                    }

                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            linearLayoutManager = new LinearLayoutManager(getActivity());

                            user_detail_following_recyclerView.setLayoutManager(linearLayoutManager);

                            user_detail_following_recyclerView.setHasFixedSize(true);

                            usersAdapter = new UsersAdapter(getActivity(),userModels, user_detail_following_recyclerView);

                            user_detail_following_recyclerView.setAdapter(usersAdapter);

                            usersAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    userModels.add(null);
                                    usersAdapter.notifyItemInserted(userModels.size() - 1);
                                    current_page += 1;

                                    pool.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String more_api = API.generic_api+"/users/"+name+"/following?"+"page="+current_page+"&access_token="+ Access_Token.access_token;

                                            try {
                                                final JSONArray moreJson  = new JSONArray(GetHttpString.getHttpDataString(more_api, "GET"));

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        userModels.remove(userModels.size() - 1);
                                                        usersAdapter.notifyItemRemoved(userModels.size());

                                                        for (int i = 0; i < moreJson.length(); i++) {

                                                            UserModel userModel = new UserModel();
                                                            JSONObject jsonObject = null;
                                                            try {
                                                                jsonObject = moreJson.getJSONObject(i);
                                                                JSONObject userJson = jsonObject.getJSONObject("followee");
                                                                String user_name = userJson.getString("name");
                                                                String username = userJson.getString("username");
                                                                String user_avatar = userJson.getString("avatar_url");
                                                                userModel.setAvatar(user_avatar);
                                                                userModel.setUser_name(username);
                                                                userModel.setName(user_name);
                                                                userModels.add(userModel);
                                                                try {
                                                                    usersAdapter.notifyItemInserted(userModels.size());
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
