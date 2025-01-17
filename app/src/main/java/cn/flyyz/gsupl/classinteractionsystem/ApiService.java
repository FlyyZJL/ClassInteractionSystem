package cn.flyyz.gsupl.classinteractionsystem;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("register")
    Call<ResponseBody> registerUser(@Body User user);

    @POST("login")
    Call<ResponseBody> loginUser(@Body User user);
}
