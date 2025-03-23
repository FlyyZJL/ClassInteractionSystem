package cn.flyyz.gsupl.classinteractionsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishAssignmentActivity extends AppCompatActivity {
    private AutoCompleteTextView actvCourses;
    private EditText etTitle, etDescription;
    private Button btnDueDate, btnSubmit;
    private TextView tvDueDate;
    private ProgressBar progressBar;
    private List<Course> courses = new ArrayList<>();
    private Calendar dueDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_assignment);

        initViews();
        loadCourses();
        setupListeners();
    }

    private void initViews() {
        actvCourses = findViewById(R.id.actv_courses);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnDueDate = findViewById(R.id.btn_due_date);
        tvDueDate = findViewById(R.id.tv_due_date);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadCourses() {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        int teacherId = Integer.parseInt(getSharedPreferences("user_data", MODE_PRIVATE).getString("user_id",null));

        api.getCoursesByTeacher(teacherId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courses = response.body();
                    ArrayAdapter<Course> adapter = new ArrayAdapter<Course>(PublishAssignmentActivity.this,
                            android.R.layout.simple_dropdown_item_1line, courses) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView textView = (TextView) super.getView(position, convertView, parent);
                            Course course = getItem(position);

                            // 显示格式："课程名称 (教师: 教师名)"
                            textView.setText(course.getCourseName() + " (教师: " + course.getTeacherName() + ")");
                            return textView;
                        }
                    };
                    actvCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Course selectedCourse = (Course) parent.getItemAtPosition(position);
                            actvCourses.setText(selectedCourse.getCourseName() + " (教师: " + selectedCourse.getTeacherName() + ")", false);
                            actvCourses.setTag(selectedCourse);
                        }
                    });
                    actvCourses.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(PublishAssignmentActivity.this, "加载课程失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // 截止时间选择
        btnDueDate.setOnClickListener(v -> showDateTimePicker());

        // 提交按钮
        btnSubmit.setOnClickListener(v -> {
            Course selectedCourse = (Course) actvCourses.getTag();
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (validateInput(selectedCourse, title)) {
                publishAssignment(selectedCourse.getCourseId(), title, description);
            }
        });
    }

    private boolean validateInput(Course course, String title) {
        if (course == null) {
            Toast.makeText(this, "请选择课程", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (title.isEmpty()) {
            etTitle.setError("请输入标题");
            return false;
        }
        if (dueDateCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "截止时间不能早于当前时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDateTimePicker() {
        // 使用正确的Builder构造方法
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择日期")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // 处理日期选择结果
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            // 时间选择器
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE))
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                // 合并日期和时间
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                // 格式化显示
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                tvDueDate.setText(sdf.format(calendar.getTime()));

                // 保存时间到成员变量
                dueDateCalendar = calendar;
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
    private void publishAssignment(int courseId, String title, String description) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("courseId", courseId);
        requestBody.addProperty("title", title);
        requestBody.addProperty("description", description);
        requestBody.addProperty("dueDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(dueDateCalendar.getTime()));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Log.d("PublishAssignment", "Request Body: " + requestBody.toString());

        api.publishAssignment(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);


                Log.d("PublishAssignment", "Response Code: " + response.code());
                Log.d("PublishAssignment", "Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject result = response.body();
                    if (result.get("success").getAsBoolean()) {
                        Toast.makeText(PublishAssignmentActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PublishAssignmentActivity.this,
                                "发布失败：" + result.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(PublishAssignmentActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}