package cn.flyyz.gsupl.classinteractionsystem;

import com.google.gson.annotations.SerializedName;

/**
 * 作业提交详情模型
 */
// SubmissionDetail.java
public class SubmissionDetail {
    @SerializedName("content")
    private String content;

    @SerializedName("filePath")
    private String filePath;

    @SerializedName("studentName")
    private String studentName;

    @SerializedName("dueDate")
    private long dueDate;

    @SerializedName("submitTime")
    private long submitTime;

    // Getter方法
    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
    public String getStudentName() { return studentName; }
    public long getDueDate() { return dueDate; }
    public long getSubmitTime() { return submitTime; }

    public boolean isSuccess() {
        return false;
    }
}