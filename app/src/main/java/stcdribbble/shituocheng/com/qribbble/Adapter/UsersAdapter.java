package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import stcdribbble.shituocheng.com.qribbble.Model.UserModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

/**
 * Created by shituocheng on 2016/10/12.
 */

public  class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<UserModel> userModels = new ArrayList<>();
    public ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_PROGRESSBAR = 0;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 5;
    private Context context;

    public UsersAdapter(Context context ,List<UserModel> userModels, RecyclerView recyclerView) {
        this.userModels = userModels;
        this.context = context;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager
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

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        private CircularNetworkImageView avatar_imageView;
        private TextView name_textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            avatar_imageView = (CircularNetworkImageView)itemView.findViewById(R.id.shots_detail_favorite_avatar);
            name_textView = (TextView)itemView.findViewById(R.id.shots_detail_favorite_name);
        }
    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder{

        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return userModels.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_PROGRESSBAR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_favorite_item, parent, false);

            vh = new ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ItemViewHolder){
            final UserModel userModel = userModels.get(position);

            ((ItemViewHolder)holder).name_textView.setText(userModel.getName());
            ((ItemViewHolder)holder).avatar_imageView.setImageUrl(userModel.getAvatar(),imageLoader);

            ((ItemViewHolder)holder).avatar_imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.openProfile(context.getApplicationContext(), userModel.getUser_name());
                }
            });
        }else {
            ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded(){
        loading = false;
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        this.onLoadMoreListener = onLoadMoreListener;
    }
}
