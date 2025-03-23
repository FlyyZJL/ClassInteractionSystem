package cn.flyyz.gsupl.classinteractionsystem;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentCoursesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private CourseAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_courses);

        // 初始化视图
        recyclerView = findViewById(R.id.rv_courses);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter();
        recyclerView.setAdapter(adapter);

        // 设置下拉刷新
        swipeRefresh.setOnRefreshListener(this::loadCourses);

        // 首次加载数据
        loadCourses();
    }

    private void loadCourses() {
        String userId = sharedPreferences.getString("user_id", null);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getStudentCourses(Integer.parseInt(userId)).enqueue(new Callback<ApiService.ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Course>>> call,
                                   Response<ApiService.ApiResponse<List<Course>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse<List<Course>> result = response.body();
                    if (result.isSuccess()) {
                        adapter.submitList(result.getData());
                    } else {
                        showError(result.getMessage());
                    }
                } else {
                    showError("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Course>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("网络错误: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // RecyclerView Adapter
    private class CourseAdapter extends RecyclerView.Adapter<CourseViewHolder> {
        private List<Course> courses = new ArrayList<>();

        void submitList(List<Course> newCourses) {
            courses = newCourses;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_course_card, parent, false);
            return new CourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            Course course = courses.get(position);
            holder.bind(course);
        }

        @Override
        public int getItemCount() {
            return courses.size();
        }
    }

    // ViewHolder
    static class CourseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseName;
        private final TextView tvTeacher;
        private final TextView tvEmail;
        private final TextView tvDescription;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvTeacher = itemView.findViewById(R.id.tv_teacher);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        void bind(Course course) {
            tvCourseName.setText(course.getCourseName());
            tvTeacher.setText(String.format("教师：%s", course.getTeacherName()));
            tvEmail.setText(course.getTeacherEmail());
            tvDescription.setText(course.getDescription());
        }
    }
}