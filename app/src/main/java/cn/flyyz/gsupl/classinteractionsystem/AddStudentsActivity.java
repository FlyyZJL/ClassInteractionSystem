package cn.flyyz.gsupl.classinteractionsystem;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStudentsActivity extends AppCompatActivity {

    private ListView lvStudents;
    private Button btnAddSelectedStudents;
    private List<Student> studentList = new ArrayList<>();
    private List<Integer> studentIds = new ArrayList<>();
    private int courseId;  // 当前课程ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        // 初始化控件
        lvStudents = findViewById(R.id.lvStudents);
        btnAddSelectedStudents = findViewById(R.id.btnAddSelectedStudents);


        //添加非空判断
        if (getIntent() == null) {
            Toast.makeText(AddStudentsActivity.this, "获取课程ID失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 获取课程ID
        courseId = getIntent().getIntExtra("courseId", -1);

        if (courseId == -1) {
            Toast.makeText(AddStudentsActivity.this, "获取课程ID失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        // 加载所有学生数据
        loadAllStudents();

        // 点击按钮将选中的学生添加到课程中
        btnAddSelectedStudents.setOnClickListener(v -> {
            if (!studentIds.isEmpty()) {
                addStudentsToCourse();
            } else {
                Toast.makeText(AddStudentsActivity.this, "请先选择学生", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllStudents() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Student>> call = apiService.getAllStudents();

        call.enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    studentList = response.body();
                    StudentAdapter adapter = new StudentAdapter(AddStudentsActivity.this, studentList, studentIds);
                    lvStudents.setAdapter(adapter);
                } else {
                    Toast.makeText(AddStudentsActivity.this, "获取学生数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                Toast.makeText(AddStudentsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AddStudentsActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void addStudentsToCourse() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // 创建请求体的数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("courseId", courseId);
        requestData.put("studentIds", studentIds);

        // 将数据转为 JSON
        Call<ResponseBody> call = apiService.addStudentsToCourse(requestData);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // 解析返回的 JSON 数据
                        String responseString = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseString);

                        // 获取状态和消息
                        String status = jsonResponse.optString("status");
                        String message = jsonResponse.optString("message");
                        String successID = jsonResponse.optString("addedStudentIds");
                        if ("success".equals(status)) {
                            // 成功
                            Toast.makeText(AddStudentsActivity.this, "成功添加学生:" + successID+"到课程", Toast.LENGTH_SHORT).show();
                        } else {
                            // 失败，显示错误信息
                            Toast.makeText(AddStudentsActivity.this, "失败: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddStudentsActivity.this, "解析返回数据失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddStudentsActivity.this, "添加学生失败,已在课程中", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AddStudentsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}