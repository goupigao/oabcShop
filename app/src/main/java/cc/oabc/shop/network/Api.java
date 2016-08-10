package cc.oabc.shop.network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.oabc.shop.model.Address;
import cc.oabc.shop.model.Cart;
import cc.oabc.shop.model.FormData;
import cc.oabc.shop.model.Good;
import cc.oabc.shop.model.Goods;
import cc.oabc.shop.model.Info;
import cc.oabc.shop.model.Order;
import cc.oabc.shop.model.Orders;
import cc.oabc.shop.model.PurchaseForm;
import cc.oabc.shop.model.Type;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/7/30.
 */
public class Api {
    public static Func1 getTypesFunc1(){
        return new Func1<String, List<Type>>() {
            @Override
            public List<Type> call(String response) {
                List<Type> types = new ArrayList<Type>();
                Matcher m = Pattern.compile("<div class=\"left_menu_sub_a\"[\\s\\S]*?typeId=([\\s\\S]*?)&[\\s\\S]*?>([\\s\\S]*?)</a>[\\s\\S]*?</div>").matcher(response);
                while(m.find()) {
                    Type type=new Type();
                    type.id=Integer.parseInt(m.group(1));
                    type.name=m.group(2).trim();
                    types.add(type);
                }
                return types;
            }
        };
    }

    public static Func1 getGoodsFunc1(){
        return new Func1<String, Goods>() {
            @Override
            public Goods call(String response) {
                Goods goods = new Goods();
                List<Good> listGoods = new ArrayList<Good>();
                Matcher m = Pattern.compile("<div class=\"ps_pic[\\s\\S]*?src='([\\s\\S]*?)'[\\s\\S]*?supplyId=([\\s\\S]*?)'[\\s\\S]*?>([\\s\\S]*?)</a>[\\s\\S]*?价格：￥([\\s\\S]*?)</div>[\\s\\S]*?会员价： ￥([\\s\\S]*?)</div>").matcher(response);
                while(m.find()) {
                    Good good=new Good();
                    good.image_url="http://shop.oabc.cc:88/zgMBFrontShopV2/"+m.group(1).trim();
                    good.id=Integer.parseInt(m.group(2).trim());
                    good.name=m.group(3).trim();
                    good.price=Float.parseFloat(m.group(4).trim());
                    good.couponPrice=Float.parseFloat(m.group(5).trim());
                    listGoods.add(good);
                }
                Matcher m1 = Pattern.compile("上一页[\\s\\S]*?</span>\\|([\\s\\S]*?)/([\\s\\S]*?)\\|").matcher(response);
                m1.find();
                goods.goods=listGoods;
                goods.page=Integer.parseInt(m1.group(1).trim())-1;
                goods.hasNextPage=Integer.parseInt(m1.group(2).trim())-1>goods.page?true:false;
                return goods;
            }
        };
    }

    public static Func1 getCartFunc1(){
        return new Func1<String, List<Cart>>() {
            @Override
            public List<Cart> call(String response) {
                List<Cart> listCart = new ArrayList<Cart>();
                Matcher m = Pattern.compile("id=\"ContentPlaceHolder1_RP1_LBL_ItemIndex_[\\s\\S]*?<td >([\\s\\S]*?)</td>[\\s\\S]*?<input[\\s\\S]*?value=\"([\\s\\S]*?)\"[\\s\\S]*?￥([\\s\\S]*?)</td>[\\s\\S]*?￥([\\s\\S]*?)</span>").matcher(response);
                while(m.find()) {
                    Cart cart = new Cart();
                    cart.name=m.group(1).trim();
                    cart.count=Integer.parseInt(m.group(2).trim());
                    cart.couponPrice=Float.parseFloat(m.group(3).trim());
                    cart.totalPrice=Float.parseFloat(m.group(4).trim());
                    listCart.add(cart);
                }
                return listCart;
            }
        };
    }

