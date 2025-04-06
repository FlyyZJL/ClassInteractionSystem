package cn.flyyz.gsupl.classinteractionsystem;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import cn.flyyz.gsupl.classinteractionsystem.ApiService.Grade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradeListAdapter extends RecyclerView.Adapter<GradeListAdapter.ViewHolder> {

    private Context context;
    private List<Grade> originalList;
    private List<Grade> filteredList;
    private OnGradeClickListener listener;
    private String currentScoreFilter = "all";
    private String searchQuery = "";

    public interface OnGradeClickListener {
        void onGradeClick(Grade grade);
    }

    public GradeListAdapter(Context context, List<Grade> gradeList, OnGradeClickListener listener) {
        this.context = context;
        this.originalList = gradeList != null ? gradeList : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grade grade = filteredList.get(position);

        // 添加 null 检查
        holder.tvStudentName.setText(grade.getStudentName() != null ? grade.getStudentName() : "");
        holder.tvGradeType.setText(grade.getGradeType() != null ? grade.getGradeType() : "");
        holder.tvScore.setText(String.valueOf(grade.getScore()));

        // 设置成绩颜色样式
        int colorRes;
        if (grade.getScore() >= 90) {
            colorRes = R.color.colorGradeHigh;
        } else if (grade.getScore() >= 60) {
            colorRes = R.color.colorGradeMedium;
        } else {
            colorRes = R.color.colorGradeLow;
        }
        holder.tvScore.setTextColor(ContextCompat.getColor(context, colorRes));


    // 设置日期 - 添加 null 检查和格式化
        if (grade.getGradeDate() != null && !grade.getGradeDate().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date date = inputFormat.parse(grade.getGradeDate());
                holder.tvDate.setText(outputFormat.format(date));
            } catch (ParseException e) {
                // 如果解析失败，显示原始字符串
                holder.tvDate.setText(grade.getGradeDate());
            }
        } else {
            holder.tvDate.setText("");
        }

        // 设置点击事件
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGradeClick(grade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // 设置成绩等级筛选
    public void setScoreFilter(String filter) {
        this.currentScoreFilter = filter != null ? filter : "all";
        applyFilters();
    }

    // 搜索筛选
    public void filter(String query) {
        this.searchQuery = query != null ? query.toLowerCase() : "";
        applyFilters();
    }

    // 应用所有筛选条件
    private void applyFilters() {
        filteredList.clear();

        for (Grade grade : originalList) {
            // 成绩等级筛选
            boolean matchesScoreFilter = currentScoreFilter.equals("all") ||
                    (currentScoreFilter.equals(Constants.GRADE_HIGH) && grade.getScore() >= 90) ||
                    (currentScoreFilter.equals(Constants.GRADE_MEDIUM) && grade.getScore() >= 60 && grade.getScore() < 90) ||
                    (currentScoreFilter.equals(Constants.GRADE_LOW) && grade.getScore() < 60);

            // 搜索关键字筛选
            boolean matchesSearch = searchQuery.isEmpty();

            if (!matchesSearch && grade.getStudentName() != null) {
                matchesSearch = grade.getStudentName().toLowerCase().contains(searchQuery);
            }

            if (!matchesSearch && grade.getGradeType() != null) {
                matchesSearch = grade.getGradeType().toLowerCase().contains(searchQuery);
            }

            // 两个条件都满足才添加
            if (matchesScoreFilter && matchesSearch) {
                filteredList.add(grade);
            }
        }

        notifyDataSetChanged();
    }

    // 更新数据
    public void updateData(List<Grade> newList) {
        this.originalList = new ArrayList<>(newList != null ? newList : new ArrayList<>());
        applyFilters();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvStudentName;
        TextView tvGradeType;
        TextView tvScore;
        TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvGradeType = itemView.findViewById(R.id.tvGradeType);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}