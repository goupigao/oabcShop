package cc.oabc.shop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.adapter.AddressAdapter;
import cc.oabc.shop.adapter.PurchaseAdapter;
import cc.oabc.shop.data.CartPrefs;
import cc.oabc.shop.model.Address;
import cc.oabc.shop.model.PurchaseForm;
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
 * Created by Administrator on 2016/8/5.
 */
public class PurchaseActivity extends AppCompatActivity {
    @BindView(R.id.recyclerViewPurchase) RecyclerView recyclerViewPurchase;
    @BindView(R.id.recyclerViewAddress) RecyclerView recyclerViewAddress;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.address_name) EditText address_name;
    @BindView(R.id.address_address) EditText address_address;
    @BindView(R.id.address_mobile) EditText address_mobile;
    @BindView(R.id.address_phone) EditText address_phone;
    @BindView(R.id.address_province) Spinner address_province;
    @BindView(R.id.address_city) Spinner address_city;
    @BindView(R.id.address_zone) Spinner address_zone;
    @BindView(R.id.orderSuccess) RelativeLayout orderSuccess;
    @BindView(R.id.cardMoney) TextView cardMoney;
    @BindView(R.id.deliveryTime) TextView deliveryTime;
    @BindView(R.id.orderNo) TextView orderNo;
    @BindView(R.id.viewDetail) Button viewDetail;
    protected Subscription subscription;
    ProgressDialog progressDialog;

    PurchaseAdapter purchaseAdapter = new PurchaseAdapter();
    AddressAdapter addressAdapter = new AddressAdapter(new OnAddressClickListener() {
        @Override
        public void onSelectAddressClick(View view,Integer addressId) {
            recyclerViewAddress.setVisibility(View.GONE);
            recyclerViewPurchase.setVisibility(View.VISIBLE);
            setActionBarTitle(getResources().getString(R.string.purchase));
            if(App.selectAddressId==null || !App.selectAddressId.equals(addressId)){
                App.selectAddressId = addressId;
                purchaseAdapter.setSelectAddressId(addressId);
                addressAdapter.setSelectAddressId(addressId);
            }
        }
        @Override
        public void onEditAddressClick(Integer position){
            editAddress(position);
        }
        public void onDeleteAddressClick(Integer position){
            deleteAddress(position);
        }
    });

    Observer<String> observerPovince = new Observer<String>() {
        @Override
        public void onNext(String response) {
            if(response.contains("OKOK")){
                String[] cityArray = TextUtils.split(response, "\\|");
                cityArray[0] = "请选择";
                setAddressSpinner(address_city,Arrays.asList(cityArray),"");
            }
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    Observer<String> observerCity = new Observer<String>() {
        @Override
        public void onNext(String response) {
            if(response.contains("OKOK")){
                String[] zoneArray = TextUtils.split(response, "\\|");
                zoneArray[0] = "请选择";
                setAddressSpinner(address_zone,Arrays.asList(zoneArray),"");
            }
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    Observer<PurchaseForm> observerPurchase = new Observer<PurchaseForm>() {
        @Override
        public void onNext(PurchaseForm purchaseForm) {
            if(progressDialog!=null){progressDialog.dismiss();}
            if(purchaseForm.cardMoney!=null){
                //已经下单成功
                setActionBarTitle(getResources().getString(R.string.orderSuccess));
                cardMoney.setText("卡内余额：￥"+purchaseForm.cardMoney.toString());
                deliveryTime.setText("预计配送时间："+purchaseForm.deliveryTime);
                orderNo.setText("订单号："+purchaseForm.orderNo);
                recyclerViewPurchase.setVisibility(View.GONE);
                orderSuccess.setVisibility(View.VISIBLE);
                CartPrefs cartPrefs = new CartPrefs(App.getContext());
                cartPrefs.removeAll();
                final String viewState = purchaseForm.viewState;
                final String eventValidation = purchaseForm.eventValidation;
                viewDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(App.getContext(), OrderActivity.class);
                        intent.putExtra("viewState",viewState);
                        intent.putExtra("eventValidation",eventValidation);
                        startActivity(intent);
                        finish();
                    }
                });
                App.newOrderSuccess=true;
                return;
            }
            purchaseForm = setSelectAddressAndTime(purchaseForm);
            if(purchaseForm.editAddressResult!=null) {
                if ("操作成功".equals(purchaseForm.editAddressResult)) {
                    //新增或编辑地址成功
                    scrollView.setVisibility(View.GONE);
                    recyclerViewAddress.setVisibility(View.VISIBLE);
                    setActionBarTitle(getResources().getString(R.string.selectAddress));
                } else {
                    //新增或编辑地址失败
                    Toast.makeText(App.getContext(), purchaseForm.editAddressResult, Toast.LENGTH_SHORT).show();
                }
            }else if(purchaseForm.orderResult!=null){
                //下单失败
                Toast.makeText(App.getContext(), purchaseForm.orderResult, Toast.LENGTH_LONG).show();
            }else{
                recyclerViewAddress.setVisibility(View.GONE);
                if (purchaseForm.provinceList.size()>0){
                    //打开新增、编辑地址的界面
                    recyclerViewPurchase.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    setActionBarTitle(getResources().getString(purchaseForm.isExistAddress?R.string.editAddress:R.string.addAddress));
                    address_name.setText(purchaseForm.name);
                    address_address.setText(purchaseForm.address);
                    address_mobile.setText(purchaseForm.mobile);
                    address_phone.setText(purchaseForm.phone);
                    setAddressSpinner(address_province,purchaseForm.provinceList,purchaseForm.province);
                    address_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
                            subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/MyHandler.ashx?myCmmd=getProvinceCity&province="+((TextView)arg1).getText().toString())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(observerPovince);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {}
                    });
                    setAddressSpinner(address_city,purchaseForm.cityList,purchaseForm.city);
                    address_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
                            subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/MyHandler.ashx?myCmmd=getCityZone2&province="+address_province.getSelectedItem()+"&city="+((TextView)arg1).getText().toString())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(observerCity);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {}
                    });
                    setAddressSpinner(address_zone,purchaseForm.zoneList,purchaseForm.zone);
                }else{
                    //打开下单的页面
                    scrollView.setVisibility(View.GONE);
                    recyclerViewPurchase.setVisibility(View.VISIBLE);
                    setActionBarTitle(getResources().getString(R.string.purchase));
                }
            }
            purchaseAdapter.setPurchaseForm(purchaseForm);
            addressAdapter.setPurchaseForm(purchaseForm);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    Observer<PurchaseForm> observerDeleteAddress = new Observer<PurchaseForm>() {
        @Override
        public void onNext(PurchaseForm purchaseForm) {
            if(progressDialog!=null){progressDialog.dismiss();}
            purchaseForm = setSelectAddressAndTime(purchaseForm);
            if(App.selectAddressId==null){
                ((TextView) findViewById(R.id.completeAddress)).setText(getResources().getString(R.string.pleaseSelectAddress));
                ((TextView) findViewById(R.id.completeContact)).setText("");
            }
            purchaseAdapter.setPurchaseForm(purchaseForm);
            addressAdapter.setPurchaseForm(purchaseForm);
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
        setContentView(R.layout.activity_purchase);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        recyclerViewPurchase.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerViewPurchase.setAdapter(purchaseAdapter);
        recyclerViewAddress.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerViewAddress.setAdapter(addressAdapter);
        Intent intent = getIntent();
        loadPurchase(intent.getStringExtra("response"));
    }

    public void loadPurchase(String response){
        Observable observable = TextUtils.isEmpty(response)?Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx"): Observable.just(response);
        subscription = observable
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerPurchase);
    }

    public void addAddress(View view){
        showProgressDialog("请稍等");
        subscription = Network.getAddAddressObservale()
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerPurchase);
    }

    public void editAddress(Integer position){
        showProgressDialog("请稍等");
        subscription = Network.getEditAddressObservale(position)
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerPurchase);
    }

    public void saveAddress(View view){
        showProgressDialog("正在保存地址");
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(address_name.getWindowToken(),0);
        FormBody.Builder fbBuilder = new FormBody.Builder();
        fbBuilder.add("__VIEWSTATE",purchaseAdapter.getPurchaseForm().viewState)
                .add("__EVENTVALIDATION", purchaseAdapter.getPurchaseForm().eventValidation)
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$TBX_Name", address_name.getText().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$pcz$DDL_Province", address_province.getSelectedItem().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$pcz$sel_city", address_city.getSelectedItem().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$pcz$sel_zone", address_zone.getSelectedItem().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$TBX_Address", address_address.getText().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$TBX_Mobile", address_mobile.getText().toString())
                .add("ctl00$ContentPlaceHolder1$ucReceiverAddrEdit1$TBX_Phone", address_phone.getText().toString())
                .add("ctl00$ContentPlaceHolder1$TBN_SetAddress.x", "52")
                .add("ctl00$ContentPlaceHolder1$TBN_SetAddress.y", "29");
        if(!purchaseAdapter.getPurchaseForm().isExistAddress){
            fbBuilder.add("ctl00$ContentPlaceHolder1$Chk_AddNewAddr","on");
        }
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx",fbBuilder.build())
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerPurchase);
    }

    public void deleteAddress(Integer position){
        showProgressDialog("正在删除地址");
        subscription = Network.getDeleteAddressObservale(position)
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerDeleteAddress);
    }

    public void order(View view){
        if(App.selectAddressId==null){
            Toast.makeText(App.getContext(),getResources().getString(R.string.pleaseSelectAddress),Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog("正在下单");
        RequestBody body = new FormBody.Builder()
                .add("__VIEWSTATE",purchaseAdapter.getPurchaseForm().viewState)
                .add("__EVENTVALIDATION", purchaseAdapter.getPurchaseForm().eventValidation)
                .add("ctl00$ContentPlaceHolder1$HDF_SelConsigneeRecID",App.selectAddressId.toString())
                .add("rbnSelectedAddress",App.selectAddressId.toString())
                .add("ctl00$ContentPlaceHolder1$DDL_Date",((Spinner) findViewById(R.id.select_time)).getSelectedItem().toString().substring(0,10))
                .add("ctl00$ContentPlaceHolder1$Btn_ConfirmOrder.x","52")
                .add("ctl00$ContentPlaceHolder1$Btn_ConfirmOrder.y","21")
                .build();
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx",body)
                .subscribeOn(Schedulers.io())
                .map(Api.getPurchaseFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerPurchase);
    }

    public void showProgressDialog(String string){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(string);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void openSelectAddress(View view){
        recyclerViewPurchase.setVisibility(View.GONE);
        recyclerViewAddress.setVisibility(View.VISIBLE);
        setActionBarTitle(getResources().getString(R.string.selectAddress));
    }

    public void setAddressSpinner(Spinner spinner,List<String> cityList,String city){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(App.getContext(), R.layout.simple_spinner_item, cityList);
        adapter.setDropDownViewResource(R.layout.my_drop_down_item);
        spinner.setAdapter(adapter);
        for(int i=0;i<cityList.size();i++){
            if(cityList.get(i).equals(city)){
                spinner.setOnItemSelectedListener(null);
                spinner.setSelection(i,true);
                break;
            }
        }
    }

    public PurchaseForm setSelectAddressAndTime(PurchaseForm purchaseForm){
        Boolean addressExisted = false;
        if (App.selectAddressId!=null){
            for(Address address:purchaseForm.addressList){
                if(App.selectAddressId.equals(address.addressId)){
                    addressExisted = true;
                    break;
                }
            }
        }
        if(!addressExisted){
            App.selectAddressId = purchaseForm.selectAddressId;
        }else{
            purchaseForm.selectAddressId = App.selectAddressId;
        }
        if(App.selectTime==null || !purchaseForm.timeList.contains(App.selectTime)){
            App.selectTime = purchaseForm.selectTime;
        }else{
            purchaseForm.selectTime = App.selectTime;
        }
        return purchaseForm;
    }

    @Override
    public void onBackPressed() {
        if(scrollView.getVisibility()==View.VISIBLE){
            scrollView.setVisibility(View.GONE);
            recyclerViewAddress.setVisibility(View.VISIBLE);
            setActionBarTitle(getResources().getString(R.string.selectAddress));
        }else if(recyclerViewAddress.getVisibility()== View.VISIBLE){
            recyclerViewAddress.setVisibility(View.GONE);
            recyclerViewPurchase.setVisibility(View.VISIBLE);
            setActionBarTitle(getResources().getString(R.string.purchase));
        }else{
            super.onBackPressed();
        }
    }

    public void setActionBarTitle(String title){
        try{
            getSupportActionBar().setTitle(title);
        }catch (Exception e){
            e.printStackTrace();
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
