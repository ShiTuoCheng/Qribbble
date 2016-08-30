package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.Model.UserModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailFavoriteFragment extends BaseFragment {

    public  RecyclerView favorite_recyclerView;
    public  ArrayList<UserModel> users = new ArrayList<>();

    private MyTask myTask = new MyTask();


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
        myTask.execute(String.valueOf(shots_id));
    }


    /*
    public void fetchData(boolean isFirstLoading, final int shots_id) {
        if (isFirstLoading){
            new Thread(new Runnable() {
                HttpURLConnection connection;
                InputStream inputStream;
                String api = API.generic_api+"shots/"+ String.valueOf(shots_id)+"/likes?access_token="+ Access_Token.access_token;
                @Override
                public void run() {

                    try {
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

                        Log.d("favorite_api",api);

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                        for (int i=0; i<jsonArray.length();i++){
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
                                int num_favorite = users.size();
                                favorite_title_textView.setText(String.valueOf(num_favorite)+" people liked this shot!");
                                UsersAdapter usersAdapter = new UsersAdapter(users);
                                favorite_recyclerView.setAdapter(usersAdapter);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                favorite_recyclerView.setLayoutManager(linearLayoutManager);
                                favorite_recyclerView.setNestedScrollingEnabled(true);

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
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myTask.cancel(false);
    }

    public class MyTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            HttpURLConnection connection;
            InputStream inputStream;
            String shots_id = params[0];
            String api = API.generic_api + "shots/" + shots_id + "/likes?access_token=" + Access_Token.access_token;

            try {
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

                Log.d("favorite_api", api);

                JSONArray jsonArray = new JSONArray(stringBuilder.toString());

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
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int num_favorite = users.size();
            ShotsDetailFavoriteFragment.UsersAdapter usersAdapter = new ShotsDetailFavoriteFragment.UsersAdapter(users);
            favorite_recyclerView.setAdapter(usersAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            favorite_recyclerView.setLayoutManager(linearLayoutManager);
            favorite_recyclerView.setNestedScrollingEnabled(true);
        }
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

            UserModel userModel = userModels.get(position);

            holder.name_textView.setText(userModel.getName());
            holder.avatar_imageView.setImageUrl(userModel.getAvatar(),imageLoader);
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
    }

}
