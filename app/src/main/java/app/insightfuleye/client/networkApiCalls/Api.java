package app.insightfuleye.client.networkApiCalls;

import org.checkerframework.framework.qual.PolyAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.insightfuleye.client.models.Data;
import app.insightfuleye.client.models.Results;
import app.insightfuleye.client.models.dto.LocationDTO;
import app.insightfuleye.client.models.loginModel.PostSignIn;
import app.insightfuleye.client.models.loginModel.Signin;
import app.insightfuleye.client.models.pushRequestApiCall.Encounter;
import app.insightfuleye.client.models.pushRequestApiCall.Person;
import app.insightfuleye.client.models.pushRequestApiCall.Visit;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface Api {

    @POST("/api/v2/signin")
    Observable<Signin> signIn(@Body PostSignIn postSignIn);

    @POST("/api/v2/signin/refreshtoken")
    Observable<Signin> refreshToken(@Body PostSignIn postSignIn);

    @GET("/api/v2/location")
    Observable<Results> getLocations();

    @POST("/api/v2/person")
    Observable<Results<Data>> postPatients(@Body Person person);

    @POST("/api/v2/visit")
    Observable<Results<Data>> postVisits(@Body Visit visit);

    @POST("/api/v2/encounter")
    Observable<Results<Data>> postEncounters(@Body Encounter encounter);

    @Multipart
    @POST("api/v2/image")
    Observable<ResponseBody> uploadImageAsync(@Part MultipartBody.Part[] part, @Part("visit_id") RequestBody requestBody1, @Part("patient_id") RequestBody requestBody2, @Part("type") RequestBody requestBody3);

    @Multipart
    @POST("api/v2/image")
    Observable<ResponseBody> uploadImageAsync(
            @Part List<MultipartBody.Part> files,
            @PartMap Map<String, RequestBody> data
    );


}