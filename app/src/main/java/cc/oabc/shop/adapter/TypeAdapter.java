package cc.oabc.shop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.model.Type;

import cc.oabc.shop.R;

/**
 * Created by Administrator on 2016/7/30.
 */
public class TypeAdapter extends RecyclerView.Adapter {
    List<Type> types;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.type_grid_item, parent, false);
        return new DebounceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DebounceViewHolder debounceViewHolder = (DebounceViewHolder) holder;
        Type type = types.get(position);
        debounceViewHolder.description.setText(type.name);
        debounceViewHolder.typeId.setText(""+type.id);
    }

    @Override
    public int getItemCount() {
        return types == null ? 0 : types.size();
    }

    public void setTypes(List<Type> types) {
        this.types = types;
        notifyDataSetChanged();
    }

    static class DebounceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.description) TextView description;
        @BindView(R.id.typeId) TextView typeId;
        public DebounceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
