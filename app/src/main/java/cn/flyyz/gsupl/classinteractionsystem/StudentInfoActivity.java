package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentInfoActivity extends AppCompatActivity {

    private TextView tvUsername, tvUserType, tvEmail, tvRegisteredAt;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        // 初始化控件
        tvUsername = findViewById(R.id.tvUsername);
        tvUserType = findViewById(R.id.tvUserType);
        tvEmail = findViewById(R.id.tvEmail);
        tvRegisteredAt = findViewById(R.id.tvRegisteredAt);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // 获取用户名（假设从 SharedPreferences 中获取）
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            // 从服务端获取用户信息
            fetchUserInfo(username);
        } else {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
        }

        // 跳转到修改密码页面
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(StudentInfoActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserInfo(String username) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // 发起网络请求，获取用户信息
        Call<ResponseBody> call = apiService.getStudentInfo(username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // 解析响应数据
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);

                        String status = jsonObject.optString("status");
                        if ("success".equals(status)) {
                            // 提取用户信息
                            String fetchedUsername = jsonObject.optString("username");
                            String email = jsonObject.optString("email");
                            String userType = jsonObject.optString("user_type");
                            String registeredAt = jsonObject.optString("created_at");

                            //将返回的用户注册时间格式化为

                            // 显示在界面上
                            tvUsername.setText("用户名: " + fetchedUsername);
                            tvEmail.setText("邮箱: " + email);
                            tvUserType.setText("用户类型: " + userType);
                            tvRegisteredAt.setText("注册时间: " + registeredAt);
                        } else {
                            String message = jsonObject.optString("message");
                            Toast.makeText(StudentInfoActivity.this, "获取用户信息失败: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(StudentInfoActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StudentInfoActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(StudentInfoActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
