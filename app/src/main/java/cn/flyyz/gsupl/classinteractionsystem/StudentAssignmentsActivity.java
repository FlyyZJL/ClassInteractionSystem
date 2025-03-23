package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAssignmentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;
    private FloatingActionButton fabRefresh;

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss") // 统一日期格式
            .disableHtmlEscaping()
            .create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignments);

        initViews();
        setupRecyclerView();
        loadAssignments();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_assignments);
        fabRefresh = findViewById(R.id.fab_refresh);

        fabRefresh.setOnClickListener(v -> {
            fabRefresh.hide();
            loadAssignments();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter();
        recyclerView.setAdapter(adapter);

        // 设置点击监听
        adapter.setOnItemClickListener((position, assignment) -> {
            Intent intent = new Intent(this, SubmitAssignmentActivity.class);
            intent.putExtra("assignment_id", assignment.getId());
            SharedPreferences sharedPreferences = getSharedPreferences("assignment_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("course_name", "课程："+assignment.getCourseName());
            editor.putString("title", "作业："+assignment.getTitle());
            editor.putString("description", assignment.getDescription());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dueDate = sdf.format(assignment.getDueDate());
            editor.putString("due_date", "截止时间："+dueDate);
            editor.apply();
            startActivity(intent);
        });
    }

    private void loadAssignments() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int studentId = Integer.parseInt(prefs.getString("user_id", "0"));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getStudentAssignments(studentId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                fabRefresh.show();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.get("success").getAsBoolean()) {
                        Type listType = new TypeToken<List<Assignment>>(){}.getType();

                        List<Assignment> newList = gson.fromJson(body.get("data"), listType);
                        adapter.submitList(newList);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                fabRefresh.show();
                Toast.makeText(StudentAssignmentsActivity.this,
                        "加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 修改后的 Adapter 实现
    static class AssignmentAdapter extends RecyclerView.Adapter<AssignmentViewHolder> {
        private List<Assignment> assignments = new ArrayList<>();
        private OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(int position, Assignment assignment);
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        void submitList(List<Assignment> newList) {
            assignments = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_assignment, parent, false);
            return new AssignmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
            Assignment assignment = assignments.get(position);
            holder.bind(assignment);

            // 在 Adapter 中处理点击事件
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position, assignment);
                }
            });
        }

        @Override
        public int getItemCount() {
            return assignments.size();
        }
    }

    // 修正后的 ViewHolder 实现
    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourse, tvTitle, tvDueDate, tvDescription;

        AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tv_course);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        void bind(Assignment assignment) {
            tvCourse.setText(assignment.getCourseName());
            tvTitle.setText(assignment.getTitle());
            tvDescription.setText(assignment.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dueDate = sdf.format(assignment.getDueDate());
            tvDueDate.setText(String.format("截止时间: %s", dueDate));

        }
    }
}