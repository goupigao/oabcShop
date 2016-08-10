package cc.oabc.shop;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/8/3.
 */
abstract public class ViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> fragments;
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>(getCount());
    }
    abstract public View getTabView(int position);
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.remove(position);
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}
