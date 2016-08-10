package cc.oabc.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.App;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Cart;
import cc.oabc.shop.module.cart.OnListClickListener;

/**
 * Created by Administrator on 2016/8/1.
 */
public class CartAdapter extends RecyclerView.Adapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;
    List<Cart> listCart;
    private OnListClickListener mListener;

    public CartAdapter(OnListClickListener l)
    {
        this.mListener=l;
    }

    @Override
    public int getItemViewType(int position) {
        Integer i = getItemCount();
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_grid_item, parent, false);
            return new ItemViewHolder(view);
        }else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_grid_foot, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            final Cart cart = listCart.get(position);
            itemViewHolder.name.setText(cart.name);
            itemViewHolder.price.setText("￥"+cart.couponPrice);
            itemViewHolder.count.setText(""+cart.count);
            itemViewHolder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifyCart(cart.goodId,Integer.parseInt(itemViewHolder.count.getText().toString())-1);
                }
            });
            itemViewHolder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifyCart(cart.goodId,Integer.parseInt(itemViewHolder.count.getText().toString())+1);
                }
            });
            itemViewHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemViewHolder.editcount.setVisibility(View.VISIBLE);
                    itemViewHolder.count.setVisibility(View.GONE);
                    itemViewHolder.editcount.setText(itemViewHolder.count.getText());
                    itemViewHolder.editcount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(itemViewHolder.editcount,0);
                    itemViewHolder.edit.setVisibility(View.GONE);
                    itemViewHolder.apply.setVisibility(View.VISIBLE);
                    itemViewHolder.minus.setVisibility(View.INVISIBLE);
                    itemViewHolder.add.setVisibility(View.INVISIBLE);
                }
            });
            itemViewHolder.apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemViewHolder.editcount.setVisibility(View.GONE);
                    itemViewHolder.editcount.clearFocus();
                    itemViewHolder.count.setVisibility(View.VISIBLE);
                    itemViewHolder.count.setText(itemViewHolder.editcount.getText());
                    itemViewHolder.edit.setVisibility(View.VISIBLE);
                    itemViewHolder.apply.setVisibility(View.GONE);
                    itemViewHolder.minus.setVisibility(View.VISIBLE);
                    itemViewHolder.add.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(itemViewHolder.editcount.getWindowToken(),0);
                    modifyCart(cart.goodId,Integer.parseInt(itemViewHolder.count.getText().toString()));
                }
            });
        }else if(holder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            if(listCart.size()>0){
                Float totalPrice=0F;
                for(Cart cart:listCart){
                    totalPrice+=cart.count*cart.couponPrice;
                }
                footViewHolder.listCart.setVisibility(View.VISIBLE);
                footViewHolder.haveNoGoodsInCart.setVisibility(View.GONE);
                footViewHolder.totalPrice.setText("￥"+totalPrice);
                footViewHolder.purchase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.OnPurchaseClick();
                    }
                });
            }else{
                footViewHolder.listCart.setVisibility(View.GONE);
                footViewHolder.haveNoGoodsInCart.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return listCart == null ? 0 : listCart.size()+1;
    }

    public  List<Cart> getCart(){
        return listCart;
    }

    public void setCart(List<Cart> listCart) {
        this.listCart = listCart;
        notifyDataSetChanged();
    }

    private void modifyCart(int goodId,int count){
        if (count>=0){
            mListener.OnCountChanged(goodId,count);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name) TextView name;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.minus) Button minus;
        @BindView(R.id.add) Button add;
        @BindView(R.id.count) TextView count;
        @BindView(R.id.editcount) EditText editcount;
        @BindView(R.id.edit) Button edit;
        @BindView(R.id.apply) Button apply;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.listCart) LinearLayout listCart;
        @BindView(R.id.totalPrice) TextView totalPrice;
        @BindView(R.id.purchase) Button purchase;
        @BindView(R.id.haveNoGoodsInCart) TextView haveNoGoodsInCart;
        public FootViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}