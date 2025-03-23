package cn.flyyz.gsupl.classinteractionsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrectListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PendingSubmissionAdapter adapter;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_list);

        //初始化浮动按钮
        fab = findViewById(R.id.fab_refresh);

        // 设置浮动按钮点击事件
        fab.setOnClickListener(v -> {
            // 刷新数据
            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
            int teacherId = Integer.parseInt(prefs.getString("user_id", null));
            loadPendingSubmissions(teacherId);

        });
        // 初始化RecyclerView
        recyclerView = findViewById(R.id.rv_pending_submissions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingSubmissionAdapter();
        recyclerView.setAdapter(adapter);

        // 获取教师ID（示例从SharedPreferences获取）
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(prefs.getString("user_id", null));

        // 加载数据
        loadPendingSubmissions(teacherId);
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
                                adapter.submitList(res.getData());
                            } else {
                                Toast.makeText(CorrectListActivity.this, "获取列表失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<List<PendingSubmission>>> call,
                                          Throwable t) {
                        Toast.makeText(CorrectListActivity.this,
                                "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新数据
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(prefs.getString("user_id", null));
        loadPendingSubmissions(teacherId);
    }
}