    public static Func1 getPurchaseFunc1(){
        return new Func1<String, PurchaseForm>() {
            @Override
            public PurchaseForm call(String response) {
                PurchaseForm purchaseForm = new PurchaseForm();
                Matcher m;
                m = Pattern.compile("ContentPlaceHolder1_UcCmmDeliverOK_LBL_CardMoney\">([\\s\\S]*?)</span>[\\s\\S]*?ContentPlaceHolder1_UcCmmDeliverOK_LBL_Date\">([\\s\\S]*?)</span>[\\s\\S]*?ContentPlaceHolder1_UcCmmDeliverOK_LBL_Number\">([\\s\\S]*?)</span>").matcher(response);
                if (m.find()){
                    //已经下单成功
                    purchaseForm.cardMoney = Float.parseFloat(m.group(1).trim());
                    purchaseForm.deliveryTime = m.group(2).trim();
                    purchaseForm.orderNo = m.group(3).trim();
                }else{
                    //还在下单页面
                    //获取地址列表
                    m = Pattern.compile("name=\"rbnSelectedAddress\" value=\"([\\s\\S]*?)\"[\\s\\S]*?id=\"ContentPlaceHolder1_RPT1_ucReceiverAddr1_[\\s\\S]*?>([\\s\\S]*?)</span>[\\s\\S]*?id=\"ContentPlaceHolder1_RPT1_ucReceiverAddr1_[\\s\\S]*?>([\\s\\S]*?)</span>").matcher(response);
                    while(m.find()) {
                        Address address = new Address();
                        address.addressId = Integer.parseInt(m.group(1).trim());
                        address.completeAddress = m.group(2).trim();
                        address.completeContact = m.group(3).trim();
                        purchaseForm.addressList.add(address);
                    }
                    //获取选中的地址
                    m = Pattern.compile("myLabelId=\"lbl_(\\d*?)\"\\s*?checked=\"checked\"").matcher(response);
                    if(m.find()){purchaseForm.selectAddressId=Integer.parseInt(m.group(1).trim());}
                    //获取配送日期列表及选中的配送日期
                    m = Pattern.compile("id=\"ContentPlaceHolder1_DDL_Date\"([\\s\\S]*?)</select>").matcher(response);
                    m.find();
                    String html = m.group(1).trim();
                    m = Pattern.compile("<option[\\s\\S]*?>([\\s\\S]*?)</option>").matcher(html);
                    while(m.find()) {
                        purchaseForm.timeList.add(m.group(1).trim());
                    }
                    m = Pattern.compile("<option[\\s\\S]*?>([\\s\\S]*?)</option>").matcher(html);
                    m.find();
                    purchaseForm.selectTime=m.group(1).trim();
                    //获取产品列表
                    m = Pattern.compile("<table class=\"tb_orderList\"([\\s\\S]*?)</table>").matcher(response);
                    m.find();
                    html = m.group(1).trim();
                    m = Pattern.compile("<tr>\\s*?<td>[\\s\\S]*?<td>([\\s\\S]*?)</td>\\s*?<td>([\\s\\S]*?)</td>[\\s\\S]*?￥([\\s\\S]*?)</td>\\s*?<td>([\\s\\S]*?)</td>\\s*?<td>[\\s\\S]*?</tr>").matcher(html);
                    while(m.find()) {
                        Cart cart = new Cart();
                        cart.name = m.group(1).trim();
                        cart.guige = m.group(2).trim();
                        cart.couponPrice = Float.parseFloat(m.group(3).trim());
                        cart.count = Integer.parseInt(m.group(4).trim());
                        purchaseForm.cartList.add(cart);
                    }
                    //获取总价
                    m = Pattern.compile("id=\"ContentPlaceHolder1_Label_Total\">([\\s\\S]*?)</span>").matcher(response);
                    m.find();
                    purchaseForm.totalPrice = Float.parseFloat(m.group(1).trim());
                    //获取编辑或新增地址时的默认值
                    m = Pattern.compile("<table class=\"tb_order_addressCheck checkOff\"([\\s\\S]*?)</table>").matcher(response);
                    if(m.find()){
                        html = m.group(1).trim();
                        //获取收件人姓名
                        m = Pattern.compile("\\$TBX_Name\" type=\"text\" value=\"([\\s\\S]*?)\" id=\"ContentPlaceHolder1_ucReceiverAddrEdit1_TBX_Name").matcher(html);
                        if (m.find()){purchaseForm.name = m.group(1).trim();}
                        //获取省份列表及选中的省份
                        m = Pattern.compile("pcz\\$DDL_Province([\\s\\S]*?)</select>").matcher(html);
                        m.find();
                        String selectHtml = m.group(1).trim();
                        m = Pattern.compile("value=\"([\\s\\S]*?)\"").matcher(selectHtml);
                        while (m.find()){
                            purchaseForm.provinceList.add(m.group(1).trim());
                        }
                        m = Pattern.compile("selected=\"selected\" value=\"([\\s\\S]*?)\">").matcher(selectHtml);
                        if (m.find()){purchaseForm.province = m.group(1).trim();}
                        //获取城市列表及选中的城市
                        m = Pattern.compile("pcz\\$sel_city([\\s\\S]*?)</select>").matcher(html);
                        m.find();
                        selectHtml = m.group(1).trim();
                        m = Pattern.compile("value=\"([\\s\\S]*?)\"").matcher(selectHtml);
                        while (m.find()){
                            purchaseForm.cityList.add(m.group(1).trim());
                        }
                        m = Pattern.compile("selected=\"selected\" value=\"([\\s\\S]*?)\">").matcher(selectHtml);
                        if (m.find()){purchaseForm.city = m.group(1).trim();}
                        //获取区县列表及选中的区县
                        m = Pattern.compile("pcz\\$sel_zone([\\s\\S]*?)</select>").matcher(html);
                        m.find();
                        selectHtml = m.group(1).trim();
                        m = Pattern.compile("value=\"([\\s\\S]*?)\"").matcher(selectHtml);
                        while (m.find()){
                            purchaseForm.zoneList.add(m.group(1).trim());
                        }
                        m = Pattern.compile("selected=\"selected\" value=\"([\\s\\S]*?)\">").matcher(selectHtml);
                        if (m.find()){purchaseForm.zone = m.group(1).trim();}
                        //获取地址详情
                        m = Pattern.compile("\\$TBX_Address\" type=\"text\" value=\"([\\s\\S]*?)\" id=\"ContentPlaceHolder1_ucReceiverAddrEdit1_TBX_Address").matcher(html);
                        if (m.find()){purchaseForm.address = m.group(1).trim();}
                        //获取手机号码
                        m = Pattern.compile("\\$TBX_Mobile\" type=\"text\" value=\"([\\s\\S]*?)\" id=\"ContentPlaceHolder1_ucReceiverAddrEdit1_TBX_Mobile").matcher(html);
                        if (m.find()){purchaseForm.mobile = m.group(1).trim();}
                        //获取固定电话
                        m = Pattern.compile("\\$TBX_Phone\" type=\"text\" value=\"([\\s\\S]*?)\" id=\"ContentPlaceHolder1_ucReceiverAddrEdit1_TBX_Phone").matcher(html);
                        if (m.find()){purchaseForm.phone = m.group(1).trim();}
                        //检测是新增还是修改地址
                        m = Pattern.compile("ContentPlaceHolder1_Chk_AddNewAddr[\\s\\S]*?checked=\"checked\"[\\s\\S]*?td8\">使用新地址").matcher(response);
                        purchaseForm.isExistAddress = !m.find();
                        //获取操作返回值
                        m = Pattern.compile("ContentPlaceHolder1_LBL_Message\"[\\s\\S]*?Red[\\s\\S]*?>([\\s\\S]*?)</").matcher(response);
                        if (m.find()){purchaseForm.editAddressResult = m.group(1).trim();}
                    }
                    //获取下单的返回值
                    m = Pattern.compile("ContentPlaceHolder1_LBL_Msg\"[\\s\\S]*?Red[\\s\\S]*?>([\\s\\S]*?)</").matcher(response);
                    if (m.find()){purchaseForm.orderResult = m.group(1).trim();}
                }
                //获取viewState和eventValidation
                m = MyPattern.getFormDataPattern().matcher(response);
                m.find();
                purchaseForm.viewState=m.group(1).trim();
                purchaseForm.eventValidation=m.group(2).trim();
                return purchaseForm;
            }
        };
    }

