package stcdribbble.shituocheng.com.qribbble.UI.TabFragments;


import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.Adapter.MainViewPagerAdapter;
import stcdribbble.shituocheng.com.qribbble.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainTabFragment extends Fragment {

    private ViewPager mainViewPager;
    private TabLayout mainTabLayout;
    private ArrayList<String> title = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_tab, container, false);
        setUpView(view);
        title.add("debuts");
        title.add("timeline");
        title.add("popular");
        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getFragmentManager(),title,getActivity());

        mainViewPager.setAdapter(mainViewPagerAdapter);
        mainTabLayout.setupWithViewPager(mainViewPager);
        return view;
    }

    private void setUpView(View view){
        mainViewPager = (ViewPager)view.findViewById(R.id.main_viewPager);
        mainTabLayout = (TabLayout)view.findViewById(R.id.main_tab_Layout);
    }

}
