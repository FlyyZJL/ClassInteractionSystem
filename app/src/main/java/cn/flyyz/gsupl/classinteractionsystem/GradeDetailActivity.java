package cn.flyyz.gsupl.classinteractionsystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;



import cn.flyyz.gsupl.classinteractionsystem.ApiService.Grade;
import cn.flyyz.gsupl.classinteractionsystem.ApiClient;
import cn.flyyz.gsupl.classinteractionsystem.ApiService.ApiResponse;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeDetailActivity extends AppCompatActivity {

    private ApiService apiService;
    private SessionManager sessionManager;
    private Grade grade;
    private int gradeId;

    // 视图
    private TextView tvStudentName, tvCourseName, tvGradeType, tvScore;
    private TextView tvFeedback, tvGradedBy, tvGradeDate;
    private View scoreCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setTitle("成绩详情");

        // 初始化视图
        tvStudentName = findViewById(R.id.tvStudentName);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvGradeType = findViewById(R.id.tvGradeType);
        tvScore = findViewById(R.id.tvScore);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvGradedBy = findViewById(R.id.tvGradedBy);
        tvGradeDate = findViewById(R.id.tvGradeDate);
        scoreCircle = findViewById(R.id.scoreCircle);

        // 初始化
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager);

        // 获取传递的成绩ID
        if (getIntent().hasExtra("gradeId")) {
            gradeId = getIntent().getIntExtra("gradeId", 0);
            loadGradeDetails(gradeId);
        } else {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 加载成绩详情
    private void loadGradeDetails(int gradeId) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
        apiService.getGradeDetail(gradeId, teacherId).enqueue(new Callback<ApiResponse<Grade>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Grade>> call,
                                   @NonNull Response<ApiResponse<Grade>> response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    grade = response.body().getData();
                    displayGradeDetails();
                } else {
                    Toast.makeText(GradeDetailActivity.this, "获取成绩详情失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Grade>> call, @NonNull Throwable t) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(GradeDetailActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // 显示成绩详情
    private void displayGradeDetails() {
        if (grade != null) {
            tvStudentName.setText(grade.getStudentName());
            tvCourseName.setText(grade.getCourseName());
            tvGradeType.setText(grade.getGradeType());
            tvScore.setText(String.valueOf(grade.getScore()));

            // 处理评语
            if (grade.getFeedback() != null && !grade.getFeedback().isEmpty()) {
                tvFeedback.setText(grade.getFeedback());
                tvFeedback.setVisibility(View.VISIBLE);
            } else {
                tvFeedback.setVisibility(View.GONE);
            }

            // 评分人与日期
            tvGradedBy.setText("评分人: " + grade.getGradedByName());
            // 评分人与日期
            String gradedByName = grade.getGradedByName();
            tvGradedBy.setText("评分人: " + gradedByName);

            try {
                String dateString = grade.getGradeDate();
                if (dateString != null && !dateString.isEmpty()) {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    Date date = inputFormat.parse(dateString);
                    String formattedDate = outputFormat.format(date);
                    tvGradeDate.setText("评分日期: " + formattedDate);
                } else {
                    tvGradeDate.setText("评分日期: 未知");
                }
            } catch (ParseException e) {
                Log.e("GradeDetailActivity", "Date parsing error: " + e.getMessage());
                tvGradeDate.setText("评分日期: " + grade.getGradeDate());
            }

            Log.d("GradeDetailActivity", "Grade details loaded: " + grade.toString());

            // 设置成绩圆圈背景色
            int colorRes;
            if (grade.getScore() >= 90) {
                colorRes = R.color.colorGradeHighBg;
            } else if (grade.getScore() >= 60) {
                colorRes = R.color.colorGradeMediumBg;
            } else {
                colorRes = R.color.colorGradeLowBg;
            }
            scoreCircle.setBackgroundTintList(getResources().getColorStateList(colorRes));
        }
    }

    // 编辑成绩
    private void editGrade() {
        if (grade != null) {
            Intent intent = new Intent(this, AddEditGradeActivity.class);
            intent.putExtra("gradeId", grade.getGradeId());
            intent.putExtra("courseId", grade.getCourseId());
            Log.d("GradeDetailActivity", "Editing grade with ID: " + grade.getGradeId()+
                    " and course ID: " + grade.getCourseId());
            startActivity(intent);
        }
    }

    // 删除成绩
    private void deleteGrade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除这条成绩记录吗？此操作不可恢复！");
        builder.setPositiveButton("删除", (dialog, which) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
            int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
            apiService.deleteGrade(gradeId, teacherId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                       @NonNull Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(GradeDetailActivity.this, "成绩已删除", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(GradeDetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    Toast.makeText(GradeDetailActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gradeId > 0) {
            loadGradeDetails(gradeId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grade_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            editGrade();
            return true;
        } else if (id == R.id.action_delete) {
            deleteGrade();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}