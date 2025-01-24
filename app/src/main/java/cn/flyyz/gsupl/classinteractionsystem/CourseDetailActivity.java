package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CourseDetailActivity extends AppCompatActivity {

    private TextView tvCourseName, tvCourseDescription;
    private Button btnAddStudents;

    private int courseId;  // 当前课程ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // 初始化控件
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        btnAddStudents = findViewById(R.id.btnAddStudents);

        // 获取传递的数据
        Intent intent = getIntent();
        courseId = intent.getIntExtra("courseId", -1);
        String courseName = intent.getStringExtra("courseName");
        String courseDescription = intent.getStringExtra("courseDescription");

        // 显示课程信息
        tvCourseName.setText(courseName);
        tvCourseDescription.setText(courseDescription);

        // 跳转到添加学生页面
        btnAddStudents.setOnClickListener(v -> {
            Intent addStudentIntent = new Intent(CourseDetailActivity.this, AddStudentsActivity.class);
            addStudentIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(addStudentIntent);
        });
    }
}