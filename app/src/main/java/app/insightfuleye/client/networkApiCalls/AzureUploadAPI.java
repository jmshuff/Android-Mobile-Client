package app.insightfuleye.client.networkApiCalls;

import java.util.List;

import app.insightfuleye.client.models.ObsImageModel.ObsJsonResponse;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.azureResultsPush;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.adapter.rxjava2.Result;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AzureUploadAPI {
    @Multipart
    @POST("api/v1/image/")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part part, @Part("creatorId") RequestBody requestBody, @Part("visitId") RequestBody requestBody1, @Part("patientId") RequestBody requestBody2, @Part("type") RequestBody requestBody3, @Part("visual_acuity") RequestBody requestBody4, @Part("pinhole_acuity") RequestBody requestBody5, @Part("sex") RequestBody requestBody6, @Part("age") RequestBody requestBody7, @Part("complaints") RequestBody requestBody8);

    @GET("api/v1/image/")
    Call<List<azureResults>> getAzureImage();

    @Multipart
    @POST("api/v1/image/")
    Observable<ResponseBody> uploadImageAsync(@Part MultipartBody.Part part, @Part("creatorId") RequestBody requestBody, @Part("visitId") RequestBody requestBody1, @Part("patientId") RequestBody requestBody2, @Part("type") RequestBody requestBody3, @Part("visual_acuity") RequestBody requestBody4, @Part("pinhole_acuity") RequestBody requestBody5, @Part("sex") RequestBody requestBody6, @Part("age") RequestBody requestBody7, @Part("complaints") RequestBody requestBody8);

}
