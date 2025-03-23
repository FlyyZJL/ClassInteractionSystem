package cn.flyyz.gsupl.classinteractionsystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
        private static final String BASE_URL = "http://192.168.1.11:9999/demo_war_exploded/";
        private static final long CONNECT_TIMEOUT = 30; // 秒
        private static final long READ_TIMEOUT = 30;
        private static final long WRITE_TIMEOUT = 60; // 文件上传需要更长时间

        private static Retrofit retrofit;
        private static OkHttpClient okHttpClient;

        // 私有构造方法
        private RetrofitClient() {}

        public static synchronized Retrofit getClient() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getOkHttpClient())
                        .addConverterFactory(GsonConverterFactory.create(getGson()))
                        .build();
            }
            return retrofit;
        }

        private static OkHttpClient getOkHttpClient() {
            if (okHttpClient == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);

                // 添加公共请求头
                builder.addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Android-Client/1.0")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                });

                okHttpClient = builder.build();
            }
            return okHttpClient;
        }

        private static Gson getGson() {
            return new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss") // 与服务端日期格式一致
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();
        }

        // 自定义日期解析器
        private static class DateDeserializer implements JsonDeserializer<Date> {
            private final List<String> dateFormats = Arrays.asList(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
            );

            @Override
            public Date deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context)
                    throws JsonParseException {

                String dateString = json.getAsString();
                for (String format : dateFormats) {
                    try {
                        return new SimpleDateFormat(format, Locale.getDefault())
                                .parse(dateString);
                    } catch (ParseException ignored) {}
                }
                throw new JsonParseException("无法解析日期: " + dateString);
            }
        }
    }