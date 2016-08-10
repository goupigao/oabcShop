package cc.oabc.shop.module.mine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.App;
import cc.oabc.shop.BaseFragment;
import cc.oabc.shop.MainActivity;
import cc.oabc.shop.OrderActivity;
import cc.oabc.shop.R;
import cc.oabc.shop.adapter.OrdersAdapter;
import cc.oabc.shop.model.Info;
import cc.oabc.shop.model.Orders;
import cc.oabc.shop.module.home.RecyclerItemClickListener;
import cc.oabc.shop.network.Api;
import cc.oabc.shop.network.Network;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/1.
 */
public class MineFragment extends BaseFragment {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.username) TextView username;
    @BindView(R.id.cardMoney) TextView cardMoney;
    @BindView(R.id.loginAndLogout) Button loginAndLogout;
    Boolean hasNextPage=true;
    Boolean isLoading;
    public Orders curOrders;

    Observer<Info> observerInfo = new Observer<Info>() {
        @Override
        public void onNext(Info info) {
            if(info.userName!=null){
                username.setText("卡号："+info.userName.toString());
                cardMoney.setText("余额：￥"+info.cardMoney.toString());
                loginAndLogout.setText(getResources().getString(R.string.logout));
                App.logged = true;
            }else{
                username.setText("卡号：");
                cardMoney.setText("余额：");
                loginAndLogout.setText(getResources().getString(R.string.login));
                curOrders = null;
                ordersAdapter.setOrders(null);
                App.logged = false;
            }
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    public OrdersAdapter ordersAdapter = new OrdersAdapter();
    Observer<Orders> observerOrders = new Observer<Orders>() {
        @Override
        public void onNext(Orders orders) {
            hasNextPage = orders.hasNextPage;
            ordersAdapter.hasNextPage = hasNextPage;
            if(orders.page==1){
                ordersAdapter.setOrders(orders);
            }else{
                ordersAdapter.addOrders(orders);
            }
            curOrders = ordersAdapter.getOrders();
            isLoading = false;
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {isLoading=false;}
    };

    Observer<String> observerLogout = new Observer<String>() {
        @Override
        public void onNext(String response) {
            username.setText("卡号：");
            cardMoney.setText("余额：");
            loginAndLogout.setText(getResources().getString(R.string.login));
            curOrders = null;
            ordersAdapter.setOrders(null);
            App.logged = false;
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
        View view;
        if(rootView!=null){
            view=rootView;
        }else{
            view = inflater.inflate(R.layout.fragment_mine, container, false);
            ButterKnife.bind(this, view);
            loginAndLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((Button) view).getText().equals(getResources().getString(R.string.login))){
                        ((MainActivity) getActivity()).openLogin();
                    }else{
                        logout();
                    }
                }
            });
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(App.getContext(),
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(App.getContext(), OrderActivity.class);
                            intent.putExtra("recId",ordersAdapter.getOrders().orders.get(position).redId);
                            startActivity(intent);
                        }
                        @Override
                        public void onLongClick(View view, int position) {
                        }
                    }));
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition + 1 == ordersAdapter.getItemCount() && newState==RecyclerView.SCROLL_STATE_IDLE) {
                        if (!hasNextPage) {
                            ordersAdapter.notifyItemRemoved(ordersAdapter.getItemCount());
                            return;
                        }
                        if (!isLoading && ordersAdapter.getOrders()!=null) {
                            loadOrders(ordersAdapter.getOrders().page+1);
                        }
                    }
                }
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
            recyclerView.setAdapter(ordersAdapter);
            rootView=view;
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadInfo();
        if(curOrders == null){
            loadOrders(1);
        }
    }

    public void loadInfo(){
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/myBalance.aspx")
                .subscribeOn(Schedulers.io())
                .map(Api.getBalanceFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerInfo);
    }

    public void loadOrders(final Integer page){
        isLoading = true;
        Observable observable;
        if(page==1){
            observable = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/MyDeliver.aspx");
        }else{
            RequestBody body = new FormBody.Builder()
                    .add("__VIEWSTATE",ordersAdapter.getOrders().viewState)
                    .add("__EVENTVALIDATION", ordersAdapter.getOrders().eventValidation)
                    .add("__EVENTTARGET","ctl00$ContentPlaceHolder1$UC_Deliver1$myPager$DropDownList_PageIndex")
                    .add("ctl00$ContentPlaceHolder1$UC_Deliver1$myPager$DropDownList_PageIndex",page.toString())
                    .build();
            observable = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/MyDeliver.aspx",body);
        }
        subscription = observable.subscribeOn(Schedulers.io())
                .map(Api.getOrdersFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerOrders);
    }

    public void logout(){
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/MyLogout.aspx")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerLogout);
    }
}
