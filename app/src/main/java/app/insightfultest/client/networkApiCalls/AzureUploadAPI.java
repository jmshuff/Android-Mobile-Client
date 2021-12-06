package app.insightfultest.client.networkApiCalls;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AzureUploadAPI {
    @Multipart
    @POST("api/v1/image/")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part part, @Part("creatorId") RequestBody requestBody, @Part("visitId") RequestBody requestBody1, @Part("patientId") RequestBody requestBody2, @Part("type") RequestBody requestBody3);
}
