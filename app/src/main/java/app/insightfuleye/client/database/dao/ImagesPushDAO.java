package app.insightfuleye.client.database.dao;

import android.app.NotificationManager;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.networkApiCalls.Api;
import app.insightfuleye.client.networkApiCalls.ApiClient;
import app.insightfuleye.client.networkApiCalls.AzureNetworkClient;
import app.insightfuleye.client.networkApiCalls.AzureUploadAPI;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.NotificationID;
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
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @NonNull
    private RequestBody createPartFromString(String description){
        if(description!=null) return RequestBody.create(MediaType.parse("text/plain"), description);
        else return RequestBody.create(MediaType.parse("text/plain"), "");
    }

    @NonNull
    private MultipartBody.Part createFilePart(String partName, String imagePath){
        File file = new File(AppConstants.IMAGE_PATH + imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        return MultipartBody.Part.createFormData("file", file.getName(), requestFile);
    }


    public boolean azureImagePush() throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
//        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
//        localdb.execSQL("delete from tbl_azure_uploads");
        String encoded = sessionManager.getEncoded();
        Retrofit retrofit = AzureNetworkClient.getRetrofit();
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<azureResults> imageQueue = new ArrayList<>();
        try {
            imageQueue = imagesDAO.getAzureImageQueue();
            Log.e(TAG, imageQueue.toString());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        int queueSize=imageQueue.size();
        int finalQueueSize = queueSize;
        int fileCount=0;
        final int[] totalUploaded = {0};

        //TO DO Add "if queue>0
        Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imagesQueueSize)+ queueSize, Toast.LENGTH_SHORT).show();
        int noteId = NotificationID.getID();
        AppConstants.notificationUtils.imageUploading(queueSize + " Images Uploading", "Progress", noteId, IntelehealthApplication.getAppContext(), queueSize, 0);

        while(queueSize>0){
            List<MultipartBody.Part> eyeImageParts= new ArrayList<>();
            Map<String, RequestBody> partMap = new HashMap<>();
            int fileNum=10;
            if (queueSize<fileNum)
                fileNum=queueSize;
            for (int i=0; i<fileNum; i++) {
                //pass it like this
                azureResults p = imageQueue.get(fileCount);
                File file= new File(AppConstants.IMAGE_PATH + p.getImagePath());
                if(file.isFile()){
                    eyeImageParts.add(createFilePart("file", p.getImagePath()));
                    partMap.put("visitId[" + i + "]", createPartFromString(p.getVisitId()));
                    partMap.put("patientId[" + i + "]", createPartFromString(p.getPatientId()));
                    partMap.put("creatorId[" + i + "]", createPartFromString(p.getChwName()));
                    partMap.put("type[" + i + "]", createPartFromString(p.getLeftRight()));
                    partMap.put("age[" + i + "]", createPartFromString(p.getAge()));
                    partMap.put("sex[" + i + "]", createPartFromString(p.getSex()));
                    if(p.getLeftRight().equals("right")){
                        partMap.put("visual_acuity[" + i + "]", createPartFromString(p.getVARight()));
                        partMap.put("pinhole_acuity[" + i + "]", createPartFromString(p.getPinholeRight()));
                        partMap.put("complaints[" + i + "]", createPartFromString(p.getComplaintStrR()));
                    }
                    else{
                        partMap.put("visual_acuity[" + i + "]", createPartFromString(p.getVALeft()));
                        partMap.put("pinhole_acuity[" + i + "]", createPartFromString(p.getPinholeLeft()));
                        partMap.put("complaints[" + i + "]", createPartFromString(p.getComplaintStrL()));
                    }
                }
                fileCount= fileCount+1;
            }

            Retrofit retrofit1 = AzureNetworkClient.getRetrofit();
            AzureUploadAPI uploadApis = retrofit1.create(AzureUploadAPI.class);
            Observable<ResponseBody> azureObservable = uploadApis.uploadImageAsync(eyeImageParts, partMap);
            //is this the right type for the observable...
            int finalFileNum = fileNum;
            queueSize=queueSize-fileNum;
            azureObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {

                        @Override
                        public void onNext(@NonNull ResponseBody responseBody) {
                            Log.d(TAG, "azure success next");

                            //Toast.makeText(IntelehealthApplication.getAppContext(), finalFileNum + " out of ", Toast.LENGTH_SHORT).show();
                            //Remove request from database and delete file
/*                        try {
                            imagesDAO.removeAzureSynced(p.getImagePath());
                        } catch (DAOException e) {
                            e.printStackTrace();
                        }*/
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror Azure" + e.getMessage());
                            Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imageUploadFailed), Toast.LENGTH_SHORT).show();
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                            AppConstants.notificationUtils.imageUploading(finalQueueSize + " Images Uploading", "Upload Failed", noteId, IntelehealthApplication.getAppContext(), finalQueueSize, totalUploaded[0]);

                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "Azure success");
                            Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imageUploadSuccess), Toast.LENGTH_SHORT).show();
                            ArrayList<azureResults> azureQueue = new ArrayList<>();
                            try {
                                azureQueue=imagesDAO.getAzureImageQueue();
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i< finalFileNum; i++){
                                azureResults q=azureQueue.get(i);

                                try {
                                    imagesDAO.removeAzureSynced(q.getImagePath());
                                } catch (DAOException e) {
                                    e.printStackTrace();
                                }
                            }
                            totalUploaded[0] +=finalFileNum;
                            AppConstants.notificationUtils.imageUploading(finalQueueSize + " Images Uploading", "Progress", noteId, IntelehealthApplication.getAppContext(), finalQueueSize, totalUploaded[0]);

                        }
                    });

        }
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        sessionManager.setPushSyncFinished(true);
        return true;
    }

    public boolean imagePost() throws DAOException{
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        Api api = ApiClient.createService(Api.class);
        ImagesDAO imagesDAO = new ImagesDAO();
        List<azureResults> imageQueue = new ArrayList<>();
        try{
            imageQueue = imagesDAO.getAzureImageQueue();
            Log.d(TAG, imageQueue.toString());
        } catch(DAOException e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        int queueSize=imageQueue.size();
        int finalQueueSize = queueSize;
        final int[] totalUploaded = {0};
        final boolean[] uploaded = {true};

        //TO DO Add "if queue>0
        int noteId = NotificationID.getID();
        if(queueSize>0){
            Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imagesQueueSize)+ queueSize, Toast.LENGTH_SHORT).show();
            AppConstants.notificationUtils.imageUploading(queueSize + " Images Uploading", "Progress", noteId, IntelehealthApplication.getAppContext(), queueSize, 0);

            List<MultipartBody.Part> eyeImageParts= new ArrayList<>();
            Map<String, RequestBody> partMap = new HashMap<>();
            for (azureResults image : imageQueue){
                File file= new File(AppConstants.IMAGE_PATH + image.getImagePath());
                if(file.isFile()){
                    eyeImageParts.add(createFilePart("file", image.getImagePath()));
                    partMap.put("visit_id", createPartFromString(image.getVisitId()));
                    partMap.put("patient_id", createPartFromString(image.getPatientId()));
                    partMap.put("type", createPartFromString(image.getLeftRight()));
                    Observable<ResponseBody> azureObservable = api.uploadImageAsync(eyeImageParts, partMap);
                    azureObservable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<ResponseBody>() {

                                @Override
                                public void onNext(@NonNull ResponseBody responseBody) {
                                    Log.d(TAG, "azure success next");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "Onerror Azure" + e.getMessage());
                                    Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imageUploadFailed), Toast.LENGTH_SHORT).show();
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                                    AppConstants.notificationUtils.imageUploading(finalQueueSize + " Images Uploading", "Upload Failed", noteId, IntelehealthApplication.getAppContext(), finalQueueSize, totalUploaded[0]);
                                    uploaded[0] =false;

                                }

                                @Override
                                public void onComplete() {
                                    Logger.logD(TAG, "Azure success");
                                    Toast.makeText(IntelehealthApplication.getAppContext(), IntelehealthApplication.getAppContext().getString(R.string.imageUploadSuccess), Toast.LENGTH_SHORT).show();
                                    try {
                                        imagesDAO.removeAzureSynced(image.getImagePath());
                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                    }

                                    totalUploaded[0] +=1;
                                    AppConstants.notificationUtils.imageUploading(finalQueueSize + " Images Uploading", "Progress", noteId, IntelehealthApplication.getAppContext(), finalQueueSize, totalUploaded[0]);

                                }
                            });

                }
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
            }

        }
        return uploaded[0];
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


}
