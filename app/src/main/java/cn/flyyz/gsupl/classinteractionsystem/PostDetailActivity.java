package cn.flyyz.gsupl.classinteractionsystem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";

    private int discussionId;
    private int userId;
    private String userRole;
    private ApiService apiService;

    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvTime;
    private TextView tvContent;
    private RecyclerView rvReplies;
    private SwipeRefreshLayout swipeRefresh;
    private ReplyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 获取参数
        discussionId = getIntent().getIntExtra("DISCUSSION_ID", 0);
        //使用sharedPreferences获取userId和userRole
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        userId = Integer.parseInt(prefs.getString("user_id", "0"));
        userRole = prefs.getString("user_type", "student");
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 初始化视图
        initViews();
        setupRecyclerView();
        loadData();
        setupReplySection();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvTime = findViewById(R.id.tv_time);
        tvContent = findViewById(R.id.tv_content);
        rvReplies = findViewById(R.id.rv_replies);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void setupRecyclerView() {
        rvReplies.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplyAdapter();
        rvReplies.setAdapter(adapter);
    }

    private void loadData() {
        // 加载帖子详情
        Call<JsonObject> detailCall = apiService.getDiscussionDetail(
                discussionId, userId, userRole
        );

        // 加载回复列表
        Call<JsonArray> repliesCall = apiService.getReplies(discussionId);

        detailCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject discussion = response.body().getAsJsonObject("discussion");
                        bindDiscussionData(discussion);
                    } else {
                        String errorMsg = "加载失败: ";
                        if (response.errorBody() != null) {
                            errorMsg += response.errorBody().string();
                        } else {
                            errorMsg += response.code();
                        }
                        showToast(errorMsg);
                        Log.d("123", "onResponse: "+errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "处理帖子详情异常", e);
                    showToast("数据解析错误");
                } finally {
                    checkAllLoaded();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "网络请求失败", t);
                showToast("网络连接失败，请检查网络");
                checkAllLoaded();
            }
        });

        repliesCall.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Reply> replies = parseReplies(response.body());
                        adapter.submitList(replies);
                    } else {
                        String errorMsg = "加载回复失败: ";
                        if (response.errorBody() != null) {
                            errorMsg += response.errorBody().string();
                        } else {
                            errorMsg += response.code();
                        }
                        showToast(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "处理回复列表异常", e);
                    showToast("回复数据解析错误");
                } finally {
                    checkAllLoaded();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.e(TAG, "加载回复失败", t);
                showToast("加载回复失败，请稍后重试");
                checkAllLoaded();
            }
        });
    }

    private void bindDiscussionData(JsonObject discussion) {
        try {
            tvTitle.setText(discussion.get("title").getAsString());
            tvAuthor.setText("作者：" + discussion.get("author").getAsString());
            tvTime.setText(formatTimestamp(discussion.get("created_at").getAsLong()));
            tvContent.setText(discussion.get("content").getAsString());
        } catch (Exception e) {
            Log.e(TAG, "绑定帖子数据异常", e);
            showToast("帖子数据异常，部分内容无法显示");
        }
    }

    private void setupReplySection() {
        EditText etReply = findViewById(R.id.et_reply);
        MaterialButton btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            String content = etReply.getText().toString().trim();
            if (!content.isEmpty()) {
                submitReply(content, null);
                etReply.setText("");
            }
        });
    }

    private void submitReply(String content, Integer parentReplyId) {
        ApiService.ReplyPostRequest request = new ApiService.ReplyPostRequest(
                discussionId, userId, userRole, content, parentReplyId
        );

        apiService.createReply(request).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    showToast("回复成功");
                    loadData();
                } else {
                    String errorMsg = "回复成功";
//                        if (response.errorBody() != null) {
//                            errorMsg += response.errorBody().string();
//                        } else {
//                            errorMsg += response.code();
//                        }
                    showToast(errorMsg);
                    loadData();
                    Log.d("huifu", "onResponse: "+errorMsg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "提交回复失败", t);
                showToast("回复失败，请检查网络连接");
            }
        });
    }

    // 辅助方法
    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }

    private void checkAllLoaded() {
        swipeRefresh.setRefreshing(false);
    }

    // 回复适配器
    private class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {
        private List<Reply> replies = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        void submitList(List<Reply> newReplies) {
            this.replies = buildNestedStructure(newReplies);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reply, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Reply reply = replies.get(position);

            // 设置左边距
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            params.leftMargin = reply.getLevel() * 32; // 每级缩进32dp
            holder.itemView.setLayoutParams(params);

            holder.tvContent.setText(reply.getContent());
            holder.tvAuthor.setText(reply.getAuthor());
            holder.tvTime.setText(formatTimestamp(reply.getCreatedAt()));

            // 点击回复
            holder.itemView.setOnClickListener(v -> {
                if (reply.getLevel() < 2) { // 最多允许三级嵌套
                    showReplyDialog(reply);
                }
            });
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent, tvAuthor, tvTime;

            ViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tv_content);
                tvAuthor = itemView.findViewById(R.id.tv_author);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }

        private List<Reply> buildNestedStructure(List<Reply> flatReplies) {
            Map<Integer, Reply> replyMap = new HashMap<>();
            List<Reply> rootReplies = new ArrayList<>();

            // 第一遍：建立映射
            for (Reply reply : flatReplies) {
                replyMap.put(reply.getId(), reply);
                reply.setLevel(0);
            }

            // 第二遍：构建层级
            for (Reply reply : flatReplies) {
                Integer parentId = reply.getParentReplyId();
                if (parentId != null && replyMap.containsKey(parentId)) {
                    Reply parent = replyMap.get(parentId);
                    parent.addChild(reply);
                    reply.setLevel(parent.getLevel() + 1);
                } else {
                    // 将无效父ID的回复作为根回复
                    if (!rootReplies.contains(reply)) {
                        rootReplies.add(reply);
                    }
                }
            }

            // 转换为扁平列表
            List<Reply> result = new ArrayList<>();
            for (Reply root : rootReplies) {
                addReplyWithChildren(result, root);
            }
            return result;
        }

        private void addReplyWithChildren(List<Reply> result, Reply reply) {
            result.add(reply);
            for (Reply child : reply.getChildren()) {
                addReplyWithChildren(result, child);
            }
        }
    }
    private List<Reply> parseReplies(JsonArray jsonArray) {
        List<Reply> replies = new ArrayList<>();
        try {
            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();

                // 修正parent_reply_id处理
                Integer parentId = null;
                if (obj.has("parent_reply_id") && !obj.get("parent_reply_id").isJsonNull()) {
                    int rawId = obj.get("parent_reply_id").getAsInt();
                    parentId = (rawId > 0) ? rawId : null; // 关键修正
                }

                Reply reply = new Reply(
                        obj.get("id").getAsInt(),
                        obj.get("content").getAsString(),
                        obj.get("author").getAsString(),
                        obj.has("created_at") ? obj.get("created_at").getAsLong() : System.currentTimeMillis(),
                        parentId, // 使用修正后的值
                        0
                );
                replies.add(reply);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析回复数据异常", e);
        }
        return replies;
    }
    private void showReplyDialog(@Nullable Reply parentReply) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reply, null);
        TextInputEditText etReply = view.findViewById(R.id.et_reply);

        // 设置对话框标题
        String title = "发表回复";
        if (parentReply != null) {
            title = "回复 @" + parentReply.getAuthor();
            etReply.setHint("回复" + parentReply.getAuthor());
        }

        builder.setTitle(title)
                .setView(view)
                .setPositiveButton("发送", (dialog, which) -> {
                    String content = etReply.getText().toString().trim();
                    if (!content.isEmpty()) {
                        submitReply(content, parentReply != null ? parentReply.getId() : null);
                    }
                })
                .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            // 自动弹出键盘
            etReply.postDelayed(() -> {
                etReply.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etReply, InputMethodManager.SHOW_IMPLICIT);
            }, 100);
        });

        dialog.show();
    }
    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "显示Toast失败", e);
        }
    }
}