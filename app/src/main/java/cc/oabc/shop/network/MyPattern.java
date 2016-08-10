package cc.oabc.shop.network;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/8/4.
 */
public class MyPattern {
    public static Pattern getFormDataPattern(){
        return Pattern.compile("__VIEWSTATE\" value=\"([\\s\\S]*?)\"[\\s\\S]*?__EVENTVALIDATION\" value=\"([\\s\\S]*?)\"");
    }

}
