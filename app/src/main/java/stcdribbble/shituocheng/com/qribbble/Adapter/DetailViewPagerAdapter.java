package stcdribbble.shituocheng.com.qribbble.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import stcdribbble.shituocheng.com.qribbble.UI.Fragments.ShotsDetailFavoriteFragment;
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
                ShotsDetailFavoriteFragment shotsDetailFavoriteFragment = new ShotsDetailFavoriteFragment();
                return shotsDetailFavoriteFragment;
            case 2:
                ShotsDetailInfoFragment shotsDetailInfoFragment2 = new ShotsDetailInfoFragment();
                return shotsDetailInfoFragment2;
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return number;
    }
}
