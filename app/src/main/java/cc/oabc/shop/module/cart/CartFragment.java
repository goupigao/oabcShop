package cc.oabc.shop.module.cart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.App;
import cc.oabc.shop.BaseFragment;
import cc.oabc.shop.MainActivity;
import cc.oabc.shop.R;
import cc.oabc.shop.adapter.CartAdapter;
import cc.oabc.shop.data.CartPrefs;
import cc.oabc.shop.model.Cart;
import cc.oabc.shop.network.Network;
import cc.oabc.shop.util.CLog;
import cc.oabc.shop.PurchaseActivity;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/30.
 */
public class CartFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    MainActivity mainActivity;
    ProgressDialog progressDialog;

    CartAdapter cartAdapter = new CartAdapter(new OnListClickListener(){
        @Override
        public void OnCountChanged(int goodId,int count){
            modifyCart(goodId,count);
        }
        @Override
        public void OnPurchaseClick(){
            purchase();
        }
    });
    Observer<String> observerCart = new Observer<String>() {
        @Override
        public void onNext(String response) {
            if(progressDialog!=null){progressDialog.dismiss();}
            Intent intent = new Intent(App.getContext(),PurchaseActivity.class);
            intent.putExtra("response",response);
            startActivity(intent);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CLog.d("pastTime",App.pastTime("CartFragment.onCreateView.begin"));
        View view;
        if(rootView!=null){
            view=rootView;
        }else{
            view = inflater.inflate(R.layout.fragment_cart, container, false);
            ButterKnife.bind(this, view);
            mainActivity = (MainActivity) getActivity();
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            recyclerView.setAdapter(cartAdapter);
            rootView=view;
        }
        CLog.d("pastTime",App.pastTime("CartFragment.onCreateView.end"));
        return view;
    }

    @Override
    public void onStart() {
        CLog.d("pastTime",App.pastTime("CartFragment.onStart.begin"));
        super.onStart();
        loadCart();
        CLog.d("pastTime",App.pastTime("CartFragment.onStart.end"));
    }

    public void loadCart(){
        CartPrefs cartPrefs = new CartPrefs(App.getContext());
        List<Cart> listCart = cartPrefs.getListCart();
        cartAdapter.setCart(listCart);
    }

    public void  modifyCart(final int goodId,final int count){
        CartPrefs cartPrefs = new CartPrefs(App.getContext());
        if(!cartPrefs.getGoodCount(goodId).equals(count)){
            cartPrefs.setGood(goodId,count);
            List<Cart> listCart = cartPrefs.getListCart();
            cartAdapter.setCart(listCart);
        }
    }

    public void purchase(){
        if(!App.logged){
            mainActivity.openLogin();
        }else{
            progressDialog = new ProgressDialog(mainActivity);
            progressDialog.setTitle("正在同步到云端");
            progressDialog.setCancelable(false);
            progressDialog.show();
            CartPrefs cartPrefs = new CartPrefs(App.getContext());
            List<Cart> listCart = cartPrefs.getListCart();
            subscription = Network.getCartObservale(listCart)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observerCart);
        }
    }
}
