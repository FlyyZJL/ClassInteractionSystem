package cn.flyyz.gsupl.classinteractionsystem;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionStatsActivity extends AppCompatActivity {
    private PieChart pieChart;
    private TextView tvTotal;
    private TextView tvStatsDetail;
    private int currentAssignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_stats);

        // 获取作业ID
        currentAssignmentId = getIntent().getIntExtra("assignment_id", 0);

        // 初始化视图
        pieChart = findViewById(R.id.pieChart);
        tvTotal = findViewById(R.id.tvTotal);
        tvStatsDetail = findViewById(R.id.tvStatsDetail);

        // 配置图表样式
        setupPieChart();

        // 加载数据
        loadStatsData();
    }

    private void setupPieChart() {
        // 基础配置
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // 动画效果
        pieChart.animateY(1400, Easing.EaseInOutQuad);

        // 图例配置
        Legend l = pieChart.getLegend();
        l.setEnabled(false); // 禁用内置图例

        // 数据标签样式
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
    }

    private void loadStatsData() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        int teacherId = Integer.parseInt(prefs.getString("user_id", "0"));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getSubmissionStats(teacherId, currentAssignmentId)
                .enqueue(new Callback<ApiService.ApiResponse<SubmissionStats>>() {
                    @Override
                    public void onResponse(Call<ApiService.ApiResponse<SubmissionStats>> call,
                                           Response<ApiService.ApiResponse<SubmissionStats>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiService.ApiResponse<SubmissionStats> res = response.body();
                            if (res.isSuccess()) {
                                updateUI(res.getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.ApiResponse<SubmissionStats>> call, Throwable t) {
                        Toast.makeText(SubmissionStatsActivity.this,
                                "数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(SubmissionStats stats) {
        // 更新总数显示
        tvTotal.setText(String.valueOf(stats.getTotal()));

        // 准备饼图数据
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(stats.getSubmittedPercent(), "已提交"));
        entries.add(new PieEntry(stats.getUnsubmittedPercent(), "未提交"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f); // 切片间距
        dataSet.setSelectionShift(5f); // 选中突出距离

        // 颜色配置
        int[] colors = {
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.red)
        };
        dataSet.setColors(colors);

        // 数值显示格式
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        // 设置数据
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // 更新详细数据
        String detailText = String.format(Locale.CHINA,
                "总人数: %d\n\n"
                        + "✓ 已提交: %d (%.1f%%)\n\n"
                        + "✗ 未提交: %d (%.1f%%)",
                stats.getTotal(),
                stats.getSubmitted(), stats.getSubmittedPercent(),
                stats.getUnsubmitted(), stats.getUnsubmittedPercent()
        );
        tvStatsDetail.setText(detailText);

        // 刷新图表
        pieChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        // 释放图表资源
        if (pieChart != null) {
            pieChart.clear();
            pieChart = null;
        }
        super.onDestroy();
    }
}