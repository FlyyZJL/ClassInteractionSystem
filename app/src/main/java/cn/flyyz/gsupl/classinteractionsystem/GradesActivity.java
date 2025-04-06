package cn.flyyz.gsupl.classinteractionsystem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.flyyz.gsupl.classinteractionsystem.ApiService.ApiResponse;


import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.flyyz.gsupl.classinteractionsystem.ApiService.Grade;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradesActivity extends AppCompatActivity implements GradeListAdapter.OnGradeClickListener {
    private static final String TAG = "GradesActivity";

    private RecyclerView recyclerView;
    private GradeListAdapter adapter;
    private List<Grade> gradeList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    private Spinner spinnerCourses;
    private SearchView searchView;
    private Chip chipAll, chipExcellent, chipPass, chipFail;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private int currentCourseId = -1;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("成绩管理");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 初始化视图
        spinnerCourses = findViewById(R.id.spinnerCourses);
        searchView = findViewById(R.id.searchView);
        chipAll = findViewById(R.id.chipAll);
        chipExcellent = findViewById(R.id.chipExcellent);
        chipPass = findViewById(R.id.chipPass);
        chipFail = findViewById(R.id.chipFail);
        fabAdd = findViewById(R.id.fabAdd);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        textEmpty = findViewById(R.id.textEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeListAdapter(this, gradeList, this);
        recyclerView.setAdapter(adapter);

        // 加载教师课程
        loadTeacherCourses();

        // 设置筛选监听
        setupFilters();

        // 悬浮按钮点击事件
        fabAdd.setOnClickListener(v -> {
            if (currentCourseId > 0) {
                Intent intent = new Intent(GradesActivity.this, AddEditGradeActivity.class);
                intent.putExtra("courseId", currentCourseId);
                startActivity(intent);
            } else {
                Toast.makeText(GradesActivity.this, "请先选择一个课程", Toast.LENGTH_SHORT).show();
            }
        });
        Button btnStatistics = findViewById(R.id.btnStatistics);
        btnStatistics.setOnClickListener(v -> {
            Course selectedCourse = (Course) spinnerCourses.getSelectedItem();
            if (selectedCourse != null) {
                openStatisticsPage(selectedCourse.getCourseId());
            } else {
                Toast.makeText(GradesActivity.this, "请先选择课程", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (currentCourseId > 0) {
            loadCourseGrades(currentCourseId);
        }
    }

    // 加载教师课程列表
    private void loadTeacherCourses() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
        Log.d("grade1", "加载教师课程: teacherId=" + teacherId);
        apiService.getTeacherCourses(teacherId).enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    courseList = response.body().getData();
                    setupCourseSpinner();
                } else {
                    Toast.makeText(GradesActivity.this, "获取课程列表失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "网络错误: " + t.getMessage());
                Toast.makeText(GradesActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 设置课程下拉框
    private void setupCourseSpinner() {
        if (courseList == null || courseList.isEmpty()) {
            spinnerCourses.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"暂无课程"}));
            fabAdd.setEnabled(false);
            textEmpty.setText("您没有任何课程");
            textEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // 创建自定义ArrayAdapter，只显示课程名称
        ArrayAdapter<Course> adapter = new ArrayAdapter<Course>(this, android.R.layout.simple_spinner_item, courseList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                Course course = getItem(position);
                // 只显示课程名
                text.setText(course != null ? course.getCourseName() : "");
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                Course course = getItem(position);
                // 只显示课程名
                text.setText(course != null ? course.getCourseName() : "");
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapter);

        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Course selectedCourse = (Course) parent.getItemAtPosition(position);
                currentCourseId = selectedCourse.getCourseId();
                loadCourseGrades(currentCourseId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });
    }
    /**
     * 打开统计页面
     */
    private void openStatisticsPage(int courseId) {
        Intent intent = new Intent(this, CourseStatisticsActivity.class);
        intent.putExtra("courseId", courseId);
        startActivity(intent);
    }

    // 加载课程成绩
    private void loadCourseGrades(int courseId) {
        progressBar.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
        Log.d("grades11", "加载课程成绩: courseId=" + courseId + ", teacherId=" + teacherId);
        apiService.getCourseGrades(courseId, teacherId).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    gradeList.clear();
                    List<Grade> receivedGrades = response.body().getData();
                    if (receivedGrades != null) {
                        gradeList.addAll(receivedGrades);
                    }

                    adapter.updateData(gradeList);

                    if (gradeList.isEmpty()) {
                        textEmpty.setText("暂无成绩记录");
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        applyFilter(currentFilter);
                    }
                } else {
                    Toast.makeText(GradesActivity.this, "获取成绩列表失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "网络错误: " + t.getMessage());
                Toast.makeText(GradesActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 设置筛选监听
    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilter(currentFilter);
        });

        chipExcellent.setOnClickListener(v -> {
            currentFilter = Constants.GRADE_HIGH;
            applyFilter(currentFilter);
        });

        chipPass.setOnClickListener(v -> {
            currentFilter = Constants.GRADE_MEDIUM;
            applyFilter(currentFilter);
        });

        chipFail.setOnClickListener(v -> {
            currentFilter = Constants.GRADE_LOW;
            applyFilter(currentFilter);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    // 应用成绩等级筛选
    private void applyFilter(String filter) {
        adapter.setScoreFilter(filter);
    }

    // 成绩项点击事件
    @Override
    public void onGradeClick(Grade grade) {
        Intent intent = new Intent(this, GradeDetailActivity.class);
        intent.putExtra("gradeId", grade.getGradeId());
        Log.d("gradeid", "点击成绩: gradeId=" + grade.getGradeId());
        startActivity(intent);
    }

    // 显示导出选项对话框
    private void showExportOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导出成绩");
        builder.setItems(new String[]{"导出当前课程成绩 (CSV)"}, (dialog, which) -> {
            if (which == 0) {
                exportGrades();
            }
        });
        builder.show();
    }

    // 导出成绩
//    private void exportGrades() {
//        if (currentCourseId <= 0) {
//            Toast.makeText(this, "请先选择一个课程", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Toast.makeText(this, "开始导出成绩...", Toast.LENGTH_SHORT).show();
//        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
//        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
//        Map<String, String> options = new HashMap<>();
//        options.put("courseId", String.valueOf(currentCourseId));
//        options.put("format", "csv");
//        options.put("teacherId", String.valueOf(teacherId));
//
//
//
//        apiService.exportGrades(options).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    try {
//                        File file = saveFile(response.body(), "grades_export.csv");
//                        Toast.makeText(GradesActivity.this, "成绩导出成功，文件保存至: " + file.getPath(), Toast.LENGTH_LONG).show();
//                        Log.d("export", "导出文件路径: " + file.getPath());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Toast.makeText(GradesActivity.this, "保存文件失败", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(GradesActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                Log.e(TAG, "导出失败: " + t.getMessage());
//                Toast.makeText(GradesActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    // 导出成绩
    private void exportGrades() {
        if (currentCourseId <= 0) {
            Toast.makeText(this, "请先选择一个课程", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "开始导出成绩...", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));

        // 构建导出URL
        String url = "http://api.flyyz.cn/api/grades/export?"
                + "teacherId=" + teacherId
                + "&courseId=" + currentCourseId
                + "&format=csv";

        // 使用浏览器下载
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        // 添加异常处理
        try {
            startActivity(intent);
            Toast.makeText(this, "正在使用浏览器下载成绩", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "未找到可以处理此链接的应用，请确保设备已安装浏览器", Toast.LENGTH_LONG).show();
            Log.e(TAG, "浏览器启动失败: " + e.getMessage());
        }
    }

    // 保存文件
    private File saveFile(ResponseBody body, String filename) throws IOException {
        File downloadsDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        File file = new File(downloadsDir, filename);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();

            return file;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grades_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_export) {
            showExportOptions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}