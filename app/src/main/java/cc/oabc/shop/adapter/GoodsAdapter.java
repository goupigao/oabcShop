package cc.oabc.shop.adapter;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Good;
import cc.oabc.shop.module.home.OnListClickListener;

/**
 * Created by Administrator on 2016/7/31.
 */
public class GoodsAdapter extends RecyclerView.Adapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;
    public Boolean hasNextPage;
    List<Good> goods;
    private OnListClickListener mListener;

    public GoodsAdapter(OnListClickListener l)
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goods_grid_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goods_grid_foot, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            final Good good = goods.get(position);
            itemViewHolder.description.setText(good.name);
            itemViewHolder.price.setText(good.price.toString());
            itemViewHolder.price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
            itemViewHolder.couponPrice.setText(good.couponPrice.toString());
            Glide.with(holder.itemView.getContext()).load(good.image_url).into(itemViewHolder.image);
            itemViewHolder.addToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnAddToCartClick(good.id,good.name,good.couponPrice,view);
                }
            });
        }else if(holder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            if(hasNextPage){
                footViewHolder.progressBar.setVisibility(View.VISIBLE);
                footViewHolder.footer.setText(R.string.loading);
            }else{
                footViewHolder.progressBar.setVisibility(View.GONE);
                footViewHolder.footer.setText(R.string.nomore);
            }
        }
    }

    @Override
    public int getItemCount() {
        return goods == null ? 0 : goods.size()+1;
    }

    public void setGoods(List<Good> goods) {
        this.goods = goods;
        notifyDataSetChanged();
    }

    public void addGoods(List<Good> goods) {
        List<Good> listFinal = new ArrayList<Good>();
        listFinal.addAll(this.goods);
        listFinal.addAll(goods);
        this.goods = listFinal;
        notifyDataSetChanged();
    }

    public List<Good> getGoods(){
        return goods;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.description) TextView description;
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.couponPrice) TextView couponPrice;
        @BindView(R.id.addToCart) Button addToCart;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progressBar) ProgressBar progressBar;
        @BindView(R.id.footer) TextView footer;
        public FootViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
