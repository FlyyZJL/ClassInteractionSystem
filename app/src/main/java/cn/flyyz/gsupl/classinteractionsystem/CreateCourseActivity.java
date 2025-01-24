package cn.flyyz.gsupl.classinteractionsystem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CreateCourseActivity extends AppCompatActivity {

    private EditText etCourseName, etCourseDescription;
    private Button btnSelectTeacher, btnCreateCourse;
    private TextView tvSelectedTeacher;
    private String selectedTeacherId = null;
    private List<Teacher> teachers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        // 初始化控件
        etCourseName = findViewById(R.id.etCourseName);
        etCourseDescription = findViewById(R.id.etCourseDescription);
        btnSelectTeacher = findViewById(R.id.btnSelectTeacher);
        btnCreateCourse = findViewById(R.id.btnCreateCourse);
        tvSelectedTeacher = findViewById(R.id.tvSelectedTeacher);

        // 获取教师列表
        fetchTeachers();

        // 点击选择教师按钮
        btnSelectTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确保教师数据已加载完成
                if (teachers != null && !teachers.isEmpty()) {
                    showTeacherSelectionDialog();
                } else {
                    Toast.makeText(CreateCourseActivity.this, "教师数据未加载", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 点击创建课程按钮
        btnCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = etCourseName.getText().toString();
                String courseDescription = etCourseDescription.getText().toString();

                // 校验输入内容
                if (TextUtils.isEmpty(courseName)) {
                    Toast.makeText(CreateCourseActivity.this, "课程名称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(courseDescription)) {
                    Toast.makeText(CreateCourseActivity.this, "课程描述不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(selectedTeacherId)) {
                    Toast.makeText(CreateCourseActivity.this, "请选择教师", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 调用服务端接口创建课程
                createCourse(courseName, courseDescription, selectedTeacherId);
            }
        });
    }

    // 获取教师列表
    private void fetchTeachers() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getTeachers();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        // 使用新的 Teacher 类进行解析
                        Teacher[] teachersArray = new Gson().fromJson(responseString, Teacher[].class);
                        teachers = new ArrayList<>();
                        for (Teacher teacher : teachersArray) {
                            if (teacher != null && teacher.getTeacherName() != null && teacher.getTeacherId() != 0) {
                                teachers.add(teacher);
                            }
                        }
                        Log.d("fetchTeachers", "教师数据加载成功: " + teachers.size() + " 名教师");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CreateCourseActivity.this, "教师数据加载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CreateCourseActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 显示选择教师的对话框
    private void showTeacherSelectionDialog() {
        String[] teacherNames = new String[teachers.size()];
        for (int i = 0; i < teachers.size(); i++) {
            teacherNames[i] = teachers.get(i).getTeacherName();  // 获取教师的姓名
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择教师")
                .setItems(teacherNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 保存选中的教师信息
                        selectedTeacherId = String.valueOf(teachers.get(which).getTeacherId());  // 获取教师 ID
                        // 更新显示选中的教师
                        tvSelectedTeacher.setText("当前选择的教师: " + teacherNames[which]);
                    }
                });
        builder.show();
    }


    // 创建课程的网络请求
    private void createCourse(String courseName, String courseDescription, String teacherId) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // 创建课程请求
        Call<ResponseBody> call = apiService.createCourse(courseName, courseDescription, teacherId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 课程创建成功
                    Toast.makeText(CreateCourseActivity.this, "课程创建成功", Toast.LENGTH_SHORT).show();
                    finish();  // 关闭当前页面
                } else {
                    // 课程创建失败
                    Toast.makeText(CreateCourseActivity.this, "课程创建失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 网络请求失败
                Toast.makeText(CreateCourseActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 教师类，用于从服务器获取教师信息
    public class Teacher {
        private int teacherId;  // 对应返回的 teacherId
        private String teacherName;  // 对应返回的 teacherName
        private String email;  // 如果需要也可以用到

        public int getTeacherId() {
            return teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public String getEmail() {
            return email;
        }
    }

}