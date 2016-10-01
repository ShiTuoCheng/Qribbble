package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailFollowingFragment extends Fragment {

    private RecyclerView user_detail_following_recyclerView;
    private ExecutorService pool = Executors.newCachedThreadPool();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_detail_following, container, false);
        setUpView(v);
        pool.execute(fetchData());
        return v;
    }

    private void setUpView(View view){

        user_detail_following_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_following_recyclerView);

    }

    private Runnable fetchData(){

        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }
}
