package cn.flyyz.gsupl.classinteractionsystem;


public class Constants {
    // 成绩等级常量
    public static final String GRADE_HIGH = "high";     // 优秀成绩，>=90
    public static final String GRADE_MEDIUM = "medium"; // 及格成绩，>=60 && <90
    public static final String GRADE_LOW = "low";       // 不及格成绩，<60

    // 成绩类型常量
    public static final String[] GRADE_TYPES = {
            "期中考试", "期末考试", "平时作业", "课堂表现", "实验报告", "项目成绩", "总评成绩"
    };
}