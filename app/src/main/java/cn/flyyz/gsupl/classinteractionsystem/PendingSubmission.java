package cn.flyyz.gsupl.classinteractionsystem;

import com.google.gson.annotations.SerializedName;

/**
 * 待批改作业列表项模型
 */
public class PendingSubmission {
    @SerializedName("submissionId")
    private int id;

    @SerializedName("studentName")
    private String student;

    @SerializedName("title")
    private String assignmentTitle;

    @SerializedName("submitTime")
    private long submitTimestamp;

    @SerializedName("hasFile")
    private boolean hasAttachment;

    // Getters
    public int getId() { return id; }
    public String getStudent() { return student; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public long getSubmitTimestamp() { return submitTimestamp; }
    public boolean hasAttachment() { return hasAttachment; }

    // Setters

    public void setId(int id) {
        this.id = id;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public void setSubmitTimestamp(long submitTimestamp) {
        this.submitTimestamp = submitTimestamp;
    }

    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public PendingSubmission(int id, String student, String assignmentTitle, long submitTimestamp, boolean hasAttachment) {
        this.id = id;
        this.student = student;
        this.assignmentTitle = assignmentTitle;
        this.submitTimestamp = submitTimestamp;
        this.hasAttachment = hasAttachment;
    }
}