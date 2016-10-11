package stcdribbble.shituocheng.com.qribbble.Model;

import android.content.PeriodicSync;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by shituocheng on 2016/7/18.
 */
public class ShotsModel implements Serializable{
    private String title;
    private int shots_like_count;
    private int shots_view_count;
    private int shots_review_count;
    private int shots_id;
    private String shots_thumbnail_url;
    private String shots_author_name;
    private String shots_author_avatar;
    private String shots_full_imageUrl;
    private String shots_share_url;
    private boolean animated;

    public String getShots_share_url() {
        return shots_share_url;
    }

    public void setShots_share_url(String shots_share_url) {
        this.shots_share_url = shots_share_url;
    }

    public int getShots_like_count() {
        return shots_like_count;
    }

    public void setShots_like_count(int shots_like_count) {
        this.shots_like_count = shots_like_count;
    }

    public int getShots_review_count() {
        return shots_review_count;
    }

    public void setShots_review_count(int shots_review_count) {
        this.shots_review_count = shots_review_count;
    }

    public int getShots_view_count() {
        return shots_view_count;
    }

    public void setShots_view_count(int shots_view_count) {
        this.shots_view_count = shots_view_count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShots_author_avatar() {
        return shots_author_avatar;
    }

    public void setShots_author_avatar(String shots_author_avatar) {
        this.shots_author_avatar = shots_author_avatar;
    }

    public String getShots_author_name() {
        return shots_author_name;
    }

    public void setShots_author_name(String shots_author_name) {
        this.shots_author_name = shots_author_name;
    }

    public String getShots_thumbnail_url() {
        return shots_thumbnail_url;
    }

    public void setShots_thumbnail_url(String shots_thumbnail_url) {
        this.shots_thumbnail_url = shots_thumbnail_url;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public String getShots_full_imageUrl() {
        return shots_full_imageUrl;
    }

    public void setShots_full_imageUrl(String shots_full_imageUrl) {
        this.shots_full_imageUrl = shots_full_imageUrl;
    }

    public int getShots_id() {
        return shots_id;
    }

    public void setShots_id(int shots_id) {
        this.shots_id = shots_id;
    }

}
