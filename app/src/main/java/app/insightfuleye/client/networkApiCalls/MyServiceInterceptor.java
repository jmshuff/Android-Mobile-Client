package app.insightfuleye.client.networkApiCalls;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.activities.setupActivity.SetupActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.models.loginModel.PostSignIn;
import app.insightfuleye.client.models.loginModel.Signin;
import app.insightfuleye.client.utilities.DialogUtils;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringEncryption;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class MyServiceInterceptor implements Interceptor {
    private static Context context;
    private static String authToken;
    private static long authExpiration;
    private static String refreshToken;

    @Inject
    @Override public Response intercept(Chain chain) throws IOException {
        Log.d("intercept", "1");
        SessionManager sessionManager=new SessionManager(IntelehealthApplication.getAppContext());
        Log.d("intercept", "2");
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();
        authToken=sessionManager.getAuthToken();
        if (authToken!=null && !authToken.isEmpty()){
            requestBuilder.header("Authorization", "Bearer " + sessionManager.getAuthToken());
            Response response=chain.proceed(requestBuilder.build());
            if(response.code()==401){
                refreshToken();
                requestBuilder.removeHeader("Authorization");
                requestBuilder.header("Authorization", "Bearer " + sessionManager.getAuthToken());
                response=chain.proceed(requestBuilder.build());
            }
            return response;
        }
        else{
            return chain.proceed(requestBuilder.build());
        }
    }
    public void refreshToken(){
        System.out.println("refreshToken");
        SessionManager sessionManager=new SessionManager(IntelehealthApplication.getAppContext());
        Api api = ApiClient.createService(Api.class);
        PostSignIn postSignIn= new PostSignIn(sessionManager.getRefreshToken());
        Observable<Signin> loginModelObservable= api.signIn(postSignIn);
        loginModelObservable.subscribe(new Observer<Signin>() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onNext(@NonNull Signin signin) {
                Log.d("signin", String.valueOf(signin.getSuccess()));
                Boolean authencated = signin.getSuccess();
                if(authencated) {
                    sessionManager.setAuthToken(signin.getData().getToken());
                }
            }


            @Override
            public void onError(@NonNull Throwable e) {
                Log.d("refresh token failed", "true");
            }

            @Override
            public void onComplete() {

            }
    });
    }

}