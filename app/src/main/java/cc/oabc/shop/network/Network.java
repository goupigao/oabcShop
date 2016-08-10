package cc.oabc.shop.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;

import cc.oabc.shop.App;
import cc.oabc.shop.R;
import cc.oabc.shop.model.Cart;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2016/7/30.
 */
public class Network {
    public static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient getClient(){
        return new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final PersistentCookieStore cookieStore = new PersistentCookieStore(App.getContext());
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        if (cookies != null && cookies.size() > 0) {
                            for (Cookie item : cookies) {
                                if(item.domain().equals("shop.oabc.cc") && item.name().equals("provinceName")){//将provinceName从会话cookie转为持久cookie
                                    item = new Cookie.Builder().hostOnlyDomain("shop.oabc.cc").name("provinceName").value(item.value()).build();
                                }
                                cookieStore.add(url, item);
                            }
                        }
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies;
                    }
                })
                .build();
    }

    public static Observable getObservale(final String url){//返回一个Observable对象，用于GET方法访问网页
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getObservale(final String url,final RequestBody body){//返回一个Observable对象，用于POST方法访问网页
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    //RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{'Select1':'杭州市'}");
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getImageObservale(final String url){//返回一个Observable对象，用于访问网络图片
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                try{
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = getClient().newCall(request).execute();
                    if (response.isSuccessful()){
                        InputStream input = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        subscriber.onNext(bitmap);
                    }
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getAddAddressObservale(){//返回一个Observable对象，用于打开新增地址的页面
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    Request request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .build();
                    Response response = getClient().newCall(request).execute();
                    Matcher m = MyPattern.getFormDataPattern().matcher(response.body().string());
                    m.find();
                    RequestBody body = new FormBody.Builder()
                            .add("__VIEWSTATE", m.group(1).trim())
                            .add("__EVENTVALIDATION", m.group(2).trim())
                            .add("ctl00$ContentPlaceHolder1$Chk_AddNewAddr","on")
                            .build();
                    request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .post(body)
                            .build();
                    response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getEditAddressObservale(final Integer position){//返回一个Observable对象，用于打开编辑地址的页面
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    Request request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .build();
                    Response response = getClient().newCall(request).execute();
                    Matcher m = MyPattern.getFormDataPattern().matcher(response.body().string());
                    m.find();
                    RequestBody body = new FormBody.Builder()
                            .add("__VIEWSTATE", m.group(1).trim())
                            .add("__EVENTVALIDATION", m.group(2).trim())
                            .add("ctl00$ContentPlaceHolder1$RPT1$ctl"+(position<10?"0"+position:position)+"$BTN_Edit","修改")
                            .build();
                    request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .post(body)
                            .build();
                    response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getDeleteAddressObservale(final Integer position){//返回一个Observable对象，用于删除一个地址
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    Request request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .build();
                    Response response = getClient().newCall(request).execute();
                    Matcher m = MyPattern.getFormDataPattern().matcher(response.body().string());
                    m.find();
                    RequestBody body = new FormBody.Builder()
                            .add("__VIEWSTATE", m.group(1).trim())
                            .add("__EVENTVALIDATION", m.group(2).trim())
                            .add("ctl00$ContentPlaceHolder1$RPT1$ctl"+(position<10?"0"+position:position)+"$BTN_Delete","删除")
                            .build();
                    request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .post(body)
                            .build();
                    response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable getCartObservale(final List<Cart> listCart){//返回一个Observable对象，用于将本地购物车的产品同步至网络上
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    String citys[] = App.getInstance().getResources().getStringArray(R.array.meinv);
                    PersistentCookieStore cookieStore = new PersistentCookieStore(App.getContext());
                    Cookie cookieProvinceName = cookieStore.getCookie(HttpUrl.parse("http://shop.oabc.cc:88/zgMBFrontShopV2/"),"provinceName");
                    String city = cookieProvinceName.value();
                    city = URLDecoder.decode(city,"utf-8");
                    String tempCity=citys[0];
                    for (int i=0;i<citys.length;i++){
                        if(city.equals(citys[i])){
                            tempCity = i==0?citys[1]:citys[0];
                            break;
                        }
                    }
                    cookieStore.add(HttpUrl.parse("http://shop.oabc.cc:88/zgMBFrontShopV2/"), new Cookie.Builder().hostOnlyDomain("shop.oabc.cc").name("provinceName").value(URLEncoder.encode(tempCity,"utf-8").toLowerCase()).build());
                    Request request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingCart.aspx")
                            .build();
                    Response response = getClient().newCall(request).execute();
                    Matcher m = MyPattern.getFormDataPattern().matcher(response.body().string());
                    m.find();
                    RequestBody body = new FormBody.Builder()
                            .add("__EVENTTARGET", "ctl00$UcHeader1$ucHeaderNavigator_Member1$DDL_DeliverCity")
                            .add("__VIEWSTATE", m.group(1).trim())
                            .add("__EVENTVALIDATION", m.group(2).trim())
                            .add("ctl00$UcHeader1$ucHeaderNavigator_Member1$DDL_DeliverCity", city)
                            .build();
                    request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingCart.aspx")
                            .post(body)
                            .build();
                    getClient().newCall(request).execute();
                    for (Cart cart:listCart){
                        request = new Request.Builder()
                                .url("http://shop.oabc.cc:88/zgMBFrontShopV2/Purchase.aspx?call=myajax&type=single&supplyId="+cart.goodId+"&itemCount="+cart.count)
                                .build();
                        getClient().newCall(request).execute();
                    }
                    request = new Request.Builder()
                            .url("http://shop.oabc.cc:88/zgMBFrontShopV2/ShoppingConfirm.aspx")
                            .build();
                    response = getClient().newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }catch (Throwable e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }
}
