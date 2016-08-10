package cc.oabc.shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.adapter.OrderAdapter;
import cc.oabc.shop.model.Order;
import cc.oabc.shop.network.Api;
import cc.oabc.shop.network.Network;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/7.
 */
public class OrderActivity extends AppCompatActivity {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    protected Subscription subscription;

    OrderAdapter orderAdapter = new OrderAdapter();
    Observer<Order> observerOrder = new Observer<Order>() {
        @Override
        public void onNext(Order order) {
            orderAdapter.setOrder(order);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(orderAdapter);
        orderAdapter.setOrder(new Order());
        Intent intent = getIntent();
        if(!TextUtils.isEmpty(intent.getStringExtra("response"))){
            subscription = Observable.just(intent.getStringExtra("response"))
                    .map(Api.getOrderFunc1())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observerOrder);
        }else if(intent.getIntExtra("recId",0)>0){
            subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/myDeliverDetail.aspx?RecID="+intent.getIntExtra("recId",0)+"&Sure=1")
                    .subscribeOn(Schedulers.io())
                    .map(Api.getOrderFunc1())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observerOrder);
        }else if(!TextUtils.isEmpty(intent.getStringExtra("viewState")) && !TextUtils.isEmpty(intent.getStringExtra("eventValidation"))){
            RequestBody body = new FormBody.Builder()
                    .add("__VIEWSTATE",intent.getStringExtra("viewState"))
                    .add("__EVENTVALIDATION", intent.getStringExtra("eventValidation"))
                    .add("ctl00$ContentPlaceHolder1$UcCmmDeliverOK$Btn_ViewDetail.x","75")
                    .add("ctl00$ContentPlaceHolder1$UcCmmDeliverOK$Btn_ViewDetail.y","7")
                    .build();
            subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/cmmResult.aspx",body)
                    .subscribeOn(Schedulers.io())
                    .map(Api.getOrderFunc1())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observerOrder);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }
}
