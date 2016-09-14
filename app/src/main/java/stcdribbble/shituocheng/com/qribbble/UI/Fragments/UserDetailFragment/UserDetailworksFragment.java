package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailworksFragment extends BaseFragment {

    private RecyclerView user_detail_works_recyclerView;


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
        }
        return v;
    }

    @Override
    public void setUpView(View view){

        user_detail_works_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_works_recyclerView);

    }

    public void fetchData(String user_name, boolean isFirstLoading) throws MalformedURLException {
        HttpURLConnection connection;
        InputStream inputStream;
        String api= API.generic_api+"/users/"+user_name+"/shots"+ Access_Token.access_token;
        if (isFirstLoading){
            try {
                connection = (HttpURLConnection)new URL(api).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
        }
    }
}
