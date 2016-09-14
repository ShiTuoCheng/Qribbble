package stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import stcdribbble.shituocheng.com.qribbble.Model.CommentModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;
import stcdribbble.shituocheng.com.qribbble.Utilities.Utils;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailCommentFragment extends Fragment {

    private RecyclerView shots_detail_comment_recyclerView;
    private Toolbar shots_detail_comment_toolBat;
    private FloatingActionButton shots_detail_comment_fab;
    private EditText shots_detail_comment_editText;

    private ArrayList<CommentModel> commentModels = new ArrayList<>();

    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final int MESSAGE_WHAT = 0;
    private static final int MESSAGE_WHAT_SCROLL_DOWN=1;

    private int current_page = 1;

    private Handler handler;

    public ShotsDetailCommentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_shots_detail_comment, container, false);
        final int shots_id = getActivity().getIntent().getIntExtra("id",0);
        setUpView(v);
        threadPool.execute(initComment(String.valueOf(shots_id), true));

        shots_detail_comment_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isSlidingToLast = false;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isSlidingToLast = dy > 0;
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    if (lastItemPosition == (totalItemCount - 1) && isSlidingToLast) {
                        threadPool.execute(initComment(String.valueOf(shots_id), false));
                    }
                }
            }
        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data",MODE_PRIVATE);
        final String access_token = sharedPreferences.getString("access_token","");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MESSAGE_WHAT:
                        int code = (int)msg.obj;
                        if (!String.valueOf(code).equals("201")){
                            Snackbar.make(v, "Upload comments error!", Snackbar.LENGTH_SHORT).show();
                        }else {
                            threadPool.execute(initComment(String.valueOf(shots_id), true));
                            Snackbar.make(v, "Upload comments successful!", Snackbar.LENGTH_SHORT).show();
                        }
                }
            }
        };
        shots_detail_comment_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String inputString = shots_detail_comment_editText.getText().toString();
                if (!access_token.isEmpty()){

                    if (inputString.isEmpty()){
                        Snackbar.make(v, "Please input something to comment", Snackbar.LENGTH_SHORT).show();
                    }else {
                        threadPool.execute(postComment(access_token, String.valueOf(shots_id), inputString));
                    }

                }else {
                    Snackbar.make(v, "Please Login Your Dribbble Account", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private void setUpView(View view){
        shots_detail_comment_recyclerView = (RecyclerView)view.findViewById(R.id.shots_detail_comment_recyclerView);
        shots_detail_comment_toolBat = (Toolbar)view.findViewById(R.id.comment_toolBar);
        shots_detail_comment_fab = (FloatingActionButton)view.findViewById(R.id.comment_send_fab);
        shots_detail_comment_editText = (EditText)view.findViewById(R.id.comment_input_editText);
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
                            JSONObject userJsonObj = eachCommentObj.getJSONObject("user");
                            commentModel.setComment_user_avatar(userJsonObj.getString("avatar_url"));
                            commentModel.setComment_user_name(userJsonObj.getString("name"));
                            commentModel.setComment_name(userJsonObj.getString("username"));
                            commentModels.add(commentModel);
                        }

                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CommentAdapter commentAdapter = new CommentAdapter(commentModels);
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    shots_detail_comment_recyclerView.setAdapter(commentAdapter);
                                    shots_detail_comment_recyclerView.setLayoutManager(linearLayoutManager);
                                    commentAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    current_page += 1;
                    String load_more_api = API.generic_api + "shots/"+shots_id+"/comments?page="+ current_page +"&access_token=" + Access_Token.access_token;
                    try {
                        connection = (HttpURLConnection)new URL(load_more_api).openConnection();
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
                            JSONObject userJsonObj = eachCommentObj.getJSONObject("user");
                            commentModel.setComment_user_avatar(userJsonObj.getString("avatar_url"));
                            commentModel.setComment_user_name(userJsonObj.getString("name"));
                            commentModel.setComment_name(userJsonObj.getString("username"));
                            commentModels.add(commentModel);
                        }

                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CommentAdapter commentAdapter = new CommentAdapter(commentModels);
                                    commentAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(), "there is nothing else!!", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        };
    }

    private Runnable postComment(final String access_token, final String shots_id, final String comment_content){
        return new Runnable() {
            HttpURLConnection connection;
            String api = API.generic_api + "shots/"+shots_id+"/comments?access_token="+access_token;
            @Override
            public void run() {
                try {
                    connection = (HttpURLConnection)new URL(api).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.connect();

                    PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
                    printWriter.print(comment_content);
                    printWriter.flush();
                    printWriter.close();

                    final int code = connection.getResponseCode();

                    Log.d("connection_code", String.valueOf(code));

                    Message message = handler.obtainMessage();
                    message.what = MESSAGE_WHAT;
                    message.obj = code;
                    message.sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

        private ArrayList<CommentModel> commentModels = new ArrayList<>();

        public class ViewHolder  extends RecyclerView.ViewHolder{

            private TextView comment_textView;
            private CircularNetworkImageView comment_user_avatar;
            private TextView comment_user_name;

            public ViewHolder(View itemView) {
                super(itemView);
                comment_textView = (TextView) itemView.findViewById(R.id.shots_detail_comment_textView);
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
        @SuppressWarnings("deprecated")
        public void onBindViewHolder(ViewHolder holder, int position) {

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();

            final CommentModel commentModel = commentModels.get(position);

            holder.comment_user_avatar.setImageUrl(commentModel.getComment_user_avatar(), imageLoader);
            holder.comment_user_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.openProfile(getContext(), commentModel.getComment_name());
                }
            });
            String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/BMitra.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;color: #fff; background-color: #000;}a{color:#ff4091; text-decoration:none}</style></head><body>";
            String pas = "</body></html>";
            holder.comment_textView.setText(Html.fromHtml(commentModel.getComment_cotent()));
            holder.comment_textView.setMovementMethod(LinkMovementMethod.getInstance());
            holder.comment_textView.setTextColor(getResources().getColor(R.color.whiteColor));
            holder.comment_user_name.setText(commentModel.getComment_user_name());
        }

        @Override
        public int getItemCount() {
            return commentModels.size();
        }
    }

    private class ItemDecoration extends RecyclerView.ItemDecoration{

        int space;

        public ItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            c.clipRect(parent.getLeft(), 0, parent.getRight(), 0);
            c.drawARGB(255, 255, 64, 145);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            if (parent.getChildPosition(view) == 0){
                outRect.top = space;
            }
        }
    }

}
