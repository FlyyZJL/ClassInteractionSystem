package cn.flyyz.gsupl.classinteractionsystem;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherHomeActivity extends AppCompatActivity {

    private ListView lvCourses;
    private Button btnCreateCourse;
    private Button btnViewAssignments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        lvCourses = findViewById(R.id.lvCourses);
        btnCreateCourse = findViewById(R.id.btnCreateCourse);
        btnViewAssignments = findViewById(R.id.btnViewAssignments);

        // 加载教师的课程数据
        loadTeacherCourses();

        // 创建课程按钮点击事件
        btnCreateCourse.setOnClickListener(v -> {
            // 跳转到创建课程页面
            Intent intent = new Intent(TeacherHomeActivity.this, CreateCourseActivity.class);
            startActivity(intent);
        });

        // 查看作业按钮点击事件
        btnViewAssignments.setOnClickListener(v -> {
            // 跳转到作业管理页面
            Intent intent = new Intent(TeacherHomeActivity.this, CorrectListActivity.class);
            startActivity(intent);
        });
        lvCourses.setOnItemClickListener((parent, view, position, id) -> {
            Course selectedCourse = (Course) parent.getItemAtPosition(position);

            // 跳转到课程详情页面
            Intent intent = new Intent(TeacherHomeActivity.this, CourseDetailActivity.class);
            intent.putExtra("courseId", selectedCourse.getCourseId());  // 传递课程ID
            intent.putExtra("courseName", selectedCourse.getCourseName());  // 传递课程名称
            intent.putExtra("courseDescription", selectedCourse.getDescription());  // 传递课程描述
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherCourses();
    }

    // 加载教师的课程数据
    private void loadTeacherCourses() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);


        // 通过SharedPreferences获取当前登录用户的ID
        String teacherId = getSharedPreferences("user_data", MODE_PRIVATE).getString("user_id", null);



        Call<List<Course>> call = apiService.getCoursesByTeacher(Integer.parseInt(teacherId));

        call.enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();
                    // 课程数据成功获取，更新UI
                    CourseAdapter adapter = new CourseAdapter(TeacherHomeActivity.this, courses);
                    lvCourses.setAdapter(adapter);
                } else {
                    Toast.makeText(TeacherHomeActivity.this, "当前无课程，请创建", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(TeacherHomeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TeacherHomeActivity", "Error: " + t.getMessage());
            }
        });
    }
}
