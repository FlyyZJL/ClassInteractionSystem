package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        // 提交修改密码请求
        btnSubmit.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (newPassword.equals(confirmPassword)) {
                SharedPreferences sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE);
                // 从登录信息sharedpref获取用户名
                String username = sharedPref.getString("username", "unknown");
                Log.d("username", "username:"+username);
                changePassword(username, oldPassword, newPassword);
            } else {
                Toast.makeText(ChangePasswordActivity.this, "新密码和确认密码不一致", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePassword(String username, String oldPassword, String newPassword) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword);

        Call<ResponseBody> call = apiService.changePassword(request);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    // 修改密码成功后，返回到上一个页面
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
