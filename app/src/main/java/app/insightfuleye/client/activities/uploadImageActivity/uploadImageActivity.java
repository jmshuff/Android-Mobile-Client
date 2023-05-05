package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ImagesPushDAO;
import app.insightfuleye.client.database.dao.SyncDAO;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.hospitalImagingModel;
import app.insightfuleye.client.syncModule.SyncUtils;
import app.insightfuleye.client.utilities.NetworkConnection;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;

public class uploadImageActivity extends AppCompatActivity {
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private List<hospitalImagingModel> rowListItem = new ArrayList<>();
    private uploadImageAdapter recyclerViewAdapter;
    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        Toolbar topToolBar = findViewById(R.id.toolbar);
        context=uploadImageActivity.this;

        //removes the bug of no translation seen even when provided....
        topToolBar.setTitle(getString(R.string.title_activity_additional_documents));
        setSupportActionBar(topToolBar);

        ImagesDAO imagesDAO = new ImagesDAO();
        ImagesPushDAO imagesPushDAO= new ImagesPushDAO();

        FloatingActionButton fab=findViewById(R.id.fab_upload);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNew = new Intent(uploadImageActivity.this, uploadImageInfoActivity.class);
                startActivity(intentNew);
            }
        });

        Button upload = findViewById(R.id.manualsyncbutton);

        rowListItem=getRowListItem();

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

        RecyclerView recyclerView = findViewById(R.id.document_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewAdapter = new uploadImageAdapter(this, rowListItem);
        recyclerView.setAdapter(recyclerViewAdapter);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NetworkConnection.isOnline(getApplication())) {
                    Toast.makeText(context, getResources().getString(R.string.upload_started), Toast.LENGTH_LONG).show();

//                    AppConstants.notificationUtils.showNotifications(getString(R.string.visit_data_upload), getString(R.string.uploading_visit_data_notif), 3, VisitSummaryActivity.this);
                    SyncDAO syncDAO = new SyncDAO();
//                    ProgressDialog pd = new ProgressDialog(VisitSummaryActivity.this);
//                    pd.setTitle(getString(R.string.syncing_visitDialog));
//                    pd.show();
//                    pd.setCancelable(false);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Added the 4 sec delay and then push data.For some reason doing immediately does not work
                            //Do something after 100ms
                            SyncUtils syncUtils = new SyncUtils();
                            boolean isSynced = syncUtils.syncForeground("uploadImageActivity");
                            if (isSynced) {
                                AppConstants.notificationUtils.DownloadDone(getString(R.string.visit_data_upload), getString(R.string.visit_uploaded_successfully), 3, uploadImageActivity.this);
                                //

                            } else {
                                AppConstants.notificationUtils.DownloadDone(getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, uploadImageActivity.this);

                            }
//                            pd.dismiss();
//                            Toast.makeText(VisitSummaryActivity.this, getString(R.string.upload_completed), Toast.LENGTH_SHORT).show();
                        }
                    }, 4000);
                } else {
                    AppConstants.notificationUtils.DownloadDone( getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, uploadImageActivity.this);
                }

                if (isNetworkConnected()) {
                    Toast.makeText(context, getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
                    try {
                        TimeUnit.MILLISECONDS.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(rowListItem.size()>0){
                        rowListItem.remove(0);
                    }
                    recyclerViewAdapter = new uploadImageAdapter(context, rowListItem);
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                }



            }
        });


    }



/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

 */
    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(uploadImageActivity.this, HomeActivity.class);
        startActivity(intent2);
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public List<hospitalImagingModel> getRowListItem(){
        List<hospitalImagingModel> rowItems = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        //Distinct keyword is used to remove all duplicate records.
        Cursor idCursor = db.rawQuery("SELECT a.uuid, a.visituuid, a.patient_uuid, b.patient_identifier, c.imageName FROM tbl_encounter a, tbl_patient b, tbl_azure_img_uploads c WHERE a.sync = ? AND  a.encounter_type_uuid = ? AND a.patient_uuid = b.uuid AND a.visituuid=c.visitId and c.type = ?", new String[]{"false", UuidDictionary.ENCOUNTER_HOSPITAL_IMAGING, "right"});
        hospitalImagingModel hospitalImaging = new hospitalImagingModel();
        Log.d("RAINBOW: ","RAINBOW: "+idCursor.getCount());
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                hospitalImaging = new hospitalImagingModel();
                hospitalImaging.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                hospitalImaging.setPatientUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_uuid")));
                hospitalImaging.setPatientIdentifier(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_identifier")));
                hospitalImaging.setImageName(idCursor.getString(idCursor.getColumnIndexOrThrow("imageName")));
                hospitalImaging.setEncounterHospitalImaging(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                rowItems.add(hospitalImaging);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        idCursor.close();

        return rowItems;
    }

}
