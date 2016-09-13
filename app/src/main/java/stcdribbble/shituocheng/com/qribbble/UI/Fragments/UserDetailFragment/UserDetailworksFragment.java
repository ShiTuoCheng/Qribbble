package stcdribbble.shituocheng.com.qribbble.UI.Fragments.UserDetailFragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import stcdribbble.shituocheng.com.qribbble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailworksFragment extends Fragment {

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
        return v;
    }


    private void setUpView(View view){

        user_detail_works_recyclerView = (RecyclerView)view.findViewById(R.id.user_detail_works_recyclerView);

    }


}
