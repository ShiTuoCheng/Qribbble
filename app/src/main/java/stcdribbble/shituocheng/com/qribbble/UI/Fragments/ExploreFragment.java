package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private Spinner list_spinner;
    private RecyclerView explore_recyclerView;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        setUpView(v);
        final FetchDataTask fetchDataTask = new FetchDataTask();
        String[] lists = getResources().getStringArray(R.array.list_array);
        ArrayAdapter<String> list_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, lists);
        list_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        list_spinner.setAdapter(list_spinner_adapter);
        list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String[] list = getResources().getStringArray(R.array.list_array);
                Toast.makeText(getActivity(), "你点击的是:"+list[i], 2000).show();
                fetchData(true);
                fetchDataTask.execute(list[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return v;
    }

    @Override
    public void setUpView(View view) {
        list_spinner = (Spinner)view.findViewById(R.id.list_spinner);
        explore_recyclerView = (RecyclerView)view.findViewById(R.id.explore_recyclerView);
    }

    public void fetchData(String shots_list) {

        HttpURLConnection connection;
        InputStream inputStream;
        String shots_api = API.getSortsShotsApi(shots_list);


    }

    private class FetchDataTask extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            String shots_list = strings[0];
            fetchData(shots_list);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
