package stcdribbble.shituocheng.com.qribbble.UI.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import stcdribbble.shituocheng.com.qribbble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    public BaseFragment() {
        // Required empty public constructor
    }


    public Runnable fetchData(boolean isFirstLoading){
        if (isFirstLoading){

        }else {

        }
        return null;
    }

    public Runnable fetchData(){

        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    public void setUpView(View view){

    }

}
