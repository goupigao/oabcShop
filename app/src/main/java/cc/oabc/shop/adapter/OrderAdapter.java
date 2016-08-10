package cc.oabc.shop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Cart;
import cc.oabc.shop.model.Order;

/**
 * Created by Administrator on 2016/8/7.
 */
public class OrderAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TOP = 0;
    public static final int TYPE_ITEM = 1;
    Order order;

    @Override
    public int getItemViewType(int position) {
        Integer i = getItemCount();
        if (position==0) {
            return TYPE_TOP;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TOP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_grid_top, parent, false);
            return new TopViewHolder(view);
        }else if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_grid_item, parent, false);
            return new ItemViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TopViewHolder) {
            TopViewHolder topViewHolder = (TopViewHolder) holder;
            topViewHolder.orderNo.setText("订单号："+order.orderNo);
            topViewHolder.orderPrice.setText("订单金额："+order.orderPrice);
            topViewHolder.payPrice.setText("实付金额："+order.payPrice);
            topViewHolder.createTime.setText("下单时间："+order.createTime);
            topViewHolder.deliveryTime.setText("配送时间："+order.deliveryTime);
            topViewHolder.orderState.setText("订单状态："+order.orderState);
            topViewHolder.name.setText("收件人姓名："+order.name);
            topViewHolder.address.setText("详细地址："+order.address);
            topViewHolder.postCode.setText("邮政编码："+order.postCode);
            topViewHolder.mobile.setText("手机号码："+order.mobile);
            topViewHolder.phone.setText("电话号码："+order.phone);
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            Cart cart = order.carts.get(position-1);
            itemViewHolder.name.setText(cart.name);
            itemViewHolder.guige.setText(cart.guige);
            itemViewHolder.countAndPrice.setText(cart.count+" × ￥"+cart.couponPrice);
        }
    }

    @Override
    public int getItemCount() {
        return order == null ? 0 : (order.carts==null ? 1 : order.carts.size()+1);
    }

    public void setOrder(Order order) {
        this.order = order;
        notifyDataSetChanged();
    }

    static class TopViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.orderNo) TextView orderNo;
        @BindView(R.id.orderPrice) TextView orderPrice;
        @BindView(R.id.payPrice) TextView payPrice;
        @BindView(R.id.createTime) TextView createTime;
        @BindView(R.id.deliveryTime) TextView deliveryTime;
        @BindView(R.id.orderState) TextView orderState;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.address) TextView address;
        @BindView(R.id.postCode) TextView postCode;
        @BindView(R.id.mobile) TextView mobile;
        @BindView(R.id.phone) TextView phone;
        public TopViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name) TextView name;
        @BindView(R.id.guige) TextView guige;
        @BindView(R.id.countAndPrice) TextView countAndPrice;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
