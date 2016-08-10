package cc.oabc.shop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.App;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Address;
import cc.oabc.shop.model.Cart;
import cc.oabc.shop.model.PurchaseForm;

/**
 * Created by Administrator on 2016/8/5.
 */
public class PurchaseAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TOP = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;
    PurchaseForm purchaseForm;

    @Override
    public int getItemViewType(int position) {
        Integer i = getItemCount();
        if(position==0){
            return TYPE_TOP;
        }else if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TOP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_grid_top, parent, false);
            return new TopViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_grid_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_grid_foot, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TopViewHolder) {
            TopViewHolder topViewHolder = (TopViewHolder) holder;
            Boolean hasSelectAddress=false;
            for (Address address:purchaseForm.addressList){
                if(address.addressId.equals(purchaseForm.selectAddressId)){
                    topViewHolder.completeAddress.setText(address.completeAddress);
                    topViewHolder.completeContact.setText(address.completeContact);
                    hasSelectAddress=true;
                    break;
                }
            }
            if(!hasSelectAddress){
                topViewHolder.completeAddress.setText(App.getInstance().getResources().getString(R.string.pleaseSelectAddress));
                topViewHolder.completeContact.setText("");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(App.getContext(), R.layout.simple_spinner_item, purchaseForm.timeList);
            adapter.setDropDownViewResource(R.layout.my_drop_down_item);
            topViewHolder.spinner.setAdapter(adapter);
            for(int i=0;i<purchaseForm.timeList.size();i++){
                if(purchaseForm.timeList.get(i).equals(purchaseForm.selectTime)){
                    topViewHolder.spinner.setSelection(i,true);
                }
            }
            topViewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
                    App.selectTime = ((TextView)arg1).getText().toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {}
            });
        }else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            Cart cart = purchaseForm.cartList.get(position-1);
            itemViewHolder.name.setText(cart.name);
            itemViewHolder.guige.setText(cart.guige);
            itemViewHolder.countAndPrice.setText(cart.count+" × ￥"+cart.couponPrice);
        }else if (holder instanceof FootViewHolder){
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            footViewHolder.totalPrice.setText("￥"+purchaseForm.totalPrice.toString());
        }
    }

    @Override
    public int getItemCount() {
        return purchaseForm == null ? 0 : purchaseForm.cartList.size()+2;
    }

    public void setPurchaseForm(PurchaseForm purchaseForm) {
        this.purchaseForm = purchaseForm;
        notifyDataSetChanged();
    }

    public void setSelectAddressId(Integer addressId){
        this.purchaseForm.selectAddressId = addressId;
        notifyDataSetChanged();
    }

    public PurchaseForm getPurchaseForm(){
        return purchaseForm;
    }

    static class TopViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.select_time) Spinner spinner;
        @BindView(R.id.completeAddress) TextView completeAddress;
        @BindView(R.id.completeContact) TextView completeContact;
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

    static class FootViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.totalPrice) TextView totalPrice;
        public FootViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
