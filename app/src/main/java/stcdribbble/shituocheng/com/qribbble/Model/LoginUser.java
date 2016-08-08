package stcdribbble.shituocheng.com.qribbble.Model;

/**
 * Created by shituocheng on 16/7/16.
 */

public class LoginUser {

    private String acess_token;
    private String token_type;
    private String scope;

    public String getAcess_token() {
        return acess_token;
    }

    public void setAcess_token(String acess_token) {
        this.acess_token = acess_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
