package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DiscussionActivity extends AppCompatActivity implements DiscussionAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DiscussionAdapter adapter;
    private int courseId;
    private int userId;
    private String userRole;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        // 初始化参数
        courseId = getIntent().getIntExtra("courseId", 1);
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        userId = Integer.parseInt(prefs.getString("user_id", "1"));
        userRole = prefs.getString("user_type", "student");
        Log.d("userdata", "onCreate: " + userId + " " + userRole + " " + courseId);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        FloatingActionButton btnNewPost = findViewById(R.id.btn_new_post);

        // 配置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiscussionAdapter(this, userRole, userId, this);
        recyclerView.setAdapter(adapter);

        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::loadDiscussions);

        // 发帖按钮
        btnNewPost.setOnClickListener(v -> showPostDialog());

        // 加载数据
        loadDiscussions();
    }

    private void loadDiscussions() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getDiscussions(courseId, userId, userRole).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<DiscussionItem> items = parseResponse(response.body());
                    adapter.submitList(items);
                } else {
                    showToast("加载失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showToast("网络错误: " + t.getMessage());
            }
        });
    }

    private List<DiscussionItem> parseResponse(JsonArray jsonArray) {
        List<DiscussionItem> items = new ArrayList<>();
        Gson gson = new Gson();
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            items.add(new DiscussionItem(
                    obj.get("id").getAsInt(),
                    obj.get("title").getAsString(),
                    obj.get("author").getAsString(),
                    obj.get("user_id").getAsInt(),
                    obj.get("created_at").getAsLong(),
                    obj.get("is_pinned").getAsBoolean(),
                    obj.has("reply_count") ? obj.get("reply_count").getAsInt() : 0 // 安全处理字段缺失
            ));
        }
        return items;
    }

    @Override
    public void onItemClick(DiscussionItem item) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("DISCUSSION_ID", item.discussionId);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_role", userRole);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showPostDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_post, null);
        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etContent = view.findViewById(R.id.et_content);

        builder.setTitle("发布新帖")
                .setView(view)
                .setPositiveButton("发布", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (validatePostInput(title, content)) submitNewPost(title, content);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private boolean validatePostInput(String title, String content) {
        if (title.isEmpty()) {
            showToast("标题不能为空");
            return false;
        }
        if (content.isEmpty()) {
            showToast("内容不能为空");
            return false;
        }
        return true;
    }

    private void submitNewPost(String title, String content) {
        ApiService.DiscussionPostRequest request = new ApiService.DiscussionPostRequest(courseId, userId, userRole, title, content);
        apiService.createDiscussion(request).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    showToast("发帖成功");
                    loadDiscussions();
                } else {
                    try {
                        String error = response.errorBody().string();
                        Log.d("fatie", "onResponse: "+error);
                        showToast("发帖失败: " + error);
                    } catch (IOException e) {
                        Log.e("DiscussionActivity", "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showToast("网络错误: " + t.getMessage());
            }
        });
    }

    // 管理方法需要设为public供ViewHolder调用
    public void manageDiscussion(int discussionId, String action) {
        apiService.manageDiscussion(discussionId, userId, userRole, action).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadDiscussions();
                } else {
                    showToast("操作失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("网络错误: " + t.getMessage());
            }
        });
    }

    public void confirmDelete(int discussionId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除确认")
                .setMessage("确定要永久删除这个讨论帖吗？")
                .setPositiveButton("删除", (dialog, which) -> manageDiscussion(discussionId, "delete"))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}