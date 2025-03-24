package cn.flyyz.gsupl.classinteractionsystem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.*;          // 通配符导入

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitAssignmentActivity extends AppCompatActivity {
    private static final int MAX_RETRY_COUNT = 3;
    private ActivityResultLauncher<String[]> filePickerLauncher;

    // Views
    private TextView tvAssignmentTitle, tvDueDate, tvCourseName, tvFileName;
    private EditText etContent;
    private ProgressBar progressBar;

    // Data
    private Uri selectedFileUri;
    private int assignmentId;
    private int retryCount = 0;

    // Network
    private WeakReference<Call<JsonObject>> currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        initViews();
        setupFilePicker();
        handleIntent();
        loadAssignmentDetails();
    }

    private void initViews() {
        tvAssignmentTitle = findViewById(R.id.tv_assignment_title);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvCourseName = findViewById(R.id.tv_course_name);
        tvFileName = findViewById(R.id.tv_file_name);
        etContent = findViewById(R.id.et_content);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_select_file).setOnClickListener(v -> openFilePicker());
        findViewById(R.id.btn_submit).setOnClickListener(v -> validateAndSubmit());
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        selectedFileUri = uri;
                        tvFileName.setText(getFileName(uri));
                    }
                }
        );
    }

    private void handleIntent() {
        Intent intent = getIntent();
        assignmentId = intent.getIntExtra("assignment_id", -1);
        if (assignmentId == -1) {
            showErrorDialog("无效的作业ID", true);
        }
    }

    private void loadAssignmentDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("assignment_data", MODE_PRIVATE);
        tvAssignmentTitle.setText(sharedPreferences.getString("title", "未知作业"));
        tvDueDate.setText(sharedPreferences.getString("due_date", "未知截止日期"));
        tvCourseName.setText(sharedPreferences.getString("course_name", "未知课程"));
    }

    private String getFileName(Uri uri) {
        String displayName = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex);
                }
            }
        } catch (SecurityException e) {
            Log.e("FilePicker", "Permission denied", e);
        }
        return displayName != null ? displayName : "未知文件";
    }

    private void openFilePicker() {
        try {
            String[] mimeTypes = {"*/*"};
            filePickerLauncher.launch(mimeTypes);
        } catch (ActivityNotFoundException e) {
            showErrorDialog("未找到文件选择器应用", false);
        }
    }

    private void validateAndSubmit() {
        if (selectedFileUri == null && etContent.getText().toString().trim().isEmpty()) {
            showErrorDialog("内容或文件必须填写一项", false);
            return;
        }

        if (selectedFileUri != null) {
            try {
                long fileSize = getFileSize(selectedFileUri);
                if (fileSize > 50 * 1024 * 1024) {
                    showErrorDialog("文件大小不能超过50MB", false);
                    return;
                }
            } catch (IOException e) {
                showErrorDialog("文件读取失败", false);
                return;
            }
        }

        showProgress(true);
        submitAssignment();
    }

    private long getFileSize(Uri uri) throws IOException {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
            return pfd.getStatSize();
        }
    }

    private void submitAssignment() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        RequestBody studentId = createTextBody(sharedPreferences.getString("user_id", "0"));
        RequestBody assignmentIdBody = createTextBody(String.valueOf(this.assignmentId));
        RequestBody content = createTextBody(etContent.getText().toString());
        MultipartBody.Part filePart = buildFilePart();

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<JsonObject> call = api.submitAssignment(studentId, assignmentIdBody, content, filePart);
        currentCall = new WeakReference<>(call);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!call.isCanceled()) {
                    handleNetworkError(t);
                }
            }
        });
    }

    private RequestBody createTextBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private MultipartBody.Part buildFilePart() {
        if (selectedFileUri == null) return null;

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            File tempFile = FileUtil.createTempFile(this, "submission_");
            FileUtil.copyInputStreamToFile(inputStream, tempFile);

            return MultipartBody.Part.createFormData(
                    "file",
                    getFileName(selectedFileUri),
                    RequestBody.create(MediaType.parse(getContentResolver().getType(selectedFileUri)), tempFile)
            );
        } catch (IOException | SecurityException e) {
            Log.e("FileUpload", "File processing failed", e);
            return null;
        }
    }

    private void handleResponse(Response<JsonObject> response) {
        showProgress(false);

        // 添加响应日志
        Log.d("HTTP", "响应码: " + response.code());
        Log.d("HTTP", "响应头: " + response.headers());

        try {
            // 处理403状态码
            if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                try {
                    String errorBody = response.errorBody().string();
                    Gson gson = new Gson();
                    JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
                    if ("ALREADY_SUBMITTED".equals(errorJson.get("code").getAsString())) {
                        new AlertDialog.Builder(this)
                                .setTitle("提交失败")
                                .setMessage("该作业已提交，不可重复提交")
                                .setPositiveButton("确定", null)
                                .show();
                        return;
                    }

                    // 原有已批改处理逻辑
                    if ("ALREADY_GRADED".equals(errorJson.get("code").getAsString())) {
                        handleGradedResponse(errorJson);
                        return;
                    }
                } catch (Exception e) {
                    Log.e("Error", "处理错误响应失败", e);
                }
            }

            // 处理其他状态码
            if (response.isSuccessful() && response.body() != null) {
                JsonObject result = response.body();
                Log.d("HTTP", "成功响应: " + result);

                if (result.get("success").getAsBoolean()) {
                    handleSuccess(result);
                } else {
                    handleBusinessError(result);
                }
            } else {
                handleHttpError(response);
            }
        } catch (IllegalStateException | JsonSyntaxException e) {
            Log.e("HTTP", "响应处理异常", e);
            showErrorDialog("响应解析失败: " + e.getMessage(), false);
        }
    }

    private void handleSuccess(JsonObject result) {
        Toast.makeText(this, result.get("message").getAsString(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra("submission_time", System.currentTimeMillis()));
        supportFinishAfterTransition();
    }

    private void handleBusinessError(JsonObject result) {
        new AlertDialog.Builder(this)
                .setTitle("提交失败")
                .setMessage(result.get("message").getAsString())
                .setPositiveButton("确定", null)
                .show();
    }

    private void handleHttpError(Response<JsonObject> response) {
        String errorMessage = "服务器错误：" + response.code();
        try {
            if (response.errorBody() != null) {
                errorMessage += "\n" + response.errorBody().string();
            }
        } catch (IOException ignored) {
        }

        new AlertDialog.Builder(this)
                .setTitle("服务器错误")
                .setMessage(errorMessage)
                .setPositiveButton("重试", (d, w) -> retrySubmission())
                .setNegativeButton("取消", null)
                .show();
    }

    private void handleNetworkError(Throwable t) {
        showProgress(false);

        if (retryCount < MAX_RETRY_COUNT) {
            new AlertDialog.Builder(this)
                    .setTitle("网络异常")
                    .setMessage("是否重试？ (" + (retryCount + 1) + "/" + MAX_RETRY_COUNT + ")")
                    .setPositiveButton("重试", (d, w) -> {
                        retryCount++;
                        submitAssignment();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            showErrorDialog("提交失败，请检查网络连接", true);
        }
    }

    private void handleGradedResponse(JsonObject response) {
        Log.d("GradeDebug", "原始响应: " + response);

        if (response == null) {
            showErrorDialog("服务器返回空响应", false);
            return;
        }

        try {
            if (!response.has("gradeInfo")) {
                throw new JSONException("响应缺少gradeInfo字段");
            }

            JsonObject gradeInfo = response.getAsJsonObject("gradeInfo");

            // 安全解析所有字段
            String score = gradeInfo.has("score") ? gradeInfo.get("score").getAsString() : "未评分";
            String feedback = gradeInfo.has("feedback") ? gradeInfo.get("feedback").getAsString() : "暂无评语";
            String gradedBy = gradeInfo.has("gradedBy") ? gradeInfo.get("gradedBy").getAsString() : "未知批改人";
            long gradedAt = gradeInfo.has("gradedAt") ? gradeInfo.get("gradedAt").getAsLong() : System.currentTimeMillis();

            // 格式化消息
            String message = String.format(Locale.CHINA,
                    "分数：%s\n评语：%s\n批改人：%s\n时间：%s",
                    score,
                    feedback,
                    gradedBy,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(gradedAt))
            );

            new AlertDialog.Builder(this)
                    .setTitle("作业已批改")
                    .setMessage(message)
                    .setPositiveButton("确定", (d, w) -> finish())
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            Log.e("GradeError", "解析失败", e);
            showErrorDialog("数据解析错误: " + e.getMessage(), false);
        }
    }

    private void retrySubmission() {
        showProgress(true);
        submitAssignment();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_submit).setEnabled(!show);
    }

    private void showErrorDialog(String message, boolean finishOnDismiss) {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(message)
                .setPositiveButton("确定", (d, w) -> {
                    if (finishOnDismiss) finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null && currentCall.get() != null) {
            currentCall.get().cancel();
        }
        super.onDestroy();
    }
}