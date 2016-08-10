package cc.oabc.shop.model;

import java.util.List;

/**
 * Created by Administrator on 2016/8/7.
 */
public class Order {
    public String orderNo="";
    public Integer redId;
    public String createTime="";
    public String deliveryTime="";
    public Float orderPrice=0F;
    public Float payPrice=0F;
    public String orderState="";
    public List<Cart> carts;
    public String name="";
    public String address="";
    public String postCode="";
    public String mobile="";
    public String phone="";
}
