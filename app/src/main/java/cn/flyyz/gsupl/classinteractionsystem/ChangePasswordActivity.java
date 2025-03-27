package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSubmit;
    private TextInputLayout tilNewPassword;

    private TextView tvPasswordStrength;
    private ProgressBar passwordStrengthBar;
    private ImageView ivLength, ivUppercase, ivSpecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        // 初始化视图
        tilNewPassword = findViewById(R.id.tilNewPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        ivLength = findViewById(R.id.ivLength);
        ivUppercase = findViewById(R.id.ivUppercase);
        ivSpecial = findViewById(R.id.ivSpecial);

        // 设置工具栏返回按钮
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 添加密码强度检查
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkPasswordStrength(s.toString());
            }
        });

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
    private void checkPasswordStrength(String password) {
        // 检查密码长度
        boolean hasLength = password.length() >= 6;
        ivLength.setImageTintList(ColorStateList.valueOf(
                hasLength ? getResources().getColor(R.color.password_medium) :
                        getResources().getColor(R.color.inactive_icon)));

        // 检查是否包含字母
        boolean hasUppercase = !(password.equals(password.toLowerCase()) && password.equals(password.toUpperCase()));
        ivUppercase.setImageTintList(ColorStateList.valueOf(
                hasUppercase ? getResources().getColor(R.color.password_medium) :
                        getResources().getColor(R.color.inactive_icon)));
        // 检查是否包含特殊字符
        boolean hasSpecial = !password.matches("[A-Za-z0-9]*");
        ivSpecial.setImageTintList(ColorStateList.valueOf(
                hasSpecial ? getResources().getColor(R.color.password_medium) :
                        getResources().getColor(R.color.inactive_icon)));

        // 计算密码强度
        int strength = 0;
        if (hasLength) strength++;
        if (hasUppercase) strength++;
        if (hasSpecial) strength++;

        // 更新UI
        if (strength == 0) {
            tvPasswordStrength.setText("密码强度: 请输入密码");
            tvPasswordStrength.setTextColor(getResources().getColor(R.color.secondary_text));
            passwordStrengthBar.setProgress(0);
            passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.secondary_text)));
        } else if (strength == 1) {
            tvPasswordStrength.setText("密码强度: 弱");
            tvPasswordStrength.setTextColor(getResources().getColor(R.color.password_weak));
            passwordStrengthBar.setProgress(33);
            passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.password_weak)));
        } else if (strength == 2) {
            tvPasswordStrength.setText("密码强度: 中");
            tvPasswordStrength.setTextColor(getResources().getColor(R.color.password_medium));
            passwordStrengthBar.setProgress(66);
            passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.password_medium)));
        } else {
            tvPasswordStrength.setText("密码强度: 强");
            tvPasswordStrength.setTextColor(getResources().getColor(R.color.password_strong));
            passwordStrengthBar.setProgress(100);
            passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.password_strong)));
        }
    }
}
