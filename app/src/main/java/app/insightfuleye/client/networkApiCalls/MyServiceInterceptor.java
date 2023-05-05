package app.insightfuleye.client.networkApiCalls;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.DnsResolver;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

@Singleton
public class MyServiceInterceptor implements Interceptor {
    private static Context context;
    private static String authToken;
    private static long authExpiration;
    private static String refreshToken;

    @Inject
    @Override
    public Response intercept(Chain chain) throws IOException {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        authToken = sessionManager.getAuthToken();
        if (authToken != null && !authToken.isEmpty()) {
            Request request = newRequestWithAccessToken(chain.request(), authToken);
            Response response = chain.proceed(request);
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                response.body().close();
                synchronized (this) {
                    String currentToken = sessionManager.getAuthToken();
                    if (currentToken != null && currentToken == authToken) {
                        refreshToken();
                    }
                }
                Request newRequest = newRequestWithAccessToken(chain.request(), sessionManager.getAuthToken());
                // retry the request
                return chain.proceed(newRequest);
            }
            return response;
        } else {
            return chain.proceed(chain.request());
        }
    }

    @NonNull
    private Request newRequestWithAccessToken(@NonNull Request request, @NonNull String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }

    public void refreshToken() {
        System.out.println("refreshToken");
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        Api api = ApiClient.createService(Api.class);
        PostSignIn postSignIn = new PostSignIn();
        postSignIn.setRefreshToken(sessionManager.getRefreshToken());
        Observable<Signin> loginModelObservable = api.refreshToken(postSignIn);
        loginModelObservable.subscribe(new Observer<Signin>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(@NonNull Signin signin) {
                Log.d("signin", String.valueOf(signin.getSuccess()));
                Boolean authencated = signin.getSuccess();
                if (authencated) {
                    sessionManager.setAuthToken(signin.getData().getToken());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d("refresh token failed", "true");
                synchronized (this){
                    renewSignIn();
                }
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void renewSignIn() {
        PostSignIn postSignIn = new PostSignIn("admin@visilant.com", "test");

        Api api = ApiClient.createService(Api.class);
        Observable<Signin> loginModelObservable = api.signIn(postSignIn);
        loginModelObservable.subscribe(new Observer<Signin>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(@NonNull Signin signin) {
                Log.d("signin", String.valueOf(signin.getSuccess()));
                Boolean authencated = signin.getSuccess();
                SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
                if (authencated) {
                    sessionManager.setSetupComplete(true);
                    sessionManager.setChwname(signin.getData().getUser().getUsername());
                    sessionManager.setCreatorID(signin.getData().getUser().getId());
                    sessionManager.setProviderID(signin.getData().getUser().getPersonId());
                    sessionManager.setAuthToken(signin.getData().getToken());
                    sessionManager.setRefreshToken(signin.getData().getRefreshToken());
                    SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                    //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                    sqLiteDatabase.beginTransaction();
                    //read_db.beginTransaction();
                    ContentValues values = new ContentValues();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
