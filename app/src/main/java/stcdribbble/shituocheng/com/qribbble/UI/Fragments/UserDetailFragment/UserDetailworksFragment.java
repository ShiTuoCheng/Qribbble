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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
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

    }

    public Runnable fetchData(){
        Intent intent = getActivity().getIntent();
        final String name = intent.getStringExtra("user_name");
        final String api= API.generic_api+"/users/"+name+"/shots?access_token="+ Access_Token.access_token;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    try {
                        JSONArray jsonArray = new JSONArray(GetHttpString.getHttpDataString(api, "GET"));

                        for (int i = 0; i < jsonArray.length(); i++){
                            ShotsModel shotsModel = new ShotsModel();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            shotsModel.setShots_id(jsonObject.getInt("id"));

                            JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                            shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                            shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));

                            shotsModels.add(shotsModel);

                            Log.w("size_shots_model", String.valueOf(shotsModels.size()));

                        }

                        if (getActivity() == null){
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

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

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        };

        return runnable;

    }

    private class UserDetailArtworkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ShotsModel> shotsModels = new ArrayList<>();
        private final int VIEW_TYPE_ITEM = 1;
        private final int VIEW_TYPE_PROGRESSBAR = 0;
        private boolean loading;
        private OnLoadMoreListener onLoadMoreListener;
        private int lastVisibleItem, totalItemCount;
        private int visibleThreshold = 5;
        private OnRecyclerViewOnClickListener mListener;

        public class ShotsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private NetworkImageView networkImageView;
            private OnRecyclerViewOnClickListener listener;

            public ShotsViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
                super(itemView);
                networkImageView = (NetworkImageView)itemView.findViewById(R.id.user_detail_artwork);
                this.listener = listener;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.OnItemClick(v,getLayoutPosition());
                }
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder{

            private ProgressBar progressBar;

            public ProgressViewHolder(View itemView) {
                super(itemView);
                progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar1);
            }
        }

        public UserDetailArtworkAdapter(List<ShotsModel> shotsModels, RecyclerView recyclerView) {
            this.shotsModels = shotsModels;
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager){
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        totalItemCount = gridLayoutManager.getItemCount();
                        lastVisibleItem = gridLayoutManager
                                .findLastVisibleItemPosition();
                        if (!loading
                                && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            // End has been reached
                            // Do something
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return shotsModels.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_PROGRESSBAR;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_TYPE_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_user_detail_artwork, parent, false);

                vh = new ShotsViewHolder(v, mListener);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.progress_item, parent, false);

                vh = new ProgressViewHolder(v);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ShotsViewHolder){

                ShotsModel shotsModel = shotsModels.get(position);
                ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                ((ShotsViewHolder)holder).networkImageView.setImageUrl(shotsModel.getShots_thumbnail_url(), imageLoader);
            }else {
                ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
            }
        }

        public void setLoaded(){
            loading = false;
        }

        @Override
        public int getItemCount() {
            return shotsModels.size();
        }

        public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
            this.onLoadMoreListener = onLoadMoreListener;
        }

        public void setItemClickListener(OnRecyclerViewOnClickListener listener){
            this.mListener = listener;
        }
    }
}