    public static Func1 getFormDataFunc1(){
        return new Func1<String, FormData>() {
            @Override
            public FormData call(String response) {
                FormData formData = new FormData();
                Matcher m = MyPattern.getFormDataPattern().matcher(response);
                m.find();
                formData.viewState=m.group(1).trim();
                formData.eventValidation=m.group(2).trim();
                return formData;
            }
        };
    }

    public static Func1 getBalanceFunc1(){
        return new Func1<String, Info>() {
            @Override
            public Info call(String response) {
                Info info = new Info();
                Matcher m = Pattern.compile("ContentPlaceHolder1_UC_Balance1_LBL_CardNumber\">([\\s\\S]*?)</span>[\\s\\S]*?ContentPlaceHolder1_UC_Balance1_LBL_Remain1\">([\\s\\S]*?)</span>").matcher(response);
                if(m.find()){
                    info.userName=Integer.parseInt(m.group(1).trim());
                    info.cardMoney=Float.parseFloat(m.group(2).trim());
                }
                return info;
            }
        };
    }

    public static Func1 getOrdersFunc1(){
        return new Func1<String, Orders>() {
            @Override
            public Orders call(String response) {
                Orders orders = new Orders();
                List<Order> listOrders = new ArrayList<Order>();
                Matcher m = Pattern.compile("<tr>\\s*?<td>([\\s\\S]*?)</td>\\s*?<td>([\\s\\S]*?)</td>\\s*?<td>([\\s\\S]*?)</td>[\\s\\S]*?￥([\\s\\S]*?)元[\\s\\S]*?<td>([\\s\\S]*?)</td>[\\s\\S]*?RecID=([\\s\\S]*?)&[\\s\\S]*?</tr>").matcher(response);
                while(m.find()) {
                    Order order=new Order();
                    order.orderNo = m.group(1).trim();
                    order.createTime = m.group(2).trim();
                    order.deliveryTime = m.group(3).trim();
                    order.orderPrice = Float.parseFloat(m.group(4).trim());
                    order.orderState = m.group(5).trim();
                    order.redId = Integer.parseInt(m.group(6).trim());
                    listOrders.add(order);
                }
                orders.orders = listOrders;
                m = Pattern.compile("ContentPlaceHolder1_UC_Deliver1_myPager_Label_Curent[\\s\\S]*?selected=\"selected\" value=\"([\\s\\S]*?)\"[\\s\\S]*?总页数：([\\s\\S]*?)总记录数：").matcher(response);
                m.find();
                orders.page = Integer.parseInt(m.group(1).trim());
                orders.hasNextPage = Integer.parseInt(m.group(2).trim())>orders.page;
                m = MyPattern.getFormDataPattern().matcher(response);
                m.find();
                orders.viewState=m.group(1).trim();
                orders.eventValidation=m.group(2).trim();
                return orders;
            }
        };
    }

