package stcdribbble.shituocheng.com.qribbble.UI.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import stcdribbble.shituocheng.com.qribbble.R;

public class SearchResultActivity extends AppCompatActivity {

    private String search_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            search_string = getIntent().getStringExtra(SearchManager.QUERY);
            Log.d("RESULT_SEARCH",search_string);
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(search_string);
    }

    private Runnable query(String search_string){

    }

}
