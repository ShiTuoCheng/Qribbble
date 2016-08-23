package stcdribbble.shituocheng.com.qribbble.Utilities;

/**
 * Created by shituocheng on 2016/7/18.
 */
public class API {

    public static final String generic_api = "https://api.dribbble.com/v1/";

    public static final String getRecentShotsAPI(){
        String access_token = Access_Token.access_token;

        return generic_api+"shots"+"?"+"sort"+"="+"recent"+"&"+ "access_token=" + access_token;
    }

    public static final String getSortsShotsApi(String sort){

        String access_token = Access_Token.access_token;

        return generic_api+"shots"+"?"+"list="+sort+"&"+"access_token="+access_token;
    }

}
