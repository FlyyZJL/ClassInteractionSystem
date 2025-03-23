package cn.flyyz.gsupl.classinteractionsystem;

import java.math.BigDecimal;

public class GradeRequest {
    private int teacherId;
    private String role;
    private int submissionId;
    private BigDecimal score;
    private String feedback;

    // 构造函数
    public GradeRequest(int teacherId, int submissionId, BigDecimal score, String feedback) {
        this.teacherId = teacherId;
        this.role = "teacher";
        this.submissionId = submissionId;
        this.score = score;
        this.feedback = feedback;
    }

    // Getter/Setter...
}