    public static Func1 getOrderFunc1(){
        return new Func1<String, Order>() {
            @Override
            public Order call(String response) {
                Order order = new Order();
                Matcher m = Pattern.compile("LBL_OrderNumber\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_State\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_DeliverDate\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_CreateTime\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_TotalMoney1\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_PayMoney\">([\\s\\S]*?)</span>[\\s\\S]*?UcReceiver_LBL_Name\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_Address\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_Postalcode\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_Phone\">([\\s\\S]*?)</span>[\\s\\S]*?LBL_Mobile\">([\\s\\S]*?)</span>").matcher(response);
                m.find();
                order.orderNo = m.group(1).trim();
                order.orderState = m.group(2).trim();
                order.deliveryTime = m.group(3).trim();
                order.createTime = m.group(4).trim();
                order.orderPrice = Float.parseFloat(m.group(5).trim());
                order.payPrice = Float.parseFloat(m.group(6).trim());
                order.name = m.group(7).trim();
                order.address = m.group(8).trim();
                order.postCode = m.group(9).trim();
                order.phone = m.group(10).trim();
                order.mobile = m.group(11).trim();
                List<Cart> listCart = new ArrayList<>();
                m = Pattern.compile("<tr>\\s*?<td>([\\s\\S]*?)</td>\\s*?<td>([\\s\\S]*?)</td>[\\s\\S]*?￥([\\s\\S]*?)元[\\s\\S]*?<td>([\\s\\S]*?)</td>[\\s\\S]*?</tr>").matcher(response);
                while(m.find()) {
                    Cart cart = new Cart();
                    cart.name = m.group(1).trim();
                    cart.guige = m.group(2).trim();
                    cart.couponPrice = Float.parseFloat(m.group(3).trim());
                    cart.count = Integer.parseInt(m.group(4).trim());
                    listCart.add(cart);
                }
                order.carts = listCart;
                return order;
            }
        };
    }

    public static Func1 getLoginFunc1(){
        return new Func1<String, String>() {
            @Override
            public String call(String response) {
                String result;
                Matcher m = Pattern.compile("colspan=\"2\"  style=\"color: red\">([\\s\\S]*?)</td>").matcher(response);
                m.find();
                result=m.group(1).trim();
                return result;
            }
        };
    }
}
