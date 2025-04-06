package cn.flyyz.gsupl.classinteractionsystem;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://api.flyyz.cn/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getClient(final SessionManager sessionManager) {
        if (apiService == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();



            // 创建OkHttpClient并添加拦截器
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);


            // 添加认证拦截器
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    // 如果用户已登录则添加token到请求头
                    if (sessionManager.isLoggedIn()) {
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", "Bearer " + sessionManager.getToken())
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }

                    return chain.proceed(original);
                }
            });

            OkHttpClient client = httpClient.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }
}