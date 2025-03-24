package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherCreateActivity extends AppCompatActivity {
    private static final int PICK_VIDEO_REQUEST = 202;
    private String videoUrl;
    private int courseId;

    // UI组件
    private EditText etTitle, etContent;
    private Button btnUpload, btnSubmit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_create);

        // 初始化视图
        initViews();

        // 获取课程ID
        courseId = getIntent().getIntExtra("courseId", -1);
        if (courseId == -1) {
            Toast.makeText(this, "无效的课程ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupButtonListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnUpload = findViewById(R.id.btn_upload);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);

        // 初始状态
        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.GONE);
    }

    private void setupButtonListeners() {
        // 视频上传按钮
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        // 提交按钮
        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                submitChapterToServer();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            handleVideoSelection(data.getData());
        }
    }

    private void handleVideoSelection(Uri videoUri) {
        if (videoUri == null) {
            showToast("文件选择错误");
            return;
        }

        File videoFile = new File(FileUtils.getPath(this, videoUri));
        if (!videoFile.exists()) {
            showToast("无法读取文件");
            return;
        }

        // 显示上传状态
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        uploadVideoFile(videoFile);
    }

    private void uploadVideoFile(File videoFile) {
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("video/*"),
                videoFile
        );

        MultipartBody.Part videoPart = MultipartBody.Part.createFormData(
                "video",
                videoFile.getName(),
                requestFile
        );

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.uploadVideo(videoPart).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    videoUrl = response.body().get("url").getAsString();
                    btnSubmit.setEnabled(true);
                    showToast("视频上传成功");
                } else {
                    showToast("上传失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                showToast("网络错误: " + t.getMessage());
            }
        });
    }

    private boolean validateInputs() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            showToast("请输入章节标题");
            return false;
        }

        if (etContent.getText().toString().trim().isEmpty()) {
            showToast("请输入章节内容");
            return false;
        }

        if (videoUrl == null) {
            showToast("请先上传视频");
            return false;
        }

        return true;
    }

    private void submitChapterToServer() {
        Chapter newChapter = new Chapter();
        newChapter.setCourseId(courseId);
        newChapter.setTitle(etTitle.getText().toString().trim());
        newChapter.setContent(etContent.getText().toString().trim());
        newChapter.setVideoUrl(videoUrl);

        // 显示提交进度
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.createChapter(newChapter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    showToast("章节创建成功");
                    finish();
                } else {
                    btnSubmit.setEnabled(true);
                    showToast("提交失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                showToast("网络错误: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}