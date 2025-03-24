package cn.flyyz.gsupl.classinteractionsystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiService {
    // 注册
    @POST("register")
    Call<ResponseBody> registerUser(@Body User user);
    // 登录
    @POST("login")
    Call<ResponseBody> loginUser(@Body User user);

    @POST("student/changePassword")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);

    // 获取学生信息
    @GET("student/info")
    Call<ResponseBody> getStudentInfo(@Query("username") String username);

    // 获取所有教师
    @GET("getTeachers")
    Call<ResponseBody> getTeachers();

    // 创建课程
    @POST("createCourse")
    Call<ResponseBody> createCourse(@Query("course_name") String courseName,
                                    @Query("course_description") String courseDescription,
                                    @Query("teacher_id") String teacherId);

    // 获取所有课程
    @GET("getCoursesByTeacher")
    Call<List<Course>> getCoursesByTeacher(@Query("teacherId") int teacherId);

    //获取所有学生
    @GET("getAllStudents")
    Call<List<Student>> getAllStudents();

    // 添加学生到课程
    @POST("addStudentsToCourse")
    Call<ResponseBody> addStudentsToCourse(@Body Map<String, Object> requestData);

    @GET("student/courses")
    Call<ApiResponse<List<Course>>> getStudentCourses(@Query("studentId") int studentId);

    // 教师发布作业
    @POST("teacher/assignments/publish")
    Call<JsonObject> publishAssignment(@Body JsonObject requestBody);

    // 获取学生作业
    @GET("api/student/assignments")
    Call<JsonObject> getStudentAssignments(@Query("studentId") int studentId);

    @Multipart
    @POST("api/student/assignments/submit")
    Call<JsonObject> submitAssignment(
            @Part("studentId") RequestBody studentId,
            @Part("assignmentId") RequestBody assignmentId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part file
    );

    // 教师批改相关接口
    @GET("api/teacher/pending-submissions")
    Call<ApiResponse<List<PendingSubmission>>> getPendingSubmissions(
            @Query("teacherId") int teacherId,
            @Query("role") String role
    );

    @GET("api/teacher/submission-detail")
    Call<ApiResponse<SubmissionDetail>> getSubmissionDetail(
            @Query("submissionId") int submissionId,
            @Query("teacherId") int teacherId,
            @Query("role") String role
    );

    @POST("api/teacher/grade-submission")
    Call<ApiResponse<Void>> submitGrade(@Body GradeRequest request);

    @Streaming // 大文件下载必须添加
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    // 新增统计信息接口
    @GET("api/teacher/submission-stats")
    Call<ApiResponse<SubmissionStats>> getSubmissionStats(
            @Query("teacherId") int teacherId,
            @Query("assignmentId") int assignmentId
    );


    // region 讨论区接口
    @GET("api/discussions")
    Call<JsonArray> getDiscussions(
            @Query("course_id") int courseId,
            @Query("user_id") int userId,
            @Query("user_role") String userRole
    );

    @POST("api/discussions/manage")
    Call<Void> manageDiscussion(
            @Query("discussion_id") int discussionId,
            @Query("user_id") int userId,
            @Query("user_role") String userRole,
            @Query("action") String action
    );

    @POST("api/discussions/post")
    Call<JsonObject> createDiscussion(
            @Body DiscussionPostRequest request
    );


    // 获取讨论帖详情
    @GET("api/discussions/detail")
    Call<JsonObject> getDiscussionDetail(
            @Query("discussion_id") int discussionId,
            @Query("user_id") int userId,
            @Query("user_role") String userRole
    );

    // 获取回复列表
    @GET("api/replies/list")
    Call<JsonArray> getReplies(
            @Query("discussion_id") int discussionId
    );

    // 创建新回复
    @POST("api/replies/post")
    Call<JsonObject> createReply(
            @Body ReplyPostRequest request
    );
    // 上传视频
    @Multipart
    @POST("upload/video")
    Call<JsonObject> uploadVideo(@Part MultipartBody.Part video);

    @POST("chapters")
    Call<Void> createChapter(@Body Chapter chapter);

    @GET("chapters")
    Call<List<Chapter>> getChapters(@Query("courseId") int courseId);

    // 数据模型
    class DiscussionPostRequest {
        @SerializedName("course_id")
        private int course_id;

        @SerializedName("user_id")
        private int user_id;

        @SerializedName("user_role")
        private String user_role;

        private String title;
        private String content;

        public DiscussionPostRequest(int course_id, int user_id, String user_role, String title, String content) {
            this.course_id = course_id;
            this.user_id = user_id;
            this.user_role = user_role;
            this.title = title;
            this.content = content;
        }

        // Getter方法...
        public int getCourseId() {
            return course_id;
        }

        public int getUserId() {
            return user_id;
        }

        public String getUserRole() {
            return user_role;
        }
        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

    }

    class ReplyPostRequest {
        @SerializedName("discussion_id")
        private int discussionId;

        @SerializedName("user_id")
        private int userId;

        @SerializedName("user_role")
        private String userRole;

        private String content;

        @SerializedName("parent_reply_id")
        private Integer parentReplyId;

        // 构造函数和Getter...

        public ReplyPostRequest(int discussionId, int userId, String userRole, String content, Integer parentReplyId) {
            this.discussionId = discussionId;
            this.userId = userId;
            this.userRole = userRole;
            this.content = content;
            this.parentReplyId = parentReplyId;
        }

        public int getDiscussionId() {
            return discussionId;
        }

        public int getUserId() {
            return userId;
        }

        public String getUserRole() {
            return userRole;
        }

        public String getContent() {
            return content;
        }

        public Integer getParentReplyId() {
            return parentReplyId;
        }

    }

    // 请求体数据类
    public class GradeRequest {
        private int teacherId;
        private String role;
        private int submissionId;
        private float score;
        private String feedback;

        // 构造函数
        public GradeRequest(int teacherId, int submissionId, float score, String feedback) {
            this.teacherId = teacherId;
            this.role = "teacher"; // 固定值
            this.submissionId = submissionId;
            this.score = score;
            this.feedback = feedback;
        }

        // Getter方法...

        public int getTeacherId() {
            return teacherId;
        }

        public String getRole() {
            return role;
        }

        public int getSubmissionId() {
            return submissionId;
        }

        public float getScore() {
            return score;
        }

        public String getFeedback() {
            return feedback;
        }

    }

    // 通用响应结构
    public class ApiResponse<T> {
        @SerializedName("success")
        private boolean isSuccess;
        private String message;
        private T data;

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }
    }
}
