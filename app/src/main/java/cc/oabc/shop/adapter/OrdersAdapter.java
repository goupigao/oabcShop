package cc.oabc.shop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Order;
import cc.oabc.shop.model.Orders;

/**
 * Created by Administrator on 2016/8/7.
 */
public class OrdersAdapter extends RecyclerView.Adapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;
    public Boolean hasNextPage;
    Orders orders;

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_grid_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_grid_foot, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            final Order order = orders.orders.get(position);
            itemViewHolder.orderNo.setText("订单号："+order.orderNo);
            itemViewHolder.createTime.setText("下单时间："+order.createTime);
            itemViewHolder.deliveryTime.setText("配送时间："+order.deliveryTime);
            itemViewHolder.orderPrice.setText("订单金额："+order.orderPrice);
            itemViewHolder.orderState.setText("订单状态："+order.orderState);
        } else if (holder instanceof FootViewHolder) {
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
        return orders == null ? 0 : orders.orders.size()+1;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    public void addOrders(Orders orders) {
        List<Order> listFinal = new ArrayList<Order>();
        listFinal.addAll(this.orders.orders);
        listFinal.addAll(orders.orders);
        orders.orders = listFinal;
        this.orders = orders;
        notifyDataSetChanged();
    }

    public Orders getOrders(){
        return orders;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.orderNo) TextView orderNo;
        @BindView(R.id.createTime) TextView createTime;
        @BindView(R.id.deliveryTime) TextView deliveryTime;
        @BindView(R.id.orderPrice) TextView orderPrice;
        @BindView(R.id.orderState) TextView orderState;
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
