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

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etEmail;
    private Spinner spinnerUserType;
    private Button btnRegister;

    // 创建一个映射关系
    private static final Map<String, String> userTypeMap = new HashMap<>();
    static {
        userTypeMap.put("学生", "student");
        userTypeMap.put("教师", "teacher");
        userTypeMap.put("管理员", "admin");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        btnRegister = findViewById(R.id.btnRegister);

        // 设置 Spinner 的适配器
        String[] userTypes = {"学生", "教师", "管理员"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String email = etEmail.getText().toString();
            String userType = spinnerUserType.getSelectedItem().toString();  // 获取选中的用户类型
            // 根据选中的中文用户类型获取对应的英文用户类型
            String userTypeInEnglish = userTypeMap.get(userType);
            System.out.println("userType: " + userTypeInEnglish);
            User user = new User(username, password, email, userTypeInEnglish);
            registerUser(user);
        });
    }

    private void registerUser(User user) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.registerUser(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorMessage = response.errorBody().string();
                        Toast.makeText(RegisterActivity.this, "错误: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "请求错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}