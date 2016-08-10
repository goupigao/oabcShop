package cc.oabc.shop;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

import cc.oabc.shop.model.FormData;
import cc.oabc.shop.network.Api;
import cc.oabc.shop.network.Network;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/8/1.
 */
public class LoginActivity extends AppCompatActivity {
    ImageView getGode;
    EditText userNameET,passwordET,getCodeET;
    String viewState="";
    String eventValidation="";
    protected Subscription subscription;
    ProgressDialog progressDialog;

    Observer<FormData> observerFormData = new Observer<FormData>() {
        @Override
        public void onNext(FormData formData) {
            viewState=formData.viewState;
            eventValidation=formData.eventValidation;
            loadGetCode(getGode);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    Observer<Bitmap> observerGetCode = new Observer<Bitmap>() {
        @Override
        public void onNext(Bitmap bitmap) {
            getGode.setImageBitmap(bitmap);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {}
    };

    Observer<String> observerLogin = new Observer<String>() {
        @Override
        public void onNext(String result) {
            if(progressDialog!=null){progressDialog.dismiss();}
            if (!"".equals(result)){
                Toast.makeText(App.getContext(),result,Toast.LENGTH_SHORT).show();
            }else{
                App.logged = true;
                finish();
            }
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
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getGode = (ImageView) findViewById(R.id.getGode);
        userNameET = (EditText) findViewById(R.id.userNameET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        getCodeET = (EditText) findViewById(R.id.getCodeET);
        initFormData();
    }

    public void  initFormData(){
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/zgcMBLogin.aspx")
                .subscribeOn(Schedulers.io())
                .map(Api.getFormDataFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerFormData);
    }

    public void loadGetCode(View view){
        String url = "http://shop.oabc.cc:88/zgMBFrontShopV2/VerifyImage.aspx?t="+new Random().nextFloat();
        subscription = Network.getImageObservale(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerGetCode);
        //Glide.with(this).load(url).into(getGode);
    }

    public void login(View view){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("正在登录");
        progressDialog.setCancelable(false);
        progressDialog.show();
        RequestBody body = new FormBody.Builder()
                .add("ucMBLogin1$UserName", userNameET.getText().toString())
                .add("ucMBLogin1$Password", passwordET.getText().toString())
                .add("ucMBLogin1$GetCode", getCodeET.getText().toString())
                .add("ucMBLogin1$LoginButton.x", 51+"")
                .add("ucMBLogin1$LoginButton.y", 16+"")
                .add("__VIEWSTATE", viewState)
                .add("__EVENTVALIDATION", eventValidation)
                .build();
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/zgcMBLogin.aspx",body)
                .subscribeOn(Schedulers.io())
                .map(Api.getLoginFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerLogin);
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
