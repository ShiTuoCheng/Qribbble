package stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFragment;


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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailFavoriteFragment extends BaseFragment {

    public  RecyclerView favorite_recyclerView;
    public  ArrayList<UserModel> users = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();

    private int current_page = 1;


    public ShotsDetailFavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_shots_detail_favorite, container, false);
        setUpView(v);
        int shots_id = getActivity().getIntent().getIntExtra("id",0);
        //fetchData(true, shots_id);
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
                        String user_avatar = userJson.getString("avatar_url");
                        userModel.setAvatar(user_avatar);
                        userModel.setName(user_name);
                        users.add(userModel);

                    }

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final UsersAdapter usersAdapter = new UsersAdapter(users);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            favorite_recyclerView.setLayoutManager(linearLayoutManager);
                            favorite_recyclerView.setAdapter(usersAdapter);
                            favorite_recyclerView.setNestedScrollingEnabled(true);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private Runnable loadMore(final String shots_id,final UsersAdapter usersAdapter){

        return new Runnable() {
            @Override
            public void run() {
                current_page += 1;
                String api = API.generic_api + "shots/" + shots_id + "/likes?"+"page="+String.valueOf(current_page)+"&access_token=" + Access_Token.access_token;

                try {
                    JSONArray jsonArray = new JSONArray(GetHttpString.getHttpDataString(api, "GET"));
                    int start = users.size();
                    int end = start+jsonArray.length();
                    for (int i = start + 1; i <= end; i++) {
                        UserModel userModel = new UserModel();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        String user_name = userJson.getString("name");
                        String name = userJson.getString("user_name");
                        String user_avatar = userJson.getString("avatar_url");
                        userModel.setAvatar(user_avatar);
                        userModel.setName(user_name);
                        userModel.setUser_name(name);
                        users.add(userModel);
                        usersAdapter.notifyItemInserted(users.size());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public  class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{

        public ArrayList<UserModel> userModels = new ArrayList<>();
        public ImageLoader imageLoader = AppController.getInstance().getImageLoader();

        public UsersAdapter(ArrayList<UserModel> userModels) {
            this.userModels = userModels;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            private CircularNetworkImageView avatar_imageView;
            private TextView name_textView;

            public ViewHolder(View itemView) {
                super(itemView);
                avatar_imageView = (CircularNetworkImageView)itemView.findViewById(R.id.shots_detail_favorite_avatar);
                name_textView = (TextView)itemView.findViewById(R.id.shots_detail_favorite_name);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favorite_item,null);

            ViewHolder viewHolder = new ViewHolder(v);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final UserModel userModel = userModels.get(position);

            holder.name_textView.setText(userModel.getName());
            holder.avatar_imageView.setImageUrl(userModel.getAvatar(),imageLoader);

            holder.avatar_imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.openProfile(getActivity(), userModel.getUser_name());
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
    }

}
