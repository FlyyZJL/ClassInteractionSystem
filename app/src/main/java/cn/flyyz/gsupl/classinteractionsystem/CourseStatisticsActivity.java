package cn.flyyz.gsupl.classinteractionsystem;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import cn.flyyz.gsupl.classinteractionsystem.ApiService.ApiResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseStatisticsActivity extends AppCompatActivity {

    private ApiService apiService;
    private int courseId;

    private TextView tvCourseName;
    private TextView tvAverageScore;
    private TextView tvPassRate;
    private TextView tvTotalStudents;
    private BarChart barChart;
    private ProgressBar progressBar;

    private DecimalFormat decimalFormat = new DecimalFormat("0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_statistics);

        // 设置顶部返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("课程统计");
        }

        // 初始化视图
        tvCourseName = findViewById(R.id.tvCourseName);
        tvAverageScore = findViewById(R.id.tvAverageScore);
        tvPassRate = findViewById(R.id.tvPassRate);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        barChart = findViewById(R.id.barChart);
        progressBar = findViewById(R.id.progressBar);

        // 获取课程ID
        Intent intent = getIntent();
        courseId = intent.getIntExtra("courseId", -1);

        if (courseId == -1) {
            Toast.makeText(this, "课程ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化API服务
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 加载统计数据
        loadCourseStatistics();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 加载课程统计数据
     */
    private void loadCourseStatistics() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getCourseStatistics(courseId).enqueue(new Callback<ApiResponse<GradeStatistics>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GradeStatistics>> call,
                                   @NonNull Response<ApiResponse<GradeStatistics>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    GradeStatistics stats = response.body().getData();
                    if (stats != null) {
                        displayStatistics(stats);
                    } else {
                        Toast.makeText(CourseStatisticsActivity.this, "获取统计数据失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "网络错误";
                    Toast.makeText(CourseStatisticsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<GradeStatistics>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("CourseStats", "统计数据加载失败", t);
                Toast.makeText(CourseStatisticsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示统计数据
     */
    private void displayStatistics(GradeStatistics stats) {
        // 设置基本信息
        tvCourseName.setText(stats.getCourseName());
        tvAverageScore.setText(decimalFormat.format(stats.getAverageScore()));
        tvPassRate.setText(decimalFormat.format(stats.getPassRate()) + "%");
        tvTotalStudents.setText(String.valueOf(stats.getTotalStudents()));

        // 设置柱状图
        setupBarChart(stats.getDistribution());
    }

    /**
     * 设置柱状图
     */
    private void setupBarChart(Map<String, Integer> distribution) {
        // 准备数据
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        // 按照固定顺序添加数据，确保图表从左到右是：不及格、及格、中等、良好、优秀
        String[] categories = {"不及格(<60)", "及格(60-69)", "中等(70-79)", "良好(80-89)", "优秀(90-100)"};

        for (String category : categories) {
            Integer value = distribution.get(category);
            entries.add(new BarEntry(index, value != null ? value : 0));
            labels.add(category);
            index++;
        }

        // 创建数据集
        BarDataSet dataSet = new BarDataSet(entries, "成绩分布");

        // 设置颜色和样式
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        // 创建BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // 设置X轴
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(45f);

        // 设置Y轴
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0);

        barChart.getAxisRight().setEnabled(false);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}