package app.insightfuleye.client.database.dao;

import android.util.ArrayMap;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.models.ObsImageModel.ObsJsonResponse;
import app.insightfuleye.client.models.ObsImageModel.ObsPushDTO;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.patientImageModelRequest.PatientProfile;
import app.insightfuleye.client.models.uploadImage;
import app.insightfuleye.client.networkApiCalls.AzureNetworkClient;
import app.insightfuleye.client.networkApiCalls.AzureUploadAPI;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.UrlModifiers;
import app.insightfuleye.client.utilities.exception.DAOException;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ImagesPushDAO {
    String TAG = ImagesPushDAO.class.getSimpleName();
    SessionManager sessionManager = null;



    public boolean patientProfileImagesPush() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setPatientProfileImageUrl();
        List<PatientProfile> patientProfiles = new ArrayList<>();
        try {
            patientProfiles = imagesDAO.getPatientProfileUnsyncedImages();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        for (PatientProfile p : patientProfiles) {
            Single<ResponseBody> personProfilePicUpload = AppConstants.apiInterface.PERSON_PROFILE_PIC_UPLOAD(url, "Basic " + encoded, p);
            personProfilePicUpload.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody responseBody) {
                            Logger.logD(TAG, "success" + responseBody);
                            try {
                                imagesDAO.updateUnsyncedPatientProfile(p.getPerson(), "PP");
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Uploaded Patient Profile", 4, IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
                        }
                    });
        }
        sessionManager.setPullSyncFinished(true);
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }

    public boolean obsImagesPush() {

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setObsImageUrl();
        List<ObsPushDTO> obsImageJsons = new ArrayList<>();
        try {
            obsImageJsons = imagesDAO.getObsUnsyncedImages();
            Log.e(TAG, "image request model" + gson.toJson(obsImageJsons));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        int i = 0;
        for (ObsPushDTO p : obsImageJsons) {
            //pass it like this
            File file = null;
            file = new File(AppConstants.IMAGE_PATH + p.getUuid() + ".jpg");
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Observable<ObsJsonResponse> obsJsonResponseObservable = AppConstants.apiInterface.OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic " + encoded, body, p);
            obsJsonResponseObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ObsJsonResponse>() {
                        @Override
                        public void onNext(ObsJsonResponse obsJsonResponse) {
                            Logger.logD(TAG, "success" + obsJsonResponse);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "success");
                            try {
                                imagesDAO.updateUnsyncedObsImages(p.getUuid());
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                    });
        }
        sessionManager.setPushSyncFinished(true);
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }


    public boolean azureImagePush() throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
//        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
//        localdb.execSQL("delete from tbl_azure_uploads");
        String encoded = sessionManager.getEncoded();
        Retrofit retrofit = AzureNetworkClient.getRetrofit();
        ImagesDAO imagesDAO = new ImagesDAO();
        List<azureResults> imageQueue = new ArrayList<>();
        try {
            imageQueue = imagesDAO.getAzureImageQueue();
            Log.e(TAG, imageQueue.toString());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        int i = 0;
        for (azureResults p : imageQueue) {
            //pass it like this
            File file = null;
            file = new File(AppConstants.IMAGE_PATH + p.getImagePath());
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part parts = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            RequestBody creatorId = RequestBody.create(MediaType.parse("text/plain"), p.getChwName());
            RequestBody visitId= RequestBody.create(MediaType.parse("text/plain"), p.getVisitId());
            RequestBody patientId= RequestBody.create(MediaType.parse("text/plain"), p.getPatientId());
            RequestBody type;
            if (p.getLeftRight()!=null){
                type = RequestBody.create(MediaType.parse("text/plain"), p.getLeftRight());
            }
            else{
                imagesDAO.removeAzureSynced(p.getImagePath());
                continue;
            }
            RequestBody sex = RequestBody.create(MediaType.parse("text/plain"), p.getSex());
            RequestBody age= RequestBody.create(MediaType.parse("text/plain"), p.getAge());
            RequestBody visual_acuity = RequestBody.create(MediaType.parse("text/plain"), p.getVARight());
            RequestBody pinhole_acuity = RequestBody.create(MediaType.parse("text/plain"),p.getPinholeRight());
            RequestBody complaints =RequestBody.create(MediaType.parse("text/plain"), p.getComplaints());
            if(p.getLeftRight()=="left"){
                visual_acuity = RequestBody.create(MediaType.parse("text/plain"), p.getVALeft());
                pinhole_acuity = RequestBody.create(MediaType.parse("text/plain"), p.getPinholeLeft());
            }
            Retrofit retrofit1 = AzureNetworkClient.getRetrofit();
            AzureUploadAPI uploadApis = retrofit1.create(AzureUploadAPI.class);
            Observable<ResponseBody> azureObservable = uploadApis.uploadImageAsync(parts, creatorId, visitId, patientId, type, visual_acuity, pinhole_acuity, sex, age, complaints);
            Log.d("AzureUpload", p.getImagePath());
            //is this the right type for the observable...
            azureObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {


                        @Override
                        public void onNext(@NonNull ResponseBody responseBody) {
                            Log.d(TAG, "azure success");
                            //Remove request from database and delete file
                            try {
                                imagesDAO.removeAzureSynced(p.getImagePath());
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror Azure" + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "success");
                            try {
                                List<azureResults> azureQueue;
                                azureQueue=imagesDAO.getAzureImageQueue();
                                Log.d("AzureQueue", azureQueue.toString());
                                Log.d(TAG, "complete");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
        sessionManager.setPushSyncFinished(true);
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());

        return true;

    }

    public boolean azureAddDocsPush() throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
//        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
//        localdb.execSQL("delete from tbl_azure_uploads");
        String encoded = sessionManager.getEncoded();
        Retrofit retrofit = AzureNetworkClient.getRetrofit();
        ImagesDAO imagesDAO = new ImagesDAO();
        List<azureResults> imageQueue = new ArrayList<>();
        List<uploadImage> imageGallery = new ArrayList<>();
        try {
            imageQueue = imagesDAO.getAzureDocsQueue();
            imageGallery=imagesDAO.getAzureGalleryQueue();
            Log.e(TAG, imageQueue.toString());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        //Make a list of patients who have images. These will be the uploaded ones
        List<azureResults> imagesToUpload = new ArrayList<>();

        for (uploadImage p : imageGallery){
            File file = new File(p.getImagePath());
            azureResults patient= new azureResults();
            if (file.exists()){
                patient.setLeftRight(p.getImageType());
                patient.setChwName(p.getPrototype());
                patient.setVisitId(p.getVisitId());
                patient.setImagePath(p.getImagePath());
                Log.d("galleryID",p.getVisitId());
                for (azureResults q : imageQueue){
                    Log.d("QUeueID", q.getVisitId());
                    if (p.getVisitId().equals(q.getVisitId())){
                        Log.d(TAG,"match");
                        if (q.getDiagnosisRight()!=null) patient.setDiagnosisRight(q.getDiagnosisRight());
                        if (q.getComplaintsRight()!=null) patient.setComplaintsRight(q.getComplaintsRight());
                        patient.setPatientId(q.getPatientId());
                        patient.setAge(q.getAge());
                        patient.setSex(q.getSex());
                        patient.setPinholeRight(q.getPinholeRight());
                        patient.setVARight(q.getVARight());
                        imagesToUpload.add(patient);
                    }
                }

            }
        }

        Log.d(TAG, "imagestoupload: " + imagesToUpload.toString());

        for (azureResults p : imagesToUpload) {
                //pass it like this
                File file = new File(p.getImagePath());
                RequestBody requestFileRight = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part imageRight = MultipartBody.Part.createFormData("file", file.getName(), requestFileRight);
                RequestBody creatorId = RequestBody.create(MediaType.parse("text/plain"), p.getChwName());
                RequestBody visitId = RequestBody.create(MediaType.parse("text/plain"), p.getVisitId());
                RequestBody patientId = RequestBody.create(MediaType.parse("text/plain"), p.getPatientId());
                RequestBody type = RequestBody.create(MediaType.parse("text/plain"), p.getLeftRight());
                RequestBody sex = RequestBody.create(MediaType.parse("text/plain"), p.getSex());
                RequestBody age = RequestBody.create(MediaType.parse("text/plain"), p.getAge());
                RequestBody visual_acuity_right = RequestBody.create(MediaType.parse("text/plain"), p.getVARight());
                RequestBody pinhole_acuity_right = RequestBody.create(MediaType.parse("text/plain"), p.getPinholeRight());
                RequestBody complaintsRight=RequestBody.create(MediaType.parse("text/plain"), "");
                if (p.getComplaintsRight()!=null) complaintsRight = RequestBody.create(MediaType.parse("text/plain"), p.getComplaintsRight().toString());
                RequestBody diagnosisRight=RequestBody.create(MediaType.parse("text/plain"), "");
                if (p.getDiagnosisRight()!=null) diagnosisRight = RequestBody.create(MediaType.parse("text/plain"), p.getDiagnosisRight().toString());

                Retrofit retrofit1 = AzureNetworkClient.getRetrofit();
                AzureUploadAPI uploadApis = retrofit1.create(AzureUploadAPI.class);

                Observable<ResponseBody> azureObservable = uploadApis.uploadImageAsync(imageRight, creatorId, visitId, patientId, type, visual_acuity_right, pinhole_acuity_right, sex, age, diagnosisRight);

                //is this the right type for the observable...
                azureObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableObserver<ResponseBody>() {


                            @Override
                            public void onNext(@NonNull ResponseBody responseBody) {
                                Log.d(TAG, "azure success " + p.getVisitId());
                                //getImageIdFromServer(p.getVisitId(), p.getPatientId(), p.getLeftRight(), p);
                                //Log.d(TAG+"i", String.valueOf(finalI));
                                //Remove request from database and delete file
                                //if(finalI ==2)
                                try {
                                    imagesDAO.removeAzureAddDoc(p.getVisitId(), p.getImagePath(), p.getImageId());
                                    imagesDAO.removeAzureGallery(p.getVisitId(),p.getImagePath());
                                } catch (DAOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.logD(TAG, "Onerror Azure2" + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                            }

                            @Override
                            public void onComplete() {
                                Logger.logD(TAG, "success");
                                try {
                                    List<azureResults> azureQueue;
                                    azureQueue = imagesDAO.getAzureDocsQueue();
                                    Log.d("AzureQueue", azureQueue.toString());
                                    Log.d(TAG, "complete");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }

        sessionManager.setPushSyncFinished(true);
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;

    }

    public void getImageIdFromServer(String visitId, String patientId, String type, azureResults patient){
        Retrofit retrofit1 = AzureNetworkClient.getRetrofit();
        AzureUploadAPI uploadApis = retrofit1.create(AzureUploadAPI.class);
        String url="api/v1/image/"+patientId+"/"+visitId;
        Call<List<azureResults>> response = uploadApis.getAzureImage(url);
        response.enqueue(new Callback<List<azureResults>>() {
            @Override
            public void onResponse(Call<List<azureResults>> call, Response<List<azureResults>> response) {
                    Log.d("Response", response.message());
                    Log.d("Response", response.body().toString());
                    for (azureResults result : response.body()) {
                        Log.d("azureresult", result.toString());
                        Log.d(TAG, "VisitId: " + patient.getVisitId() + " ImageID: " + result.getImageId());
                        if (result.getLeftRight() == type)
                            azureAddDiagnosis(result.getId(), patient);
                    }

            }


            @Override
            public void onFailure(Call<List<azureResults>> call, Throwable t) {
                Log.d(TAG, String.valueOf(t));

            }


        });
    }


    public boolean azureAddDiagnosis(String id, azureResults patient){
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Retrofit retrofit = AzureNetworkClient.getRetrofit();
        ImagesDAO imagesDAO = new ImagesDAO();
        String diagnosis_id= UUID.randomUUID().toString();
        String diagnosis=patient.getDiagnosisRight().toString();
        String creator= patient.getChwName();
        Map<String, Object> jsonParams = new ArrayMap<>();
//put something inside the map, could be null
        jsonParams.put("diagnosis_id", diagnosis_id);
        jsonParams.put("diagnosis", diagnosis);
        jsonParams.put("creator_id", creator);
        jsonParams.put("image_id", id);
        String images= "[{\"id\" : \"" + id + "\", \"diagnosis_id\" : \"" + diagnosis_id + "\"}]";
        jsonParams.put("images", images);
        /*String postBody="{\n \"id\" : \""+ diagnosis_id + "\", \n " +
                        "\"diagnosis\" : \"" + diagnosis + "\", \n"+
                        "\"creator_id\" : \"" + creator + "\", \n"+
                        "\"images\" : \"" + images + "\", \n}";
         */
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());
        Retrofit retrofit1 = AzureNetworkClient.getRetrofit();
        AzureUploadAPI uploadApis = retrofit1.create(AzureUploadAPI.class);
        Call<ResponseBody> response = uploadApis.addDiagnosis(body);

        response.enqueue(new Callback<ResponseBody>()
        {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> rawResponse)
            {
                try
                {
                    //get your response....
                    Log.d(TAG, "RetroFit2.0 :RetroGetLogin: " + rawResponse.body().string());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable)
            {
                Log.d(TAG, String.valueOf(throwable));
            }
        });

        //is this the right type for the observable...

        sessionManager.setPushSyncFinished(true);
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;

    }



    public boolean deleteObsImage() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        List<String> voidedObsImageList = new ArrayList<>();
        try {
            voidedObsImageList = imagesDAO.getVoidedImageObs();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        for (String voidedObsImage : voidedObsImageList) {
            String url = urlModifiers.obsImageDeleteUrl(voidedObsImage);
            Observable<Void> deleteObsImage = AppConstants.apiInterface.DELETE_OBS_IMAGE(url, "Basic " + encoded);
            deleteObsImage.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Void>() {
                        @Override
                        public void onNext(Void aVoid) {
                            Logger.logD(TAG, "success" + aVoid);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "successfully Deleted the images from server");
                        }
                    });
        }
        return true;
    }

}
