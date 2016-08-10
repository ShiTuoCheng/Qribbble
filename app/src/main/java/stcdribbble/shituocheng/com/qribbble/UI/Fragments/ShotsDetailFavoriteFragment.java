package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.Model.UserModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailFavoriteFragment extends BaseFragment {

    private RecyclerView favorite_recyclerView;
    private ArrayList<UserModel> users = new ArrayList<>();

    public ShotsDetailFavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shots_detail_favorite, container, false);
    }

    @Override
    public void setUpView(View view) {
        favorite_recyclerView = (RecyclerView)view.findViewById(R.id.shots_detail_favorite_recyclerView);
    }

    public class usersAdapter extends RecyclerView.Adapter<usersAdapter.ViewHolder>{


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
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
    }

}
