package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.RecentShotsFragment;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnLoadMoreListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

/**
 * Created by shituocheng on 2016/7/18.
 */
public class ShotsRecyclerViewAdapter extends RecyclerView.Adapter{

    private List<ShotsModel> mShotsModels = new ArrayList<>();
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_PROGRESSBAR = 0;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 5;

    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();
    private OnRecyclerViewOnClickListener mListener;


    public static class ShotsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private NetworkImageView each_shots_imageView;
        private TextView each_shots_textView;
        private TextView each_shots_author_textView;
        private TextView each_shots_review_times_textView;
        private TextView each_shots_favorite_times_textView;
        private TextView each_shots_view_times_textView;
        private CircularNetworkImageView each_shots_author_avatar;
        private ImageView isGifImageView;
        private OnRecyclerViewOnClickListener listener;


        public ShotsViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
            super(itemView);
            each_shots_imageView = (NetworkImageView)itemView.findViewById(R.id.shots_imageView);
            each_shots_textView = (TextView)itemView.findViewById(R.id.shots_title);
            each_shots_author_textView = (TextView)itemView.findViewById(R.id.shots_author_textView);
            each_shots_favorite_times_textView = (TextView)itemView.findViewById(R.id.shots_favorite_times);
            each_shots_review_times_textView = (TextView)itemView.findViewById(R.id.shots_review_times);
            each_shots_view_times_textView = (TextView)itemView.findViewById(R.id.shots_view_times);
            each_shots_author_avatar = (CircularNetworkImageView)itemView.findViewById(R.id.shots_author_avatar);
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


    public static class ProgressViewHolder extends RecyclerView.ViewHolder{

        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar1);
        }
    }


    public ShotsRecyclerViewAdapter(List<ShotsModel> shotsModels, RecyclerView recyclerView) {
        mShotsModels = shotsModels;

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

    @Override
    public int getItemViewType(int position) {
        return mShotsModels.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_PROGRESSBAR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.layout_shots_item, parent, false);

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

        //runEnterAnimation(holder.itemView,position);

        if (holder instanceof ShotsViewHolder){
            ShotsModel shotsModel = mShotsModels.get(position);

            ((ShotsViewHolder)holder).each_shots_textView.setText(shotsModel.getTitle());

            ((ShotsViewHolder)holder).each_shots_imageView.setImageUrl(shotsModel.getShots_thumbnail_url(),mImageLoader);

            ((ShotsViewHolder)holder).each_shots_view_times_textView.setText(String.valueOf(shotsModel.getShots_view_count()));

            ((ShotsViewHolder)holder).each_shots_favorite_times_textView.setText(String.valueOf(shotsModel.getShots_like_count()));

            ((ShotsViewHolder)holder).each_shots_author_textView.setText(shotsModel.getShots_author_name());

            ((ShotsViewHolder)holder).each_shots_review_times_textView.setText(String.valueOf(shotsModel.getShots_review_count()));

            ((ShotsViewHolder)holder).each_shots_author_avatar.setImageUrl(shotsModel.getShots_author_avatar(),mImageLoader);

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
        return mShotsModels.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setItemClickListener(OnRecyclerViewOnClickListener listener){
        this.mListener = listener;
    }

}
