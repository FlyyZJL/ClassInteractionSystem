package cn.flyyz.gsupl.classinteractionsystem;

import java.util.List;

public class ApiResponse2<T> {
    private boolean success;
    private String message;
    private T data;
    private List<T> dataList;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public List<T> getDataList() { return dataList; }
    public void setDataList(List<T> dataList) { this.dataList = dataList; }
}