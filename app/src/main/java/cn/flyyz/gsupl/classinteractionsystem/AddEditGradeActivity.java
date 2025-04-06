package cn.flyyz.gsupl.classinteractionsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cn.flyyz.gsupl.classinteractionsystem.ApiService.Grade;
import cn.flyyz.gsupl.classinteractionsystem.ApiService.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditGradeActivity extends AppCompatActivity {

    private ApiService apiService;
    private SessionManager sessionManager;

    private Spinner spinnerStudents;
    private Spinner spinnerGradeType;
    private EditText etScore;
    private EditText etFeedback;
    private Button btnSubmit;

    private int courseId;
    private int gradeId = 0;
    private boolean isEditMode = false;

    private List<User> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_grade);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(sessionManager);

        // 初始化视图
        spinnerStudents = findViewById(R.id.spinnerStudents);
        spinnerGradeType = findViewById(R.id.spinnerGradeType);
        etScore = findViewById(R.id.etScore);
        etFeedback = findViewById(R.id.etFeedback);
        btnSubmit = findViewById(R.id.btnSubmit);

        // 设置成绩类型下拉框
        setupGradeTypeSpinner();

        // 获取传递的参数
        if (getIntent().hasExtra("courseId")) {
            courseId = getIntent().getIntExtra("courseId", 0);

            if (getIntent().hasExtra("gradeId")) {
                // 编辑模式
                gradeId = getIntent().getIntExtra("gradeId", 0);
                isEditMode = true;
                getSupportActionBar().setTitle("编辑成绩");
                loadGradeDetails(gradeId);
            } else {
                // 添加模式
                getSupportActionBar().setTitle("添加成绩");
            }

            // 加载课程学生列表
            loadCourseStudents(courseId);
        } else {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> submitGrade());
    }

    // 设置成绩类型下拉框
    private void setupGradeTypeSpinner() {
        String[] gradeTypes = new String[]{
                "期中考试", "期末考试", "平时作业", "课堂表现", "实验报告", "项目成绩", "总评成绩"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, gradeTypes);
        spinnerGradeType.setAdapter(adapter);
    }

    // 加载课程学生列表
    private void loadCourseStudents(int courseId) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        Log.d("AddEditGradeActivity", "Loading students for course ID: " + courseId);
        apiService.getCourseStudents(courseId).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call,
                                   @NonNull Response<ApiResponse<List<User>>> response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    studentList = response.body().getData();
                    setupStudentSpinner();
                } else {
                    Toast.makeText(AddEditGradeActivity.this, "获取学生列表失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(AddEditGradeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 设置学生下拉框
    private void setupStudentSpinner() {
        if (studentList == null || studentList.isEmpty()) {
            spinnerStudents.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"暂无学生"}));
            return;
        }
        // 创建自定义ArrayAdapter，只显示学生用户名
        ArrayAdapter<User> adapter = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, studentList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                User user = getItem(position);
                // 只显示用户名
                text.setText(user != null ? user.getUsername() : "");
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                User user = getItem(position);
                // 只显示用户名
                text.setText(user != null ? "学生:"+user.getUsername() : "");
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapter);
    }

    // 加载成绩详情（编辑模式）
    private void loadGradeDetails(int gradeId) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
        apiService.getGradeDetail(gradeId, teacherId).enqueue(new Callback<ApiResponse<Grade>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Grade>> call,
                                   @NonNull Response<ApiResponse<Grade>> response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Grade grade = response.body().getData();
                    populateForm(grade);
                } else {
                    Toast.makeText(AddEditGradeActivity.this, "获取成绩详情失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Grade>> call, @NonNull Throwable t) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(AddEditGradeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // 填充表单（编辑模式）
    private void populateForm(Grade grade) {
        // 设置成绩分数和评语
        etScore.setText(String.valueOf(grade.getScore()));
        etFeedback.setText(grade.getFeedback());

        // 设置成绩类型
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGradeType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(grade.getGradeType())) {
                spinnerGradeType.setSelection(i);
                break;
            }
        }

        // 学生选择需要等待学生列表加载完毕后再设置
        spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (studentList != null && !studentList.isEmpty()) {
                    User selectedStudent = (User) parent.getItemAtPosition(position);
                    if (selectedStudent.getUserId() == grade.getStudentId()) {
                        spinnerStudents.setOnItemSelectedListener(null); // 移除监听器，防止循环调用
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });

        // 设置学生选择项
        if (studentList != null) {
            for (int i = 0; i < studentList.size(); i++) {
                if (studentList.get(i).getUserId() == grade.getStudentId()) {
                    spinnerStudents.setSelection(i);
                    break;
                }
            }
        }
    }

    // 提交成绩
    private void submitGrade() {
        // 验证表单
        if (spinnerStudents.getSelectedItem() == null ||
                spinnerGradeType.getSelectedItem() == null ||
                etScore.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double score = Double.parseDouble(etScore.getText().toString().trim());
            if (score < 0 || score > 100) {
                Toast.makeText(this, "分数必须在0-100之间", Toast.LENGTH_SHORT).show();
                return;
            }

            // 准备成绩数据
            Grade grade = new Grade();
            grade.setCourseId(courseId);

            // 获取选中的学生ID
            User selectedStudent = (User) spinnerStudents.getSelectedItem();
            grade.setStudentId(selectedStudent.getUserId());

            // 获取选中的成绩类型
            grade.setGradeType(spinnerGradeType.getSelectedItem().toString());

            grade.setScore(score);
            grade.setFeedback(etFeedback.getText().toString().trim());
            SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
            int teacherId = Integer.parseInt(sharedPreferences.getString("user_id", "-1"));
            grade.setGradedBy(teacherId);

            btnSubmit.setEnabled(false);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            if (isEditMode) {
                // 更新成绩
                grade.setGradeId(gradeId);
                apiService.updateGrade(gradeId, grade).enqueue(new Callback<ApiResponse<Grade>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Grade>> call,
                                           @NonNull Response<ApiResponse<Grade>> response) {
                        handleSubmitResponse(response, "成绩已更新");
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Grade>> call, @NonNull Throwable t) {
                        handleSubmitError(t);
                    }
                });
            } else {
                // 添加成绩
                apiService.addGrade(grade).enqueue(new Callback<ApiResponse<Grade>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Grade>> call,
                                           @NonNull Response<ApiResponse<Grade>> response) {
                        handleSubmitResponse(response, "成绩已添加");
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Grade>> call, @NonNull Throwable t) {
                        handleSubmitError(t);
                    }
                });
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的分数", Toast.LENGTH_SHORT).show();
        }
    }

    // 处理提交响应
    private void handleSubmitResponse(Response<ApiResponse<Grade>> response, String successMessage) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        btnSubmit.setEnabled(true);

        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            Toast.makeText(AddEditGradeActivity.this, successMessage, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String errorMsg = response.body() != null ? response.body().getMessage() : "操作失败";
            Toast.makeText(AddEditGradeActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    // 处理提交错误
    private void handleSubmitError(Throwable t) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
        Toast.makeText(AddEditGradeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    }