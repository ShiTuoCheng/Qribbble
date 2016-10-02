package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.Adapter.ShotsRecyclerViewAdapter;
import stcdribbble.shituocheng.com.qribbble.Model.ShotsModel;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.OnRecyclerViewOnClickListener;

public class SearchResultActivity extends AppCompatActivity {

    private String search_string;
    private ArrayList<ShotsModel> shotsModels = new ArrayList<>();
    private ExecutorService pool = Executors.newCachedThreadPool();
    private ProgressBar progressBar;
    private RecyclerView search_result_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        setUpViews();
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            search_string = getIntent().getStringExtra(SearchManager.QUERY);
            Log.d("RESULT_SEARCH", search_string);
            getSupportActionBar().setTitle("The result of "+search_string);
            pool.execute(query(search_string));
        }
    }

    private void setUpViews(){

        progressBar = (ProgressBar)findViewById(R.id.search_progressBar);
        search_result_recyclerView = (RecyclerView)findViewById(R.id.search_recyclerView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.finish();
        }
        return true;
    }

    private Runnable query(final String search_string) {
        return new Runnable() {

            String search_api = ("http://dribbble.com/search?q=" + search_string /*+ "&page=" + page*/).replaceAll("\\s", "%20");

            @Override
            public void run() {

                Elements elements = null;
                try {
                    elements = Jsoup.connect(search_api)
                            .cookie("shot_meta_preference", "with")
                            .cookie("shot_size", "large")
                            .get().select(".dribbble");

                    for (Element element : elements) {
                        Element shotImgElement = element.select(".dribbble-shot").first();
                        String imageUrl = shotImgElement.select(".dribbble-img a div div").attr("data-src");
                        String shotUrl = shotImgElement.select(".dribbble-img a").attr("href");
                        String title = shotImgElement.select(".dribbble-over strong").html();
                        int shotId = Integer.parseInt(shotUrl.substring(7, shotUrl.indexOf("-")));

                        int likes = 0, views = 0, comments = 0;
                        try {
                            Element shotToolsElement = shotImgElement.select("[class=tools group]").first();
                            likes = Integer.parseInt(shotToolsElement.select(".fav a").html().replaceAll(",", ""));
                            comments = Integer.parseInt(shotToolsElement.select(".cmnt span").html().replaceAll(",", ""));
                            views = Integer.parseInt(shotToolsElement.select(".views span").html().replaceAll(",", ""));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        Element shotExtrasElement = element.select(".extras").first();
                        boolean hasRebounds = Integer.parseInt(shotExtrasElement.select("a span").html().substring(0, 1)) > 0;
                        final ShotsModel shotsModel = new ShotsModel();
                        shotsModel.setTitle(title);
                        shotsModel.setShots_like_count(likes);
                        shotsModel.setShots_id(shotId);
                        shotsModel.setShots_review_count(comments);
                        shotsModel.setShots_thumbnail_url(imageUrl);
                        shotsModel.setShots_view_count(views);
                        shotsModels.add(shotsModel);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                search_result_recyclerView.setVisibility(View.VISIBLE);
                                ShotsRecyclerViewAdapter shotsRecyclerViewAdapter = new ShotsRecyclerViewAdapter(shotsModels,getApplicationContext());
                                shotsRecyclerViewAdapter.notifyDataSetChanged();
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                search_result_recyclerView.setLayoutManager(linearLayoutManager);
                                search_result_recyclerView.setAdapter(shotsRecyclerViewAdapter);
                                shotsRecyclerViewAdapter.setItemClickListener(new OnRecyclerViewOnClickListener() {
                                    @Override
                                    public void OnItemClick(View v, int position) {

                                        Intent intent = new Intent(SearchResultActivity.this, ShotsDetailActivity.class);
                                        ShotsModel shotsModel = shotsModels.get(position);
                                        String imageUrl = shotsModel.getShots_thumbnail_url();
                                        String imageName = shotsModel.getTitle();
                                        int id = shotsModel.getShots_id();
                                        boolean isGif = shotsModel.isAnimated();
                                        intent.putExtra("imageName",imageName);
                                        intent.putExtra("imageURL",imageUrl);
                                        intent.putExtra("isGif",isGif);
                                        intent.putExtra("id",id);

                                        startActivity(intent);
                                    }
                                });
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
