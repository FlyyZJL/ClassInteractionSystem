package cn.flyyz.gsupl.classinteractionsystem;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("register")
    Call<ResponseBody> registerUser(@Body User user);

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




}
