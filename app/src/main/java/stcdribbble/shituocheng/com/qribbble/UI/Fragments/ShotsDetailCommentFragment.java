package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import stcdribbble.shituocheng.com.qribbble.Model.CommentModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailCommentFragment extends Fragment {

    private RecyclerView shots_detail_comment_recyclerView;
    private Toolbar shots_detail_comment_toolBat;
    private FloatingActionButton shots_detail_comment_fab;
    private TextInputEditText shots_detail_comment_editText;

    private ArrayList<CommentModel> commentModels = new ArrayList<>();

    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public ShotsDetailCommentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_shots_detail_comment, container, false);
        int shots_id = getActivity().getIntent().getIntExtra("id",0);
        setUpView(v);
        threadPool.execute(initComment(String.valueOf(shots_id), true));

        return v;
    }

    private void setUpView(View view){
        shots_detail_comment_recyclerView = (RecyclerView)view.findViewById(R.id.shots_detail_comment_recyclerView);
        shots_detail_comment_toolBat = (Toolbar)view.findViewById(R.id.comment_toolBar);
        shots_detail_comment_fab = (FloatingActionButton)view.findViewById(R.id.comment_send_fab);
        shots_detail_comment_editText = (TextInputEditText)view.findViewById(R.id.comment_input_editText);
    }

    private Runnable initComment(final String shots_id, final boolean isFirstLaoding){
        return new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                InputStream inputStream;
                String api = API.generic_api + "shots/"+shots_id+"/comments?access_token=" + Access_Token.access_token;
                if (isFirstLaoding){
                    try {
                        connection = (HttpURLConnection)new URL(api).openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = bufferedReader.readLine())!=null){
                            stringBuilder.append(line);
                        }

                        JSONArray comments_jsonArray = new JSONArray(stringBuilder.toString());
                        for (int i = 0; i<comments_jsonArray.length(); i++){
                            CommentModel commentModel = new CommentModel();
                            JSONObject eachCommentObj = comments_jsonArray.getJSONObject(i);
                            commentModel.setComment_cotent(eachCommentObj.getString("body"));

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                }
            }
        };
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

        private ArrayList<CommentModel> commentModels = new ArrayList<>();

        public class ViewHolder  extends RecyclerView.ViewHolder{

            private WebView comment_webView;
            private CircularNetworkImageView comment_user_avatar;
            private TextView comment_user_name;

            public ViewHolder(View itemView) {
                super(itemView);
                comment_webView = (WebView)itemView.findViewById(R.id.shots_detail_comment_webView);
                comment_user_avatar = (CircularNetworkImageView)itemView.findViewById(R.id.shots_detail_comment_avatar);
                comment_user_name = (TextView)itemView.findViewById(R.id.shots_detail_comment_name);
            }
        }

        public CommentAdapter(ArrayList<CommentModel> commentModels) {
            this.commentModels = commentModels;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment_item, null);
            ViewHolder viewHolder = new ViewHolder(v);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();

            CommentModel commentModel = commentModels.get(position);

            holder.comment_user_avatar.setImageUrl(commentModel.getComment_user_avatar(), imageLoader);
            String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/BMitra.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;color: #fff; background-color: #000;}a{color:#ff4091; text-decoration:none}</style></head><body>";
            String pas = "</body></html>";
            holder.comment_webView.loadData(pish+commentModel.getComment_cotent()+pas,"text/html;charset=UTF-8",null);
            holder.comment_user_name.setText(commentModel.getComment_user_name());

        }

        @Override
        public int getItemCount() {
            return commentModels.size();
        }
    }

}
