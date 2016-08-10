package cc.oabc.shop.module.cart;

/**
 * Created by Administrator on 2016/8/1.
 */
public interface OnListClickListener {
    public void OnCountChanged(int goodId,int count);
    public void OnPurchaseClick();
}
