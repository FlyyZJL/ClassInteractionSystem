package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrectListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PendingSubmissionAdapter adapter;
    private FloatingActionButton fabRefresh, fabExport, fabStatistics;
    private List<PendingSubmission> pendingSubmissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_list);

        // 初始化视图
        fabRefresh = findViewById(R.id.fab_refresh);
        fabExport = findViewById(R.id.fab_export);
        fabStatistics = findViewById(R.id.fab_statistics);
        recyclerView = findViewById(R.id.rv_pending_submissions);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingSubmissionAdapter();
        recyclerView.setAdapter(adapter);

        // 按钮点击监听
        fabRefresh.setOnClickListener(v -> refreshData());
        fabExport.setOnClickListener(v -> showExportDialog());
        fabStatistics.setOnClickListener(v -> showStatisticsDialog());

        // 加载初始数据
        refreshData();
    }

    private void refreshData() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(prefs.getString("user_id", "0"));
        loadPendingSubmissions(teacherId);
    }

    private void showStatisticsDialog() {
        if (pendingSubmissions.isEmpty()) {
            Toast.makeText(this, "暂无作业统计数据", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要查看统计的作业");

        // 生成带课程名称的条目
        String[] items = pendingSubmissions.stream()
                .map(sub -> String.format("%s - %s",
                        sub.getCourseName(),
                        sub.getAssignmentTitle()))
                .toArray(String[]::new);

        builder.setItems(items, (dialog, which) -> {
            int assignmentId = pendingSubmissions.get(which).getAssignmentId();
            navigateToStatistics(assignmentId);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void navigateToStatistics(int assignmentId) {
        Intent intent = new Intent(this, SubmissionStatsActivity.class);
        intent.putExtra("assignment_id", assignmentId);
        startActivity(intent);
    }

    private void loadPendingSubmissions(int teacherId) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getPendingSubmissions(teacherId, "teacher")
                .enqueue(new Callback<ApiService.ApiResponse<List<PendingSubmission>>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<List<PendingSubmission>>> call,
                                           Response<ApiService.ApiResponse<List<PendingSubmission>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiService.ApiResponse<List<PendingSubmission>> res = response.body();
                            if (res.isSuccess()) {
                                pendingSubmissions = res.getData();
                                adapter.submitList(pendingSubmissions);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<List<PendingSubmission>>> call, Throwable t) {
                        Toast.makeText(CorrectListActivity.this,
                                "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void showExportDialog() {
        if (pendingSubmissions.isEmpty()) {
            Toast.makeText(this, "没有可导出的作业", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建作业选择对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要导出未交列表的作业");

        // 提取作业标题列表
        String[] titles = pendingSubmissions.stream()
                .map(PendingSubmission::getAssignmentTitle)
                .toArray(String[]::new);

        builder.setItems(titles, (dialog, which) -> {
            int assignmentId = pendingSubmissions.get(which).getAssignmentId();
            startExport(assignmentId);
        });

        builder.show();
    }

    private void startExport(int assignmentId) {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(prefs.getString("user_id", "0"));

        // 构建导出URL
        String url = "http://api.flyyz.cn/api/teacher/export-unsubmitted?"
                + "teacherId=" + teacherId
                + "&assignmentId=" + assignmentId;

        // 使用浏览器下载
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}