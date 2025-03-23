package cn.flyyz.gsupl.classinteractionsystem;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradingActivity extends AppCompatActivity {
    private int submissionId;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);

        // 获取参数
        submissionId = getIntent().getIntExtra("submission_id", -1);
        int teacherId = Integer.parseInt(getSharedPreferences("user_data", MODE_PRIVATE).getString("user_id", null));

        // 加载作业详情
        loadSubmissionDetail(teacherId);

        // 设置提交按钮
        findViewById(R.id.btn_submit).setOnClickListener(v -> submitGrade(teacherId));

        // 设置附件查看
        findViewById(R.id.btn_view_file).setOnClickListener(v -> openFile());
    }

    private void loadSubmissionDetail(int teacherId) {
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getSubmissionDetail(submissionId, teacherId, "teacher")
                .enqueue(new Callback<ApiService.ApiResponse<SubmissionDetail>>() {
                    @Override
                    public void onResponse(
                            Call<ApiService.ApiResponse<SubmissionDetail>> call,
                            Response<ApiService.ApiResponse<SubmissionDetail>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiService.ApiResponse<SubmissionDetail> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                displayDetail(apiResponse.getData());
                            } else {
                                Toast.makeText(GradingActivity.this, "加载失败1", LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<SubmissionDetail>> call, Throwable t) {
                        Toast.makeText(GradingActivity.this, "加载失败", LENGTH_SHORT).show();
                    }
                });
    }

    private void displayDetail(SubmissionDetail detail) {
        ((TextView) findViewById(R.id.tv_content)).setText(detail.getContent());
        ((TextView) findViewById(R.id.tv_student)).setText(detail.getStudentName());


        if (detail.getFilePath() != null && !detail.getFilePath().isEmpty()) {
            filePath = detail.getFilePath();
            findViewById(R.id.btn_view_file).setVisibility(View.VISIBLE);
        }
    }

    private void openFile() {
        if (filePath == null || filePath.isEmpty()) {
            Toast.makeText(this, "文件不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建显示意图
        Intent intent = new Intent(this, FileViewerActivity.class);
        intent.putExtra("file_path", filePath);

        // 验证是否有可用组件
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "没有可用的文件查看器", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitGrade(int teacherId) {
        EditText etScore = findViewById(R.id.et_score);
        EditText etFeedback = findViewById(R.id.et_feedback);

        try {
            float score = Float.parseFloat(etScore.getText().toString());
            if (score < 0 || score > 100) {
                Toast.makeText(this, "分数必须在0-100之间", LENGTH_SHORT).show();
                return;
            }

            ApiService.GradeRequest request = new ApiService.GradeRequest(
                    teacherId, submissionId, score, etFeedback.getText().toString()
            );

            ApiService service = RetrofitClient.getClient().create(ApiService.class);
            service.submitGrade(request).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                       Response<ApiService.ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiService.ApiResponse<Void> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            setResult(RESULT_OK);
                            Toast.makeText(GradingActivity.this, "提交成功", LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(GradingActivity.this, apiResponse.getMessage(), LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                    Toast.makeText(GradingActivity.this, "提交失败", LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效分数", LENGTH_SHORT).show();
        }
    }
}