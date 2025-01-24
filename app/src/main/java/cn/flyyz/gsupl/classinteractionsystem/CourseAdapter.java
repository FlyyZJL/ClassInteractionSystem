package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

public class CourseAdapter extends BaseAdapter {

    private Context context;
    private List<Course> courseList;
    private LayoutInflater inflater;

    public CourseAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Override
    public Object getItem(int position) {
        return courseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_course, parent, false);
        }

        TextView tvCourseName = convertView.findViewById(R.id.tvCourseName);
        TextView tvCourseDescription = convertView.findViewById(R.id.tvCourseDescription);

        Course course = courseList.get(position);

        tvCourseName.setText(course.getCourseName());
        tvCourseDescription.setText(course.getDescription());

        return convertView;
    }
}