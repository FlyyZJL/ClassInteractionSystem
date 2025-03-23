package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StudentHomeActivity extends AppCompatActivity {

    private Button btnViewCourses;
    private Button btnViewAssignments;
    private Button btnViewProfile;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        // 初始化控件
        btnViewCourses = findViewById(R.id.btnViewCourses);
        btnViewAssignments = findViewById(R.id.btnViewAssignments);
        btnViewProfile = findViewById(R.id.btnViewProfile);


        // 设置按钮点击事件
        btnViewCourses.setOnClickListener(v -> {
            // 跳转到查看课程页面
            Intent intent = new Intent(StudentHomeActivity.this, StudentCoursesActivity.class);
            startActivity(intent);
        });

        btnViewAssignments.setOnClickListener(v -> {
            // 跳转到查看作业页面
            Intent intent = new Intent(StudentHomeActivity.this, StudentAssignmentsActivity.class);
            startActivity(intent);
        });

        btnViewProfile.setOnClickListener(v -> {
            // 跳转到查看个人信息页面
            Intent intent = new Intent(StudentHomeActivity.this, StudentInfoActivity.class);
            startActivity(intent);
        });


    }
}
