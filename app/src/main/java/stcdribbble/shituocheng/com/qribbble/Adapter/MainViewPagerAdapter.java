package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import stcdribbble.shituocheng.com.qribbble.UI.Fragments.RecentShotsFragment;

/**
 * Created by shituocheng on 16/7/13.
 */

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private int number;

    public MainViewPagerAdapter(FragmentManager fm, int number) {
        super(fm);
        this.number = number;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RecentShotsFragment recentShotsFragment = new RecentShotsFragment();
                return recentShotsFragment;
            case 1:
                RecentShotsFragment mainFollowFragment = new RecentShotsFragment();
                return mainFollowFragment;
            case 2:
                RecentShotsFragment mainTimeLineFragment = new RecentShotsFragment();
                return mainTimeLineFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return number;
    }
}
