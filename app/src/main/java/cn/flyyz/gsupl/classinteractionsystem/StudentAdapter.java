package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class StudentAdapter extends BaseAdapter {

    private Context context;
    private List<Student> studentList;
    private List<Integer> selectedStudentIds; // 保存选中学生的ID

    public StudentAdapter(Context context, List<Student> studentList, List<Integer> selectedStudentIds) {
        this.context = context;
        this.studentList = studentList;
        this.selectedStudentIds = selectedStudentIds;
    }

    @Override
    public int getCount() {
        return studentList.size();
    }

    @Override
    public Object getItem(int position) {
        return studentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
            holder = new ViewHolder();
            holder.tvStudentName = convertView.findViewById(R.id.tvStudentName);
            holder.tvEmail = convertView.findViewById(R.id.tvEmail);
            holder.cbSelectStudent = convertView.findViewById(R.id.cbSelectStudent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Student student = studentList.get(position);

        // 设置学生信息
        holder.tvStudentName.setText(student.getStudentName());
        holder.tvEmail.setText(student.getEmail());

        // 设置 CheckBox 的状态
        holder.cbSelectStudent.setChecked(selectedStudentIds.contains(student.getStudentId()));

        // 监听 CheckBox 的点击事件
        holder.cbSelectStudent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedStudentIds.contains(student.getStudentId())) {
                    selectedStudentIds.add(student.getStudentId());
                }
            } else {
                selectedStudentIds.remove((Integer) student.getStudentId());
            }
        });

        return convertView;
    }

    // ViewHolder 用于优化性能
    private static class ViewHolder {
        TextView tvStudentName;
        TextView tvEmail;
        CheckBox cbSelectStudent;
    }
}
