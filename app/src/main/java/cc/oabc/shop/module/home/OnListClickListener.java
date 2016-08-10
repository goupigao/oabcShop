package cc.oabc.shop.module.home;

import android.view.View;

/**
 * Created by Administrator on 2016/7/31.
 */
public interface OnListClickListener {
    public void OnAddToCartClick(Integer id, String name, Float couponPrice, View view);
}
