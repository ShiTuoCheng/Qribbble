package stcdribbble.shituocheng.com.qribbble.UI.TabFragments;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.Adapter.MainViewPagerAdapter;
import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.UI.View.BottomDialog;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;

import static stcdribbble.shituocheng.com.qribbble.Utilities.AppController.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainTabFragment extends Fragment {

    private ViewPager mainViewPager;
    private TabLayout mainTabLayout;
    private FloatingActionButton floatingActionButton;
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
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state){
                    case ViewPager.SCROLL_STATE_IDLE:
                        floatingActionButton.show();
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                        floatingActionButton.hide();
                        break;
                }
            }
        });
        return view;
    }

    private void setUpView(View view){
        mainViewPager = (ViewPager)view.findViewById(R.id.main_viewPager);
        mainTabLayout = (TabLayout)view.findViewById(R.id.main_tab_Layout);
        floatingActionButton = (FloatingActionButton)view.findViewById(R.id.recent_shots_fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogFragment();
            }
        });
    }

    public void showDialogFragment(){
        Log.i(TAG,"showDialogFragment");
        BottomDialog bottomDialog = BottomDialog.newInstance();
        bottomDialog.show(getChildFragmentManager(),BottomDialog.class.getSimpleName());
    }

}
