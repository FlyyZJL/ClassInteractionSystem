package cn.flyyz.gsupl.classinteractionsystem;

import java.io.Serializable;
import java.util.Map;

public class GradeStatistics implements Serializable {
    private int courseId;
    private String courseName;
    private double averageScore;
    private Map<String, Integer> distribution;
    private int totalStudents;
    private int passedStudents;
    private double passRate;

    // Getters and Setters
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public Map<String, Integer> getDistribution() { return distribution; }
    public void setDistribution(Map<String, Integer> distribution) { this.distribution = distribution; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getPassedStudents() { return passedStudents; }
    public void setPassedStudents(int passedStudents) { this.passedStudents = passedStudents; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
}