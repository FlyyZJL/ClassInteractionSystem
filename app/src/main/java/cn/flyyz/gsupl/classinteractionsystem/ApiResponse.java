package cn.flyyz.gsupl.classinteractionsystem;

// 文件名：ApiResponse.java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Getter 和 Setter 方法
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}