package stcdribbble.shituocheng.com.qribbble.Model;

/**
 * Created by shituocheng on 2016/9/6.
 */

public class CommentModel {

    private String comment_cotent;
    private String comment_user_avatar;
    private String comment_user_name;
    private String comment_name;

    public CommentModel(Builder builder) {
        this.comment_cotent = builder.comment_cotent;
        this.comment_user_avatar = builder.comment_user_avatar;
        this.comment_user_name = builder.comment_user_name;
        this.comment_name = builder.comment_name;
    }

    public String getComment_cotent() {
        return comment_cotent;
    }

    public void setComment_cotent(String comment_cotent) {
        this.comment_cotent = comment_cotent;
    }

    public String getComment_user_avatar() {
        return comment_user_avatar;
    }

    public void setComment_user_avatar(String comment_user_avatar) {
        this.comment_user_avatar = comment_user_avatar;
    }

    public String getComment_user_name() {
        return comment_user_name;
    }

    public void setComment_user_name(String comment_user_name) {
        this.comment_user_name = comment_user_name;
    }

    public String getComment_name() {
        return comment_name;
    }

    public void setComment_name(String comment_name) {
        this.comment_name = comment_name;
    }

    public static class Builder{

        private String comment_cotent;
        private String comment_user_avatar;
        private String comment_user_name;
        private String comment_name;

        public Builder comment_cotent(String comment_cotent){
            this.comment_cotent = comment_cotent;
            return this;
        }

        public Builder comment_user_avatar(String comment_user_avatar){
            this.comment_user_avatar = comment_user_avatar;
            return this;
        }

        public Builder comment_user_name(String comment_user_name){
            this.comment_user_name = comment_user_name;
            return this;
        }

        public Builder comment_name(String comment_name){
            this.comment_name = comment_name;
            return this;
        }

        public CommentModel build(){
            return new CommentModel(this);
        }

    }
}
