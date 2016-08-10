package cc.oabc.shop.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.oabc.shop.model.Cart;

/**
 * Created by Administrator on 2016/8/4.
 */
public class CartPrefs {
    private static final String CART_PREFS = "Cart_Prefs";
    private final SharedPreferences cartPrefs;
    public CartPrefs(Context context) {
        cartPrefs = context.getSharedPreferences(CART_PREFS, 0);
    }
    public void setGood(Integer id,String name,Float couponPrice, Integer count){
        SharedPreferences.Editor editor =cartPrefs.edit();
        List<String> keys = new ArrayList<String>(Arrays.asList(TextUtils.split(cartPrefs.getString("keys",""), ",")));
        if(!keys.contains(id.toString())){
            keys.add(id.toString());
            editor.putString("keys",TextUtils.join(",",keys));
        }
        editor.putString("name@"+id,name);
        editor.putFloat("couponPrice@"+id,couponPrice);
        editor.putInt("count@"+id,count);
        editor.apply();
    }
    public void setGood(Integer id, Integer count){
        SharedPreferences.Editor editor =cartPrefs.edit();
        if(count>0){
            editor.putInt("count@"+id,count);
            editor.apply();
        }else if(count==0){
            removeGood(id);
        }
    }
    public void removeGood(Integer id){
        SharedPreferences.Editor editor =cartPrefs.edit();
        List<String> keys = new ArrayList<String>(Arrays.asList(TextUtils.split(cartPrefs.getString("keys",""), ",")));
        if(keys.contains(id.toString())){
            keys.remove(id.toString());
            editor.putString("keys",TextUtils.join(",",keys));
            editor.remove("name@"+id);
            editor.remove("couponPrice@"+id);
            editor.remove("count@"+id);
            editor.apply();
        }
    }
    public void removeAll(){
        cartPrefs.edit().clear().apply();
    }
    public String getGoodName(Integer id){
        return cartPrefs.getString("name@"+id,"");
    }
    public Float getGoodCouponPrice(Integer id){
        return cartPrefs.getFloat("couponPrice@"+id,0);
    }
    public Integer getGoodCount(Integer id){
        return cartPrefs.getInt("count@"+id,0);
    }
    public List<Cart> getListCart(){
        List<Cart> listCart = new ArrayList<>();
        String[] keys = TextUtils.split(cartPrefs.getString("keys",""), ",");
        for(int i=0;i<keys.length;i++){
            Cart cart = new Cart();
            cart.goodId = Integer.parseInt(keys[i]);
            cart.name = cartPrefs.getString("name@"+keys[i], "");
            cart.couponPrice = cartPrefs.getFloat("couponPrice@"+keys[i], 0);
            cart.count = cartPrefs.getInt("count@"+keys[i], 0);
            listCart.add(cart);
        }
        return listCart;
    }
}
