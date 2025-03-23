package cn.flyyz.gsupl.classinteractionsystem;

public class SubmissionStats {
    private int total;
    private int submitted;
    private int unsubmitted;

    // 计算百分比
    public float getSubmittedPercent() {
        return total == 0 ? 0 : submitted * 100f / total;
    }

    public float getUnsubmittedPercent() {
        return total == 0 ? 0 : unsubmitted * 100f / total;
    }

    // Getters and Setters
    public int getTotal() { return total; }
    public int getSubmitted() { return submitted; }
    public int getUnsubmitted() { return unsubmitted; }
    // ... 省略Setter方法
}