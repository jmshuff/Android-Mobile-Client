package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import app.insightfuleye.client.activities.cameraActivity.CameraActivity;
import app.insightfuleye.client.activities.complaintNodeActivity.ComplaintNodeActivity;
import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.activities.prototypeIterationActivity.prototypeIterationActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ImagesPushDAO;
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


        FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNew = new Intent(uploadImageActivity.this, prototypeIterationActivity.class);
                startActivity(intentNew);
            }
        });

        Button upload = findViewById(R.id.manualsyncbutton);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            ArrayList<File> fileList = new ArrayList<File>();
            List<azureResults> additionalDocs= new ArrayList<>();
            try {
                additionalDocs=imagesDAO.getAzureDocsQueue();
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

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        recyclerViewAdapter = new uploadImageAdapter(context, rowListItem, AppConstants.IMAGE_PATH);
                        recyclerView.setAdapter(recyclerViewAdapter);
                    } else {
                        Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                    }
                    try {
                        imagesPushDAO.azureAddDocsPush();
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }


                }
            });


        }


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

/*
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

 */

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


}
