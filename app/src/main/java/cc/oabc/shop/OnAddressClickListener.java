package cc.oabc.shop;

import android.view.View;

/**
 * Created by Administrator on 2016/8/5.
 */
public interface OnAddressClickListener {
    public void onSelectAddressClick(View view,Integer addressId);
    public void onEditAddressClick(Integer position);
    public void onDeleteAddressClick(Integer position);
}
