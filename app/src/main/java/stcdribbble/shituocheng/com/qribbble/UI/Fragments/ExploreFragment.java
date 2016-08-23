package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


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
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private Spinner list_spinner;
    private RecyclerView explore_recyclerView;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        setUpView(v);
        String[] lists = getResources().getStringArray(R.array.list_array);
        ArrayAdapter<String> list_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, lists);
        list_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        list_spinner.setAdapter(list_spinner_adapter);
        list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String[] list = getResources().getStringArray(R.array.list_array);
                Toast.makeText(getActivity(), "你点击的是:"+list[i], Toast.LENGTH_SHORT).show();
                fetchData(list[i]);
                //execute(list[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    @Override
    public void setUpView(View view) {
        list_spinner = (Spinner)view.findViewById(R.id.list_spinner);
        explore_recyclerView = (RecyclerView)view.findViewById(R.id.explore_recyclerView);
    }

    public void fetchData(final String shots_list){

        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            InputStream inputStream;
            String shots_api = API.getSortsShotsApi(shots_list);

            @Override
            public void run() {
                try {
                    connection = (HttpURLConnection) new URL(shots_api).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    Log.d("fetchDataApi", shots_api);

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

                            Log.d("fragment", String.valueOf(shotsModels.size()));

                        }
                    }else {
                        shotsModels.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

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

                            Log.d("fragment", String.valueOf(shotsModels.size()));

                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getActivity());
                            shotsRecyclerViewAdapter.notifyDataSetChanged();
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            explore_recyclerView.setAdapter(shotsRecyclerViewAdapter);
                            explore_recyclerView.setLayoutManager(linearLayoutManager);
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

                                    AnimationUtils.show(v);
                                    startActivity(intent);
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
    }

    public class ExploreRecyclerViewAdapter extends RecyclerView.Adapter<ExploreRecyclerViewAdapter.ViewHolder>{

        private ArrayList<ShotsModel> shotsModels = new ArrayList<>();

        private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

        public class ViewHolder extends RecyclerView.ViewHolder{
            private NetworkImageView each_shots_imageView;
            private TextView each_shots_textView;
            private TextView each_shots_author_textView;
            private TextView each_shots_review_times_textView;
            private TextView each_shots_favorite_times_textView;
            private TextView each_shots_view_times_textView;
            private CircularNetworkImageView each_shots_author_avatar;
            private ImageView isGifImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                each_shots_imageView = (NetworkImageView)itemView.findViewById(R.id.shots_imageView);
                each_shots_textView = (TextView)itemView.findViewById(R.id.shots_title);
                each_shots_author_textView = (TextView)itemView.findViewById(R.id.shots_author_textView);
                each_shots_favorite_times_textView = (TextView)itemView.findViewById(R.id.shots_favorite_times);
                each_shots_review_times_textView = (TextView)itemView.findViewById(R.id.shots_review_times);
                each_shots_view_times_textView = (TextView)itemView.findViewById(R.id.shots_view_times);
                each_shots_author_avatar = (CircularNetworkImageView)itemView.findViewById(R.id.shots_author_avatar);
                isGifImageView = (ImageView)itemView.findViewById(R.id.isGif);
            }

        }

        public ExploreRecyclerViewAdapter(ArrayList<ShotsModel> shotsModels) {
            this.shotsModels = shotsModels;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shots_item,null);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            ShotsModel shotsModel = shotsModels.get(position);

            holder.each_shots_textView.setText(shotsModel.getTitle());

            holder.each_shots_imageView.setImageUrl(shotsModel.getShots_thumbnail_url(),mImageLoader);

            holder.each_shots_view_times_textView.setText(String.valueOf(shotsModel.getShots_view_count()));

            holder.each_shots_favorite_times_textView.setText(String.valueOf(shotsModel.getShots_like_count()));

            holder.each_shots_author_textView.setText(shotsModel.getShots_author_name());

            holder.each_shots_review_times_textView.setText(String.valueOf(shotsModel.getShots_review_count()));

            holder.each_shots_author_avatar.setImageUrl(shotsModel.getShots_author_avatar(),mImageLoader);

            if (shotsModel.isAnimated()){
                holder.isGifImageView.setImageResource(R.drawable.ic_gif_black_24dp);
            }else {


            }
        }

        @Override
        public int getItemCount() {
            return shotsModels.size();
        }
    }
}
