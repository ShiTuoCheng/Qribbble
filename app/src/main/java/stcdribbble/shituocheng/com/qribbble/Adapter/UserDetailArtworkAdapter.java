package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

/**
 * Created by shituocheng on 2016/10/9.
 */

public class UserDetailArtworkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ShotsModel> shotsModels = new ArrayList<>();
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_PROGRESSBAR = 0;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 5;
    private OnRecyclerViewOnClickListener mListener;

    public class ShotsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView each_shots_review_times_textView;
        private TextView each_shots_favorite_times_textView;
        private TextView each_shots_view_times_textView;
        private ImageView isGifImageView;
        private NetworkImageView networkImageView;
        private OnRecyclerViewOnClickListener listener;

        public ShotsViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
            super(itemView);
            networkImageView = (NetworkImageView)itemView.findViewById(R.id.user_detail_artwork);
            each_shots_favorite_times_textView = (TextView)itemView.findViewById(R.id.shots_favorite_times);
            each_shots_review_times_textView = (TextView)itemView.findViewById(R.id.shots_review_times);
            each_shots_view_times_textView = (TextView)itemView.findViewById(R.id.shots_view_times);
            isGifImageView = (ImageView)itemView.findViewById(R.id.isGif);
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
        if (holder instanceof UserDetailArtworkAdapter.ShotsViewHolder){

            ShotsModel shotsModel = shotsModels.get(position);
            ImageLoader imageLoader = AppController.getInstance().getImageLoader();

            ((ShotsViewHolder)holder).networkImageView.setImageUrl(shotsModel.getShots_thumbnail_url(), imageLoader);

            ((ShotsViewHolder)holder).each_shots_view_times_textView.setText(String.valueOf(shotsModel.getShots_view_count()));

            ((ShotsViewHolder)holder).each_shots_favorite_times_textView.setText(String.valueOf(shotsModel.getShots_like_count()));

            ((ShotsViewHolder)holder).each_shots_review_times_textView.setText(String.valueOf(shotsModel.getShots_review_count()));

            if (shotsModel.isAnimated()){
                ((ShotsViewHolder)holder).isGifImageView.setImageResource(R.drawable.ic_gif_black_24dp);
            }else {

            }
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
