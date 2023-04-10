package app.insightfuleye.client.networkApiCalls;


import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import app.insightfuleye.client.utilities.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //


    private static OkHttpClient.Builder client = new OkHttpClient.Builder();
    private static String apiBaseUrl = "https://devapi.visilant.org";    //testing server
    private static Retrofit retrofit;

    public static String getApiBaseUrl() {
        return apiBaseUrl;
    }

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create());


    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;
        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(apiBaseUrl);

    }

    public static <S> S createService(Class<S> serviceClass) {

        MyServiceInterceptor headerInceptor= new MyServiceInterceptor();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        client.addInterceptor(headerInceptor);
        client.connectTimeout(70, TimeUnit.SECONDS);
        client.readTimeout(70, TimeUnit.SECONDS);
        client.writeTimeout(70, TimeUnit.SECONDS);
        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

}