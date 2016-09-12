package stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.ShotsDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Activities.UserDetailActivity;
import stcdribbble.shituocheng.com.qribbble.UI.Fragments.BaseFragment;
import stcdribbble.shituocheng.com.qribbble.UI.View.CircularNetworkImageView;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AppController;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShotsDetailInfoFragment extends BaseFragment {

    private TextView shots_detail_title;
    private TextView shots_detail_description;
    private CircularNetworkImageView shots_author_avatar;
    private TextView shots_author_textView;
    private RecyclerView tagsRecyclerView;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private ArrayList<String> tagsList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shots_detail_info, container, false);



        setUpView(view);
        fetchData();
        return view;
    }

    @Override
    public void setUpView(View view){

        shots_detail_title = (TextView)view.findViewById(R.id.shots_detail_title);
        shots_detail_description = (TextView) view.findViewById(R.id.shots_detail_description);
        shots_author_avatar = (CircularNetworkImageView)view.findViewById(R.id.shots_detail_avatar);
        shots_author_textView = (TextView)view.findViewById(R.id.shots_author_name);
        tagsRecyclerView = (RecyclerView)view.findViewById(R.id.shots_tag_recyclerView);

    }

    @Override
    public void fetchData(){

        new Thread(new Runnable() {
            HttpURLConnection connection;
            InputStream inputStream;
            int shots_id = getActivity().getIntent().getIntExtra("id",0);
            String shots_api = API.generic_api+"shots/"+ String.valueOf(shots_id)+"?access_token="+ Access_Token.access_token;
            @Override
            public void run() {
                try {
                    connection = (HttpURLConnection)new URL(shots_api).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = bufferedReader.readLine())!=null){
                        stringBuilder.append(line);
                    }
                    Log.d("shots", shots_api);

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                    final String shots_title_name = jsonObject.getString("title");
                    final String shots_description = jsonObject.getString("description");
                    JSONObject userJsonObj = jsonObject.getJSONObject("user");
                    final String shots_author_name = userJsonObj.getString("name");
                    final String shots_author_username = userJsonObj.getString("username");
                    boolean isPro = userJsonObj.getBoolean("pro");
                    final JSONArray tagsArray = jsonObject.getJSONArray("tags");
                    for (int i=0; i<tagsArray.length(); i++){
                        String tag = tagsArray.getString(i);
                        tagsList.add(tag);
                    }
                    Log.d("tags",tagsArray.toString());

                    final String shots_author_avatar_img = userJsonObj.getString("avatar_url");

                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            shots_detail_title.setText(shots_title_name);
                            String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/BMitra.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;color: #fff; background-color: #000;}a{color:#ff4091; text-decoration:none}</style></head><body>";
                            String pas = "</body></html>";

                            //<style>a{color:purple; text-decoration:none}</style>
                            /*
                            String data = "<html><head>"
                                    + "<style type=\"text/css\">body{color: #fff; background-color: #000;}"
                                    + "</style></head>"
                                    + "<body>"
                                    + shots_description
                                    + "</body></html>";
                                    */
                            String data = pish + shots_description + pas;
                            String no_descriptions = pish + "(No Descriptions)" + pas;
                            if (shots_description.equals("null")){
                                shots_detail_description.setText("No descriptions");
                                shots_detail_description.setTextColor(getResources().getColor(R.color.whiteColor));
                            }else {
                                shots_detail_description.setText(Html.fromHtml(shots_description));
                                shots_detail_description.setMovementMethod(LinkMovementMethod.getInstance());
                                shots_detail_description.setTextColor(getResources().getColor(R.color.whiteColor));
                            }
                            shots_author_avatar.setImageUrl(shots_author_avatar_img, imageLoader);
                            shots_author_textView.setText(shots_author_name);
                            TagAdapter tagAdapter = new TagAdapter(tagsList);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            tagsRecyclerView.setLayoutManager(linearLayoutManager);
                            tagsRecyclerView.setAdapter(tagAdapter);
                            tagsRecyclerView.setNestedScrollingEnabled(false);
                            shots_author_avatar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                                    Log.w("test",shots_author_name);
                                    intent.putExtra("user_name",shots_author_username);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder>{

        private ArrayList<String> tags = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder{

            private TextView tag_textView;

            public ViewHolder(View itemView) {
                super(itemView);
                tag_textView = (TextView)itemView.findViewById(R.id.tag_textView);
            }

        }

        public TagAdapter(ArrayList<String> tags) {
            this.tags = tags;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tag_item,null);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.tag_textView.setText(tags.get(position));

        }

        @Override
        public int getItemCount() {
            return tags.size();
        }
    }

}
