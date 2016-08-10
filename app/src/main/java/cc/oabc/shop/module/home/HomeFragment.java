package cc.oabc.shop.module.home;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.oabc.shop.App;
import cc.oabc.shop.BaseFragment;
import cc.oabc.shop.MainActivity;
import cc.oabc.shop.R;
import cc.oabc.shop.adapter.GoodsAdapter;
import cc.oabc.shop.adapter.TypeAdapter;
import cc.oabc.shop.data.CartPrefs;
import cc.oabc.shop.model.Good;
import cc.oabc.shop.model.Goods;
import cc.oabc.shop.model.Type;
import cc.oabc.shop.network.Api;
import cc.oabc.shop.network.Network;
import cc.oabc.shop.network.PersistentCookieStore;
import cc.oabc.shop.util.CLog;
import cc.oabc.shop.util.CartAnimation;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/30.
 */
public class HomeFragment extends BaseFragment {
    @BindView(R.id.field_item_spinner_content) Spinner spinner;
    @BindView(R.id.swipeRefreshLayoutType) SwipeRefreshLayout swipeRefreshLayoutType;
    @BindView(R.id.recyclerViewType) RecyclerView recyclerViewType;
    @BindView(R.id.swipeRefreshLayoutGood) SwipeRefreshLayout swipeRefreshLayoutGood;
    @BindView(R.id.recyclerViewGood) RecyclerView recyclerViewGood;
    Integer goodsPage=-1;
    String typeId;
    Boolean hasNextPage;
    Boolean isLoading;
    List<Type> curTypes;
    List<Good> curGoods;
    MainActivity mainActivity;
    AdapterView.OnItemSelectedListener spinnerOnItemSelectedListener;
    CartAnimation cartAnimation;

