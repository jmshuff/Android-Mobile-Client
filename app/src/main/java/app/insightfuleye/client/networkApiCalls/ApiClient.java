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


    private static OkHttpClient.Builder httpClient;
    private static String apiBaseUrl = "https://devapi.visilant.org";    //testing server

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
        if(httpClient== null){
            httpClient = new OkHttpClient.Builder();
            MyServiceInterceptor headerInceptor= new MyServiceInterceptor();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
            httpClient.addInterceptor(headerInceptor);
            httpClient.connectTimeout(70, TimeUnit.SECONDS);
            httpClient.readTimeout(70, TimeUnit.SECONDS);
            httpClient.writeTimeout(70, TimeUnit.SECONDS);

        }
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

}