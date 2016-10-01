package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.GetHttpString;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailworksFragment extends Fragment {

    private RecyclerView user_detail_works_recyclerView;
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();
    private OnRecyclerViewOnClickListener mListener;


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

        pool.execute(fetchData(true));
        return v;
    }

    public void setUpView(View view){

        user_detail_works_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_works_recyclerView);

    }

    public Runnable fetchData(final boolean isFirstLoading){
        Intent intent = getActivity().getIntent();
        final String name = intent.getStringExtra("user_name");
        final String api= API.generic_api+"/users/"+name+"/shots?access_token="+ Access_Token.access_token;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isFirstLoading){
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

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                UserDetailArtworkAdapter userDetailArtworkAdapter = new UserDetailArtworkAdapter(shotsModels);
                                userDetailArtworkAdapter.notifyDataSetChanged();
                                user_detail_works_recyclerView.setAdapter(userDetailArtworkAdapter);
                                GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
                                user_detail_works_recyclerView.setLayoutManager(gridLayoutManager);
                                Animation animation = android.view.animation.AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.anim_item);
                                LayoutAnimationController controller = new LayoutAnimationController(animation);
                                controller.setDelay(0.5f);
                                controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
                                user_detail_works_recyclerView.setLayoutAnimation(controller);

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

                }else {

                }
            }
        };

        return runnable;

    }

    private class UserDetailArtworkAdapter extends RecyclerView.Adapter<UserDetailArtworkAdapter.ViewHolder>{

        private ArrayList<ShotsModel> shotsModels = new ArrayList<>();
        private OnRecyclerViewOnClickListener mListener;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private NetworkImageView networkImageView;
            private OnRecyclerViewOnClickListener listener;

            public ViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
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

        public UserDetailArtworkAdapter(ArrayList<ShotsModel> shotsModels) {
            this.shotsModels = shotsModels;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_detail_artwork, null);

            ViewHolder viewHolder = new ViewHolder(view, mListener);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShotsModel shotsModel = shotsModels.get(position);
            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
            holder.networkImageView.setImageUrl(shotsModel.getShots_thumbnail_url(), imageLoader);
        }

        @Override
        public int getItemCount() {
            return shotsModels.size();
        }

        public void setItemClickListener(OnRecyclerViewOnClickListener listener){
            this.mListener = listener;
        }
    }
}
