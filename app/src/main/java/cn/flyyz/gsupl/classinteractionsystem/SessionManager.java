package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "SessionPref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    // 保存用户登录信息
    public void createLoginSession(String token, User user) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    // 获取当前登录用户
    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    // 获取用户ID
    public int getUserId() {
        User user = getUser();
        return user != null ? user.getUserId() : -1;
    }

    // 获取用户类型
    public String getUserType() {
        User user = getUser();
        return user != null ? user.getUserType() : "";
    }

    // 获取认证令牌
    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }

    // 检查用户是否已登录
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 注销用户
    public void logout() {
        editor.clear();
        editor.commit();
    }
}