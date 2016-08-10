package cc.oabc.shop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.OnAddressClickListener;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Address;
import cc.oabc.shop.model.PurchaseForm;

/**
 * Created by Administrator on 2016/8/5.
 */
public class AddressAdapter extends RecyclerView.Adapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;
    PurchaseForm purchaseForm;
    private OnAddressClickListener mListener;

    public AddressAdapter(OnAddressClickListener l)
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_grid_item, parent, false);
            return new ItemViewHolder(view);
        }else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_grid_foot, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            Address address = purchaseForm.addressList.get(position);
            itemViewHolder.radioButton.setText(address.completeAddress+"\n"+address.completeContact);
            if(address.addressId.equals(purchaseForm.selectAddressId)){
                itemViewHolder.radioButton.setChecked(true);
            }else{
                itemViewHolder.radioButton.setChecked(false);
            }
            itemViewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSelectAddressClick(v,purchaseForm.addressList.get(position).addressId);
                }
            });
            itemViewHolder.editAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditAddressClick(position);
                }
            });

            itemViewHolder.deleteAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteAddressClick(position);
                }
            });
        }else if (holder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            if(getItemCount()>5){
                footViewHolder.cannotAddAddress.setVisibility(View.VISIBLE);
                footViewHolder.addAddress.setVisibility(View.GONE);
            }else{
                footViewHolder.cannotAddAddress.setVisibility(View.GONE);
                footViewHolder.addAddress.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setSelectAddressId(Integer addressId){
        this.purchaseForm.selectAddressId = addressId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return purchaseForm == null ? 0 : purchaseForm.addressList.size()+1;
    }

    public void setPurchaseForm(PurchaseForm purchaseForm) {
        this.purchaseForm = purchaseForm;
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.editAddress) Button editAddress;
        @BindView(R.id.deleteAddress) Button deleteAddress;
        @BindView(R.id.radioButton) RadioButton radioButton;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.addAddress) Button addAddress;
        @BindView(R.id.cannotAddAddress) TextView cannotAddAddress;
        public FootViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