    TypeAdapter typeAdapter = new TypeAdapter();
    Observer<List<Type>> observerType = new Observer<List<Type>>() {
        @Override
        public void onNext(List<Type> types) {
            curTypes=types;
            typeAdapter.setTypes(types);
            swipeRefreshLayoutType.setRefreshing(false);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {
            swipeRefreshLayoutType.setRefreshing(false);
        }
    };

    GoodsAdapter goodsAdapter = new GoodsAdapter(new OnListClickListener(){
        @Override
        public void OnAddToCartClick(Integer id, String name, Float couponPrice, View view){
            CartPrefs cartPrefs = new CartPrefs(App.getContext());
            cartPrefs.setGood(id,name,couponPrice,cartPrefs.getGoodCount(id)+1);
            cartAnimation = new CartAnimation(getActivity());
            cartAnimation.plusOneAnim(view);
        }
    });
    Observer<Goods> observerGood = new Observer<Goods>() {
        @Override
        public void onNext(Goods goods) {
            hasNextPage = goods.hasNextPage;
            goodsAdapter.hasNextPage = hasNextPage;
            if(goods.page==0){
                goodsAdapter.setGoods(goods.goods);
            }else{
                goodsAdapter.addGoods(goods.goods);
            }
            curGoods= goodsAdapter.getGoods();
            isLoading=false;
            swipeRefreshLayoutGood.setRefreshing(false);
        }
        @Override
        public void onCompleted() {
        }
        @Override
        public void onError(Throwable e) {
            isLoading=false;
            swipeRefreshLayoutGood.setRefreshing(false);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CLog.d("pastTime",App.pastTime("HomeFragment.onCreateView.begin"));
        View view;
        if(rootView!=null){
            view=rootView;
        }else{
            view = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.bind(this, view);
            mainActivity = (MainActivity) getActivity();
            recyclerViewType.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            recyclerViewType.addOnItemTouchListener(new RecyclerItemClickListener(App.getContext(),
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            TextView typeIdTV = (TextView) view.findViewById(R.id.typeId);
                            swipeRefreshLayoutType.setVisibility(View.GONE);
                            swipeRefreshLayoutGood.setVisibility(View.VISIBLE);
                            goodsPage=0;
                            typeId = typeIdTV.getText().toString();
                            loadGoods(typeId,goodsPage);
                        }
                        @Override
                        public void onLongClick(View view, int position) {
                        }
                    }));
            recyclerViewType.setAdapter(typeAdapter);
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    Integer type = goodsAdapter.getItemViewType(position);
                    if(type== GoodsAdapter.TYPE_ITEM){
                        return 1;
                    }else if(type== GoodsAdapter.TYPE_FOOTER){
                        return 2;
                    }
                    return 1;
                }
            });
            recyclerViewGood.setLayoutManager(gridLayoutManager);
            recyclerViewGood.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition + 1 == goodsAdapter.getItemCount() && newState==RecyclerView.SCROLL_STATE_IDLE) {
                        boolean isRefreshing = swipeRefreshLayoutGood.isRefreshing();
                        if (!hasNextPage || isRefreshing) {
                            goodsAdapter.notifyItemRemoved(goodsAdapter.getItemCount());
                            return;
                        }
                        if (!isLoading) {
                            loadGoods(typeId,++goodsPage);
                        }
                    }
                }
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
            recyclerViewGood.setAdapter(goodsAdapter);
            swipeRefreshLayoutType.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
            swipeRefreshLayoutType.setEnabled(false);
            swipeRefreshLayoutGood.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
            swipeRefreshLayoutGood.setEnabled(false);
            swipeRefreshLayoutGood.setVisibility(View.GONE);
            initCitys();
            rootView=view;
        }
        CLog.d("pastTime",App.pastTime("HomeFragment.onCreateView.end"));
        return view;
    }

    private void initCitys(){
        final String citys[] = getResources().getStringArray(R.array.meinv);//获取城市列表
        final HttpUrl httpUrl = HttpUrl.parse("http://shop.oabc.cc:88/zgMBFrontShopV2/");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(App.getContext(), R.layout.simple_spinner_item, citys);
        adapter.setDropDownViewResource(R.layout.my_drop_down_item);
        spinner.setAdapter(adapter);
        PersistentCookieStore cookieStore = new PersistentCookieStore(App.getContext());
        Cookie cookie = cookieStore.getCookie(httpUrl,"provinceName");
        if(cookie==null){//若cookie中尚未设置provinceName的值，则默认设置为citys数组中的第一个
            try{
                cookieStore.add(httpUrl, new Cookie.Builder().hostOnlyDomain("shop.oabc.cc").name("provinceName").value(URLEncoder.encode(citys[0],"utf-8").toLowerCase()).build());
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            for (int i=0;i<citys.length;i++){
                try{
                    if(URLDecoder.decode(cookie.value(),"utf-8").equals(citys[i])){
                        spinner.setSelection(i,true);
                        break;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        spinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
                final PersistentCookieStore cookieStore = new PersistentCookieStore(App.getContext());
                AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
                dialog.setTitle("警告");
                dialog.setMessage("切换城市将会清空您的购物车，是否继续？");
                dialog.setCancelable(false);//不允许点击Back键
                dialog.setPositiveButton("切换", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            //修改cookie中provinceName的值，如果当前是在产品列表页面，则刷新产品列表
                            cookieStore.add(httpUrl, new Cookie.Builder().hostOnlyDomain("shop.oabc.cc").name("provinceName").value(URLEncoder.encode(((TextView)arg1).getText().toString(),"utf-8").toLowerCase()).build());
                            if(swipeRefreshLayoutGood.getVisibility()== View.VISIBLE){
                                goodsPage=0;
                                loadGoods(typeId,goodsPage);
                            }
                            CartPrefs cartPrefs = new CartPrefs(App.getContext());
                            cartPrefs.removeAll();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cookie cookie = cookieStore.getCookie(httpUrl,"provinceName");
                        for (int i=0;i<citys.length;i++){
                            try{
                                if(URLDecoder.decode(cookie.value(),"utf-8").equals(citys[i])){
                                    spinner.setOnItemSelectedListener(null);
                                    spinner.setSelection(i,true);
                                    spinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
                                    break;
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                dialog.show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        };
        spinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
    }

    @Override
    public void onStart() {
        CLog.d("pastTime",App.pastTime("HomeFragment.onStart.begin"));
        super.onStart();
        if(swipeRefreshLayoutGood.getVisibility()==View.GONE){
            if(curTypes==null){
                loadTypes();
            }else{
                typeAdapter.setTypes(curTypes);
            }
        }else{
            typeAdapter.setTypes(curTypes);
            goodsAdapter.setGoods(curGoods);
        }
        CLog.d("pastTime",App.pastTime("HomeFragment.onStart.end"));
    }

    private void loadTypes(){
        swipeRefreshLayoutType.setRefreshing(true);
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/")
                .subscribeOn(Schedulers.io())
                .map(Api.getTypesFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerType);
    }

    private void loadGoods(final String typeId,final Integer page) {
        isLoading = true;
        if(page==0){
            unsubscribe();
            goodsAdapter.setGoods(null);
        }
        swipeRefreshLayoutGood.setRefreshing(true);
        subscription = Network.getObservale("http://shop.oabc.cc:88/zgMBFrontShopV2/mbcMain.aspx?typeId="+typeId+"&page="+page)
                .subscribeOn(Schedulers.io())
                .map(Api.getGoodsFunc1())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observerGood);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * 内存过低时及时处理动画产生的未处理冗余tg
     */
    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        if (cartAnimation != null) {
            try {
                cartAnimation.root.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onLowMemory();
        }
    }
}
