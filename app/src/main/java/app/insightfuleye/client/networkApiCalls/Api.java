package app.insightfuleye.client.networkApiCalls;

import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.models.Data;
import app.insightfuleye.client.models.Results;
import app.insightfuleye.client.models.dto.LocationDTO;
import app.insightfuleye.client.models.loginModel.PostSignIn;
import app.insightfuleye.client.models.loginModel.Signin;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {
    @POST("/api/v2/signin")
    Observable<Signin> signIn(@Body PostSignIn postSignIn);

    @POST("/api/v2/signin/refreshToken")
    Observable<Signin> refreshToken(@Body PostSignIn postSignIn);

    @GET("/api/v2/location")
    Observable<Results<Data<List<LocationDTO>>>> getLocations();

}