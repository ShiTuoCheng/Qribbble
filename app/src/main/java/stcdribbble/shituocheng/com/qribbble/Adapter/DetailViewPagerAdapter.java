package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailInfoFragment;

/**
 * Created by shituocheng on 31/07/2016.
 */

public class DetailViewPagerAdapter extends FragmentStatePagerAdapter {

    int number;

    public DetailViewPagerAdapter(FragmentManager fm, int number) {
        super(fm);
        this.number = number;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ShotsDetailInfoFragment shotsDetailInfoFragment = new ShotsDetailInfoFragment();
                return shotsDetailInfoFragment;
            case 1:
                ShotsDetailInfoFragment shotsDetailInfoFragment1 = new ShotsDetailInfoFragment();
                return shotsDetailInfoFragment1;
            case 2:
                ShotsDetailInfoFragment shotsDetailInfoFragment2 = new ShotsDetailInfoFragment();
                return shotsDetailInfoFragment2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return number;
    }
}
