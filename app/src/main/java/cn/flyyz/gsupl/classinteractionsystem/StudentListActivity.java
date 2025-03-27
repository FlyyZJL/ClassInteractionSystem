package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private View emptyView;
    private SwipeRefreshLayout swipeRefresh;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // 设置工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 获取传递的课程ID
        courseId = getIntent().getIntExtra("courseId", 0);

        // 初始化视图
        initViews();

        // 加载章节数据
        loadChapters();

        // 添加章节按钮点击事件（如果需要）
        FloatingActionButton fabAddChapter = findViewById(R.id.fab_add_chapter);
        fabAddChapter.setOnClickListener(v -> {
            // 根据您的需求打开创建章节的Activity
             Intent intent = new Intent(StudentListActivity.this, TeacherCreateActivity.class);
             intent.putExtra("courseId", courseId);
             startActivity(intent);
        });

        // 如果是学生用户，隐藏添加章节按钮
        if (!isTeacher()) {
            fabAddChapter.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 设置下拉刷新
        swipeRefresh.setColorSchemeResources(R.color.primary_color, R.color.accent_color);
        swipeRefresh.setOnRefreshListener(this::loadChapters);
    }

    // 判断是否为教师用户的方法（示例）
    private boolean isTeacher() {
        // 根据您的用户系统实现此方法
        // 例如，从SharedPreferences获取用户类型
        return getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("user_type", "").equals("teacher");
    }

    private void loadChapters() {
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getChapters(courseId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Chapter> chapters = response.body();

                    if (chapters.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);

                        ChapterAdapter adapter = new ChapterAdapter(chapters);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    showError("获取章节失败，请稍后再试");
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("网络错误：" + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ChapterAdapter extends RecyclerView.Adapter<ChapterViewHolder> {
        private final List<Chapter> chapters;

        ChapterAdapter(List<Chapter> chapters) {
            this.chapters = chapters;
        }

        @Override
        public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter, parent, false);
            return new ChapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChapterViewHolder holder, int position) {
            Chapter chapter = chapters.get(position);

            // 设置章节序号
            holder.tvChapterNumber.setText(String.valueOf(position + 1));

            // 设置章节标题
            holder.tvTitle.setText(chapter.getTitle());

            // 设置章节内容预览
            String content = chapter.getContent();
            if (content != null && !content.isEmpty()) {
                holder.tvPreview.setText(content);
                holder.tvPreview.setVisibility(View.VISIBLE);
            } else {
                holder.tvPreview.setVisibility(View.GONE);
            }

            // 设置日期
            // 假设Chapter类有getCreatedAt方法，根据实际情况修改
            if (chapter.getCreatedAt() != null) {
                holder.tvDate.setText(formatDate(chapter.getCreatedAt()));
                holder.tvDate.setVisibility(View.VISIBLE);
            } else {
                holder.tvDate.setVisibility(View.GONE);
            }

            // 设置视频标签
            // 假设Chapter类有hasVideo方法，根据实际情况修改
            holder.videoTag.setVisibility(chapter.hasVideo() ? View.VISIBLE : View.GONE);

            // 设置更多按钮（如果是教师用户）
            if (isTeacher()) {
                holder.btnMore.setVisibility(View.VISIBLE);
                holder.btnMore.setOnClickListener(v -> showChapterOptions(v, chapter, position));
            } else {
                holder.btnMore.setVisibility(View.GONE);
            }

            // 设置整个条目的点击事件
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StudentListActivity.this, StudentDetailActivity.class);
                intent.putExtra("chapter", chapter);
                startActivity(intent);
            });
        }

        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(date);
        }

        private void showChapterOptions(View view, Chapter chapter, int position) {
            PopupMenu popup = new PopupMenu(StudentListActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.chapter_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    // 打开编辑章节页面
                    // Intent intent = new Intent(StudentListActivity.this, EditChapterActivity.class);
                    // intent.putExtra("chapter", chapter);
                    // startActivity(intent);
                    return true;
                } else if (id == R.id.action_delete) {
                    // 显示删除确认对话框
                    confirmDeleteChapter(chapter, position);
                    return true;
                }
                return false;
            });
            popup.show();
        }

        private void confirmDeleteChapter(Chapter chapter, int position) {
            new MaterialAlertDialogBuilder(StudentListActivity.this)
                    .setTitle("删除章节")
                    .setMessage("确定要删除这个章节吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 调用API删除章节
//                        deleteChapter(chapter.getId(), position);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }

//        private void deleteChapter(int chapterId, int position) {
//            // 实现删除章节的API调用
//            ApiService api = RetrofitClient.getClient().create(ApiService.class);
//            api.deleteChapter(chapterId).enqueue(new Callback<Void>() {
//                @Override
//                public void onResponse(Call<Void> call, Response<Void> response) {
//                    if (response.isSuccessful()) {
//                        chapters.remove(position);
//                        notifyItemRemoved(position);
//                        notifyItemRangeChanged(position, chapters.size());
//
//                        // 检查列表是否为空
//                        if (chapters.isEmpty()) {
//                            recyclerView.setVisibility(View.GONE);
//                            emptyView.setVisibility(View.VISIBLE);
//                        }
//
//                        Toast.makeText(StudentListActivity.this, "章节已删除", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(StudentListActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Void> call, Throwable t) {
//                    Toast.makeText(StudentListActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }

        @Override
        public int getItemCount() {
            return chapters != null ? chapters.size() : 0;
        }
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterNumber;
        TextView tvTitle;
        TextView tvPreview;
        TextView tvDate;
        View videoTag;
        ImageButton btnMore;

        ChapterViewHolder(View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
            tvPreview = itemView.findViewById(R.id.tv_chapter_preview);
            tvDate = itemView.findViewById(R.id.tv_chapter_date);
            videoTag = itemView.findViewById(R.id.video_tag);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}