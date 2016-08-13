package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import stcdribbble.shituocheng.com.qribbble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private Spinner list_spinner;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        setUpView(v);
        String[] lists = getResources().getStringArray(R.array.list_array);
        ArrayAdapter<String> list_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_array_list, lists);
        list_spinner_adapter.setDropDownViewResource(R.layout.custom_drop_down);
        list_spinner.setAdapter(list_spinner_adapter);
        list_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String[] list = getResources().getStringArray(R.array.list_array);
                Toast.makeText(getActivity(), "你点击的是:"+list[i], 2000).show();
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
    }
}
