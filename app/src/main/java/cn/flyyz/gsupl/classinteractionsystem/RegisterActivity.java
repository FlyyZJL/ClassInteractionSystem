package cn.flyyz.gsupl.classinteractionsystem;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout tilUsername, tilPassword, tilEmail, tilUserType;
    private TextInputEditText etUsername, etPassword, etEmail;
    private AutoCompleteTextView spinnerUserType;
    private MaterialButton btnRegister, btnGoToLogin;

    // 创建一个映射关系
    private static final Map<String, String> userTypeMap = new HashMap<>();
    static {
        userTypeMap.put("学生", "student");
        userTypeMap.put("教师", "teacher");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化控件
        initViews();

        // 设置用户类型下拉菜单
        setupUserTypeDropdown();

        // 设置按钮点击事件
        setupClickListeners();
    }

    private void initViews() {
        // TextInputLayouts
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilUserType = findViewById(R.id.tilUserType);

        // TextInputEditTexts
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);

        // AutoCompleteTextView (替代原有的Spinner)
        spinnerUserType = findViewById(R.id.spinnerUserType);

        // Buttons
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
    }

    private void setupUserTypeDropdown() {
        String[] userTypes = {"学生", "教师"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item,
                userTypes
        );
        spinnerUserType.setAdapter(adapter);

        // 设置默认选项
        if (userTypes.length > 0) {
            spinnerUserType.setText(userTypes[0], false);
        }
    }

    private void setupClickListeners() {
        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String email = etEmail.getText().toString();
                String userType = spinnerUserType.getText().toString();

                // 根据选中的中文用户类型获取对应的英文用户类型
                String userTypeInEnglish = userTypeMap.get(userType);

                User user = new User(username, password, email, userTypeInEnglish);
                registerUser(user);
            }
        });

        // 返回登录页面
        btnGoToLogin.setOnClickListener(v -> finish());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // 验证用户名
        if (TextUtils.isEmpty(etUsername.getText())) {
            tilUsername.setError("用户名不能为空");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // 验证密码
        String password = etPassword.getText().toString();
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        if (password.length() < 6 || !hasLetter || !hasDigit) {
            tilPassword.setError("密码必须至少6个字符且包含字母和数字");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // 验证邮箱
        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("请输入有效的邮箱地址");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // 验证用户类型
        if (TextUtils.isEmpty(spinnerUserType.getText())) {
            tilUserType.setError("请选择用户类型");
            isValid = false;
        } else {
            tilUserType.setError(null);
        }

        return isValid;
    }

    private void registerUser(User user) {
        btnRegister.setEnabled(false);  // 禁用按钮防止重复点击

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.registerUser(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);  // 恢复按钮状态

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                    // 注册成功后跳转到登录页面
                    finish();
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ?
                                response.errorBody().string() : "未知错误";
                        Toast.makeText(RegisterActivity.this,
                                "错误: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this,
                                "请求错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);  // 恢复按钮状态
                Toast.makeText(RegisterActivity.this,
                        "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}