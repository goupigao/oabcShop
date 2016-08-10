package cc.oabc.shop.model;

import java.util.List;

/**
 * Created by Administrator on 2016/8/7.
 */
public class Orders {
    public List<Order> orders;
    public Integer page;
    public Boolean hasNextPage;
    public String viewState;
    public String eventValidation;
}
