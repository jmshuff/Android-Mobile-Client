package app.insightfuleye.client.networkApiCalls;


import android.os.Build;

import com.google.android.gms.common.api.Api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AzureNetworkClient {
    private static Retrofit retrofit;
    private static String BASE_URL="http://testing.visilant.org:3006";
    private static AzureNetworkClient instance = null;
    private Api myApi;
    public static Retrofit getRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).
                    addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }
        return retrofit;
    }

}