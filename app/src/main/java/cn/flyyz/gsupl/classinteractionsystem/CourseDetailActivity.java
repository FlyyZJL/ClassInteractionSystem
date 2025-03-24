package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CourseDetailActivity extends AppCompatActivity {

    private TextView tvCourseName, tvCourseDescription;
    private Button btnAddStudents,btnPublishAssignment,btnDiscussion,btnCreateChapter,btnViewChapter;
    private String userRole;
    private int courseId;  // 当前课程ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // 初始化控件
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        btnAddStudents = findViewById(R.id.btnAddStudents);
        btnPublishAssignment = findViewById(R.id.btnPublishAssignment);
        btnDiscussion = findViewById(R.id.btnDiscussion);
        btnCreateChapter = findViewById(R.id.btnCreateChapter);
        btnViewChapter = findViewById(R.id.btnViewChapters);
        // 获取传递的数据
        Intent intent = getIntent();
        courseId = intent.getIntExtra("courseId", -1);
        String courseName = intent.getStringExtra("courseName");
        String courseDescription = intent.getStringExtra("courseDescription");

        // 新增：获取用户角色
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        userRole = prefs.getString("user_type", "student"); // 默认设为学生
        // 显示课程信息
        tvCourseName.setText(courseName);
        tvCourseDescription.setText(courseDescription);

        setupUIByRole();

        // 跳转到添加学生页面
        btnAddStudents.setOnClickListener(v -> {
            Intent addStudentIntent = new Intent(CourseDetailActivity.this, AddStudentsActivity.class);
            addStudentIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(addStudentIntent);
        });
        btnPublishAssignment.setOnClickListener(v -> {
            Intent publishAssignmentIntent = new Intent(CourseDetailActivity.this, PublishAssignmentActivity.class);
            publishAssignmentIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(publishAssignmentIntent);
        });
        // 跳转到讨论区页面
        btnDiscussion.setOnClickListener(v -> {
            Intent discussionIntent = new Intent(CourseDetailActivity.this, DiscussionActivity.class);
            discussionIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(discussionIntent);
        });
        // 跳转到创建章节页面
        btnCreateChapter.setOnClickListener(v -> {
            Intent createChapterIntent = new Intent(CourseDetailActivity.this, TeacherCreateActivity.class);
            createChapterIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(createChapterIntent);
        });

        // 跳转到查看章节页面
        btnViewChapter.setOnClickListener(v -> {
            Intent viewChapterIntent = new Intent(CourseDetailActivity.this, StudentListActivity.class);
            viewChapterIntent.putExtra("courseId", courseId);  // 传递课程ID
            startActivity(viewChapterIntent);
        });
    }
    private void setupUIByRole() {
        if ("student".equals(userRole)) {
            // 学生隐藏管理功能
            btnAddStudents.setVisibility(View.GONE);
            btnPublishAssignment.setVisibility(View.GONE);
            btnCreateChapter.setVisibility(View.GONE);

            // 可选：调整讨论区按钮位置
            btnDiscussion.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        } else {
            // 教师显示全部功能
            btnAddStudents.setVisibility(View.VISIBLE);
            btnPublishAssignment.setVisibility(View.VISIBLE);
            btnCreateChapter.setVisibility(View.VISIBLE);
        }
    }
}