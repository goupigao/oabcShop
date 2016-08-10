package cc.oabc.shop;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.module.cart.CartFragment;
import cc.oabc.shop.module.home.HomeFragment;
import cc.oabc.shop.module.mine.MineFragment;
import cc.oabc.shop.util.CLog;

/**
 * Created by Administrator on 2016/7/30.
 */
public class MainActivity extends AppCompatActivity {
    @BindView(android.R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.viewPager) ViewPager viewPager;
    public View cartTabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CLog.d("pastTime",App.pastTime("MainActivity.onCreate.begin"));
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(savedInstanceState!=null){
            Toast.makeText(this,savedInstanceState.getString("test",""),Toast.LENGTH_SHORT).show();
        }

        final ViewPagerAdapter fm = new ViewPagerAdapter(getFragmentManager()) {
            private CharSequence[] tabTitle = {getString(R.string.title_home),getString(R.string.title_cart),getString(R.string.title_mine)};
            private int[] tabIcon = { R.drawable.ic_view_list, R.drawable.ic_shopping_cart, R.drawable.ic_account_circle};
            @Override
            public int getCount() {
                return 3;
            }
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new HomeFragment();
                    case 1:
                        return new CartFragment();
                    case 2:
                        return new MineFragment();
                    default:
                        return new HomeFragment();
                }
            }
            @Override
            public View getTabView(int position){
                View v = getLayoutInflater().inflate(R.layout.custom_tab,null);
                ImageView img = (ImageView)v.findViewById(R.id.tab_icon);
                img.setImageResource(tabIcon[position]);
                TextView tv = (TextView)v.findViewById(R.id.tab_text);
                tv.setText(tabTitle[position]);
                if(position == 0){
                    v.setSelected(true);
                }
                return v;
            }
        };
        viewPager.setAdapter(fm);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if(position==1){
                    CartFragment cfg = (CartFragment) fm.getFragment(position);
                    cfg.loadCart();
                }else if(position==2){
                    if(App.newOrderSuccess){
                        App.newOrderSuccess=false;
                        MineFragment mfg = (MineFragment) fm.getFragment(position);
                        mfg.curOrders = null;
                        mfg.ordersAdapter.setOrders(null);
                        mfg.loadOrders(1);
                    }
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        tabLayout.setupWithViewPager(viewPager);
        View tabView;
        for(int i=0;i<tabLayout.getTabCount();i++){
            tabView = fm.getTabView(i);
            if(i==1){cartTabView=tabView;}
            tabLayout.getTabAt(i).setCustomView(tabView);
        }
        CLog.d("pastTime",App.pastTime("MainActivity.onCreate.end"));
    }

    @Override
    public void onBackPressed() {
        SwipeRefreshLayout srlg = (SwipeRefreshLayout) viewPager.findViewById(R.id.swipeRefreshLayoutGood);
        SwipeRefreshLayout srlt = (SwipeRefreshLayout) viewPager.findViewById(R.id.swipeRefreshLayoutType);
        if(srlg!=null && srlg.getVisibility()== View.VISIBLE){
            srlg.setVisibility(View.GONE);
            srlt.setVisibility(View.VISIBLE);
        }else{
            super.onBackPressed();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void openLogin(){
        Intent intent = new Intent(App.getContext(),LoginActivity.class);
        startActivity(intent);
    }
}