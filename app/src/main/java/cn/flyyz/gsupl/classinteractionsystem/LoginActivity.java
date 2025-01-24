package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // 检查用户名和密码是否为空
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建用户对象
            User user = new User(username, password, null, null);
            loginUser(user);  // 执行登录请求
        });

        // 跳转到注册页面按钮点击事件
        btnGoToRegister.setOnClickListener(v -> {
            // 启动注册页面
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);  // 跳转到注册页面
        });
    }

    // 登录用户的逻辑
    private void loginUser(User user) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.loginUser(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 登录成功
                    try {
                        // 解析响应数据，获取 user_type 或其他信息（具体根据后台返回内容）
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        String status = jsonObject.optString("status");
                        if ("success".equals(status)) {
                            // 登录成功，获取用户类型,用户ID并跳转
                            String userType = jsonObject.optString("user_type");
                            String username = user.getUsername();
                            String userid = jsonObject.optString("user_id");

                            // 保存用户数据到 SharedPreferences
                            saveUserData(username, userType, userid);

                            // 根据用户类型跳转到不同页面
                            navigateToHomePage(userType);
                        } else {
                            Toast.makeText(LoginActivity.this, "登录失败: " + jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "解析错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 登录失败
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 网络错误
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 保存用户数据到 SharedPreferences
    private void saveUserData(String username, String userType, String userid) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("user_type", userType);
        editor.putString("user_id", userid);
        editor.apply();  // 异步提交
    }

    // 根据用户类型跳转到不同的主页
    private void navigateToHomePage(String userType) {
        Intent intent;
        switch (userType) {
            case "admin":
                intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                break;
            case "teacher":
                intent = new Intent(LoginActivity.this, TeacherHomeActivity.class);
                break;
            case "student":
                intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
                break;
            default:
                Toast.makeText(LoginActivity.this, "未知用户类型", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();  // 结束当前登录界面
    }
}
