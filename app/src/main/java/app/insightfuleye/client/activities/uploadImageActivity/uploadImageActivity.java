package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import app.insightfuleye.client.activities.cameraActivity.CameraActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.models.DocumentObject;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;

public class uploadImageActivity extends AppCompatActivity {
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private List<azureResults> rowListItem;
    private uploadImageAdapter recyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        Toolbar topToolBar = findViewById(R.id.toolbar);

        //removes the bug of no translation seen even when provided....
        topToolBar.setTitle(getString(R.string.title_activity_additional_documents));
        setSupportActionBar(topToolBar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            ArrayList<File> fileList = new ArrayList<File>();
            List<azureResults> additionalDocs= new ArrayList<>();
            try {
                additionalDocs=getAzureImageQueue();
            } catch (DAOException e) {
                e.printStackTrace();
            }
            rowListItem = new ArrayList<>();
            for (azureResults doc : additionalDocs) {
                String filename = AppConstants.IMAGE_PATH + doc.getImagePath() + ".jpg";
                if (new File(filename).exists()) {
                    rowListItem.add(doc);
                }
            }

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

            RecyclerView recyclerView = findViewById(R.id.document_RecyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new uploadImageAdapter(this, rowListItem, AppConstants.IMAGE_PATH);
            recyclerView.setAdapter(recyclerViewAdapter);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                File photo = new File(mCurrentPhotoPath);
                if (photo.exists()) {
                    try{

                        long length = photo.length();
                        length = length/1024;
                        Log.e("------->>>>",length+"");
                    }catch(Exception e){
                        System.out.println("File not found : " + e.getMessage() + e);
                    }

                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
                    updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
                }
            }
        }
    }

 */

    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                Intent intent = new Intent(this, uploadImageInfoActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public List<azureResults> getAzureImageQueue() throws DAOException {
        //get unsynced images from local storage
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        List<azureResults> azureResultList = new ArrayList<>();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_azure_additional_docs", null);
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    azureResults ImageQueue= new azureResults();
                    ImageQueue.setChwName(idCursor.getString(idCursor.getColumnIndexOrThrow("creatorId")));
                    ImageQueue.setImagePath(idCursor.getString(idCursor.getColumnIndexOrThrow("imageName")));
                    ImageQueue.setLeftRight(idCursor.getString(idCursor.getColumnIndexOrThrow("type")));
                    ImageQueue.setVisitId(idCursor.getString(idCursor.getColumnIndexOrThrow("visitId")));
                    ImageQueue.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patientId")));
                    ImageQueue.setVARight(idCursor.getString(idCursor.getColumnIndexOrThrow("VARight")));
                    ImageQueue.setVALeft(idCursor.getString(idCursor.getColumnIndexOrThrow("VALeft")));
                    ImageQueue.setPinholeRight(idCursor.getString(idCursor.getColumnIndexOrThrow("PinholeRight")));
                    ImageQueue.setPinholeLeft(idCursor.getString(idCursor.getColumnIndexOrThrow("PinholeLeft")));
                    ImageQueue.setAge(idCursor.getString(idCursor.getColumnIndexOrThrow("age")));
                    ImageQueue.setSex(idCursor.getString(idCursor.getColumnIndexOrThrow("sex")));
                    ImageQueue.setComplaints(idCursor.getString(idCursor.getColumnIndexOrThrow("complaints")));
                    azureResultList.add(ImageQueue);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return azureResultList;
    }
}
