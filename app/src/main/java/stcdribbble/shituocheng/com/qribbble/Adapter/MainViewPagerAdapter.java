package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;

import java.util.ArrayList;

import stcdribbble.shituocheng.com.qribbble.UI.Fragments.RecentShotsFragment;

/**
 * Created by shituocheng on 16/7/13.
 */

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> title;
    private Context context;

    public MainViewPagerAdapter(FragmentManager fm, ArrayList<String> title, Context context) {
        super(fm);
        this.title = title;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return RecentShotsFragment.newInstance(position+1);
    }

    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }
}
