package cn.flyyz.gsupl.classinteractionsystem;

import com.google.gson.annotations.SerializedName;

public class SubmissionResult {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // Getters

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}