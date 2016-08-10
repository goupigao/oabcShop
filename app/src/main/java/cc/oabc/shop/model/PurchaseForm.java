package cc.oabc.shop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/5.
 */
public class PurchaseForm {
    public List<Address> addressList = new ArrayList<>();
    public Integer selectAddressId;
    public List<String> timeList = new ArrayList<>();
    public String selectTime;
    public List<Cart> cartList = new ArrayList<>();
    public Float totalPrice;
    public String viewState;
    public String eventValidation;
    public String name;
    public String province;
    public List<String> provinceList = new ArrayList<>();
    public String city;
    public List<String> cityList = new ArrayList<>();
    public String zone;
    public List<String> zoneList = new ArrayList<>();
    public String address;
    public String mobile;
    public String phone;
    public Boolean isExistAddress;
    public String editAddressResult;
    public String orderResult;
    public Float cardMoney;
    public String deliveryTime;
    public String orderNo;
}
