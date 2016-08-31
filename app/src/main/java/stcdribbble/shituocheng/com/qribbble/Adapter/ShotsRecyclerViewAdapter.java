package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.content.Context;
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
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

/**
 * Created by shituocheng on 2016/7/18.
 */
public class ShotsRecyclerViewAdapter extends RecyclerView.Adapter<ShotsRecyclerViewAdapter.ViewHolder> {

    private List<ShotsModel> mShotsModels = new ArrayList<>();
    private Context context;
    private final LayoutInflater inflater;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_PROGRESSBAR = 0;
    private boolean isFooterEnabled = true;

    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

    private RecentShotsFragment recentShotsFragment = new RecentShotsFragment();
    private boolean animateItems = false;
    private int lastAnimatedPosition = -1;
    private OnRecyclerViewOnClickListener mListener;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private NetworkImageView each_shots_imageView;
        private TextView each_shots_textView;
        private TextView each_shots_author_textView;
        private TextView each_shots_review_times_textView;
        private TextView each_shots_favorite_times_textView;
        private TextView each_shots_view_times_textView;
        private CircularNetworkImageView each_shots_author_avatar;
        private ImageView isGifImageView;
        private OnRecyclerViewOnClickListener listener;


        public ViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
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
            progressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
        }
    }

    public ShotsRecyclerViewAdapter(List<ShotsModel> shotsModels, Context context) {
        mShotsModels = shotsModels;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shots_item,null);

        ViewHolder viewHolder = new ViewHolder(view, mListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        runEnterAnimation(holder.itemView,position);

        ShotsModel shotsModel = mShotsModels.get(position);

        holder.each_shots_textView.setText(shotsModel.getTitle());

        holder.each_shots_imageView.setImageUrl(shotsModel.getShots_thumbnail_url(),mImageLoader);

        holder.each_shots_view_times_textView.setText(String.valueOf(shotsModel.getShots_view_count()));

        holder.each_shots_favorite_times_textView.setText(String.valueOf(shotsModel.getShots_like_count()));

        holder.each_shots_author_textView.setText(shotsModel.getShots_author_name());

        holder.each_shots_review_times_textView.setText(String.valueOf(shotsModel.getShots_review_count()));

        holder.each_shots_author_avatar.setImageUrl(shotsModel.getShots_author_avatar(),mImageLoader);

        if (shotsModel.isAnimated()){
            holder.isGifImageView.setImageResource(R.drawable.ic_gif_black_24dp);
        }else {


        }

    }

    @Override
    public int getItemCount() {
        return mShotsModels.size();
    }

    public void setItemClickListener(OnRecyclerViewOnClickListener listener){
        this.mListener = listener;
    }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= 3) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(recentShotsFragment.getActivity()));
            view.animate()
                    .translationY(0)
                    .setStartDelay(100 * position)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }
}
