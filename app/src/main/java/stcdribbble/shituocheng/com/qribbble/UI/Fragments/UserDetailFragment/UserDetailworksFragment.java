package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailworksFragment extends BaseFragment {

    private RecyclerView user_detail_works_recyclerView;
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();


    public UserDetailworksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_detailworks, container, false);
        setUpView(v);
        Intent intent = getActivity().getIntent();
        if (intent !=null){
            String name = intent.getStringExtra("user_name");

            pool.execute(fetchData(name, false));
        }

        return v;
    }

    @Override
    public void setUpView(View view){

        user_detail_works_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_works_recyclerView);

    }

    public Runnable fetchData(String user_name, final boolean isFirstLoading){
        final HttpURLConnection[] connection = new HttpURLConnection[1];
        final InputStream[] inputStream = new InputStream[1];
        final String api= API.generic_api+"/users/"+user_name+"/shots"+ Access_Token.access_token;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isFirstLoading){
                    try {
                        connection[0] = (HttpURLConnection)new URL(api).openConnection();
                        connection[0].setRequestMethod("GET");
                        connection[0].connect();

                        inputStream[0] = connection[0].getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream[0]));
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine())!=null){
                            stringBuilder.append(line);
                        }

                        JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                        for (int i = 0; i < jsonArray.length(); i++){
                            ShotsModel shotsModel = new ShotsModel();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            JSONObject imageJsonObj = jsonObject.getJSONObject("images");
                            shotsModel.setShots_thumbnail_url(imageJsonObj.getString("normal"));
                            shotsModel.setShots_full_imageUrl(imageJsonObj.getString("hidpi"));

                            shotsModels.add(shotsModel);

                        }

                        if (getActivity() != null){
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {

                }
            }
        };

        return runnable;

    }

    private class UserDetailArtworkAdapter extends RecyclerView.Adapter<UserDetailArtworkAdapter.ViewHolder>{

        ArrayList<ShotsModel> shotsModels = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder{

            private NetworkImageView networkImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                networkImageView = (NetworkImageView)itemView.findViewById(R.id.user_detail_artwork);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_detail_artwork, null);

            ViewHolder viewHolder = new ViewHolder(view);

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
    }
}
