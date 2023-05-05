package app.insightfuleye.client.activities.uploadImageActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.cameraActivity.CameraActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.database.dao.VisitsDAO;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.models.dto.PatientDTO;
import app.insightfuleye.client.models.dto.VisitDTO;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.UuidGenerator;
import app.insightfuleye.client.utilities.exception.DAOException;

public class uploadImageInfoActivity extends AppCompatActivity {
    private static final String TAG = uploadImageInfoActivity.class.getSimpleName();
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_RIGHT = 1001;
    private static final int IMAGE_CAPTURE_LEFT=1002;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    EditText mAge;
    RadioButton mGenderM;
    RadioButton mGenderF;
    private String mImager;
    Spinner spinVARight;
    Spinner spinVALeft;
    Spinner spinPinholeRight;
    Spinner spinPinholeLeft;
    ImageView mImageViewRight;
    ImageView mImageViewLeft;
    EditText vaRightText;
    EditText vaLeftText;
    EditText phRightText;
    EditText phLeftText;
    private String vaRight, vaLeft, phRight, phLeft;
    private String mGender;
    ImagesDAO imagesDAO = new ImagesDAO();
    private String mCurrentPhotoPath;
    private String mType;
    Context context;
    String visituuid_edit;
    String visituuid;
    String patientuuid;
    String visilantId;
    azureResults azureResults = new azureResults();

    //make a listview adapter. Never do this. This is such bad coding.
    // I was in a hurry. I apologize to future me
    CheckBox matCatR;
    CheckBox matCatL;
    CheckBox immatCatR;
    CheckBox immatCatL;
    CheckBox pterygiumR;
    CheckBox pterygiumL;
    CheckBox cornealUlcerR;
    CheckBox cornealUlcerL;
    CheckBox cornealOpacityR;
    CheckBox cornealOpacityL;
    CheckBox normalR;
    CheckBox normalL;
    CheckBox pciolR;
    CheckBox pciolL;
    CheckBox blurryCloseR;
    CheckBox blurryCloseL;
    CheckBox blurryFarR;
    CheckBox blurryFarL;
    CheckBox rednessR;
    CheckBox rednessL;
    CheckBox eyePainR;
    CheckBox eyePainL;
    CheckBox headacheR;
    CheckBox headacheL;
    CheckBox eyeTraumaR;
    CheckBox eyeTraumaL;
    CheckBox noComplaintR;
    CheckBox noComplaintL;
    TextView imageRightTV;
    TextView imageLeftTV;

    LinearLayout previewRight;
    LinearLayout previewLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mAge = findViewById(R.id.upload_image_age);
        mGenderM = findViewById(R.id.upload_image_gender_male);
        mGenderF = findViewById(R.id.upload_image_gender_female);

        spinVARight=findViewById(R.id.spinner_varight);
        spinVALeft=findViewById(R.id.spinner_valeft);
        spinPinholeRight=findViewById(R.id.spinner_pinholeright);
        spinPinholeLeft=findViewById(R.id.spinner_pinholeleft);
        mImageViewRight=findViewById(R.id.imageview_right_eye_picture);
        imageRightTV = findViewById(R.id.textview_right_eye_picture);
        imageLeftTV = findViewById(R.id.textview_left_eye_picture);
        mImageViewLeft=findViewById(R.id.imageview_left_eye_picture);
        vaLeftText=findViewById(R.id.valeft);
        vaRightText=findViewById(R.id.varight);
        phLeftText=findViewById(R.id.pinholeleft);
        phRightText=findViewById(R.id.pinholeright);
//the absolute worst idea. kill me
/*
        matCatR=findViewById(R.id.checkbox_mature_cat_r);
        matCatL=findViewById(R.id.checkbox_mature_cat_l);
        immatCatR=findViewById(R.id.checkbox_immature_cat_r);
        immatCatL=findViewById(R.id.checkbox_immature_cat_l);
        pterygiumR=findViewById(R.id.checkbox_pterygium_r);
        pterygiumL=findViewById(R.id.checkbox_pterygium_l);
        cornealOpacityR=findViewById(R.id.checkbox_corneal_opacity_r);
        cornealOpacityL=findViewById(R.id.checkbox_corneal_opacity_l);
        cornealUlcerR=findViewById(R.id.checkbox_corneal_ulcer_r);
        cornealUlcerL=findViewById(R.id.checkbox_corneal_ulcer_l);
        normalR=findViewById(R.id.checkbox_normal_r);
        normalL=findViewById(R.id.checkbox_normal_l);
        pciolR=findViewById(R.id.checkbox_pciol_r);
        pciolL=findViewById(R.id.checkbox_pciol_l);
*/

        blurryCloseR=findViewById(R.id.checkbox_blurry_close_r);
        blurryCloseL=findViewById(R.id.checkbox_blurry_close_l);
        blurryFarR=findViewById(R.id.checkbox_blurry_far_r);
        blurryFarL=findViewById(R.id.checkbox_blurry_far_l);
        rednessR=findViewById(R.id.checkbox_redness_r);
        rednessL=findViewById(R.id.checkbox_redness_l);
        eyePainR=findViewById(R.id.checkbox_eye_pain_r);
        eyePainL=findViewById(R.id.checkbox_eye_pain_l);
        headacheR=findViewById(R.id.checkbox_headache_r);
        headacheL=findViewById(R.id.checkbox_headache_l);
        eyeTraumaR=findViewById(R.id.checkbox_eye_trauma_r);
        eyeTraumaL=findViewById(R.id.checkbox_eye_trauma_l);
        noComplaintR=findViewById(R.id.checkbox_no_complaints_r);
        noComplaintL=findViewById(R.id.checkbox_no_complaints_l);

        //load past details to edit
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("visitId")) {
                visituuid_edit = intent.getStringExtra("visitId");
                visituuid=visituuid_edit;
                patientuuid = intent.getStringExtra("patientId");
                visilantId=intent.getStringExtra("patientIdentifier");
                setscreen();
            }
        }

        if (visituuid_edit ==null){
            PatientsDAO patientsDAO= new PatientsDAO();
            visilantId=patientsDAO.generateVisilantId();
            UuidGenerator uuidGenerator = new UuidGenerator();
            patientuuid= uuidGenerator.UuidGenerator();
            visituuid = uuidGenerator.UuidGenerator();
        }

        setTitle("Visilant Id: " + visilantId);
        //Age need to convert from Birthday
        Resources res = getResources();
        ArrayAdapter<CharSequence> vaRightAdapter = ArrayAdapter.createFromResource(this,
                R.array.visual_acuity_scores, R.layout.custom_spinner);
        ArrayAdapter<CharSequence> vaLeftAdapter = ArrayAdapter.createFromResource(this,
                R.array.visual_acuity_scores, R.layout.custom_spinner);
        ArrayAdapter<CharSequence> phRightAdapter = ArrayAdapter.createFromResource(this,
                R.array.visual_acuity_scores, R.layout.custom_spinner);
        ArrayAdapter<CharSequence> phLeftAdapter = ArrayAdapter.createFromResource(this,
                R.array.visual_acuity_scores, R.layout.custom_spinner);

        spinVARight.setAdapter(vaRightAdapter);
        spinVALeft.setAdapter(vaLeftAdapter);
        spinPinholeRight.setAdapter(phRightAdapter);
        spinPinholeLeft.setAdapter(phLeftAdapter);

        if(visituuid_edit != null){
            //set spinners for edit
            spinVARight.setSelection(vaRightAdapter.getPosition(String.valueOf(azureResults.getVARight())));
            spinVALeft.setSelection(vaLeftAdapter.getPosition(String.valueOf(azureResults.getVALeft())));
            spinPinholeRight.setSelection(phRightAdapter.getPosition(String.valueOf(azureResults.getPinholeRight())));
            spinPinholeLeft.setSelection(phLeftAdapter.getPosition(String.valueOf(azureResults.getPinholeLeft())));
            //set images for edit
            File imageFileRight=new File(AppConstants.IMAGE_PATH + azureResults.getImagePath());
            File imageFileLeft= new File(AppConstants.IMAGE_PATH + azureResults.getImageId());
            if (imageFileRight.exists()){
                Glide.with(this)
                        .load(new File(AppConstants.IMAGE_PATH + azureResults.getImagePath()))
                        .thumbnail(0.25f)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(mImageViewRight);
            }
            if(imageFileLeft.exists()){
                Glide.with(this)
                        .load(new File(AppConstants.IMAGE_PATH + azureResults.getImageId()))
                        .thumbnail(0.25f)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(mImageViewLeft);
            }
            mAge.setText(azureResults.getAge());
            //if (patient.getDiagnosisRight()!=null) setCheckedRight(patient.getDiagnosisRight());
            if (azureResults.getComplaintsRight()!=null) setCheckedRight(azureResults.getComplaintsRight());
            //if (patient.getDiagnosisLeft()!=null) setCheckedLeft(patient.getDiagnosisLeft());
            if (azureResults.getComplaintsLeft()!= null) setCheckedLeft(azureResults.getComplaintsLeft());
        }

        if (visituuid_edit != null) {
            if (azureResults.getSex().equals("male")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
            } else {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
            }


        }
        if (mGenderM.isChecked()) {
            mGender = "male";

        } else {
            mGender = "female";
        }


/*     previewRight.setVisibility(View.GONE);

     previewLeft.setVisibility(View.GONE);*/

        spinVARight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vaRight = parent.getItemAtPosition(position).toString();
                if(position!=0)
                    vaRightText.setError(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinVALeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vaLeft = parent.getItemAtPosition(position).toString();
                if(position!=0)
                    vaLeftText.setError(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinPinholeRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                phRight=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinPinholeLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                phLeft=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mGenderF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });


        mImageViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (azureResults.getImagePath()!=null){
                    previewImage(uploadImageInfoActivity.this, azureResults.getImagePath(), "right");

                }else {
                    File filePath = new File(AppConstants.IMAGE_PATH + azureResults.getImagePath());
/*                    if (!filePath.exists()) {
                        filePath.mkdir();
                    }*/
                    azureResults.setImagePath(UUID.randomUUID().toString() + ".jpg");
                    manageCameraPermissions(azureResults.getImagePath(),IMAGE_CAPTURE_RIGHT);
                    //Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                    // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
//                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageNameRight);
//                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
//                    cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, "right");
                    //startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                }
            }
        });

        mImageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (azureResults.getImageId()!=null){
                    previewImage(uploadImageInfoActivity.this, azureResults.getImageId(), "left");
                }

                else {
                    azureResults.setImageId(UUID.randomUUID().toString() + ".jpg");
                    File filePath = new File(AppConstants.IMAGE_PATH + azureResults.getImageId());
/*                    if (!filePath.exists()) {
                        filePath.mkdir();
                    }*/
                    manageCameraPermissions(azureResults.getImageId(), IMAGE_CAPTURE_LEFT);
/*                    Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                    // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageNameLeft);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                    cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, "left");
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);*/
                }
            }
        });



        mAge.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if (!mAge.getText().toString().isEmpty()) {
                        Calendar calendar = Calendar.getInstance();
                        int curYear = calendar.get(Calendar.YEAR);
                        int mAgeYear = Integer.parseInt(mAge.getText().toString());
                        int birthYear = curYear - mAgeYear; //mAge will just be the year JS
                        int curMonth = calendar.get(Calendar.MONTH);
                        int birthMonth = curMonth; //There is no birth month JS
                        int birthDay = calendar.get(Calendar.DAY_OF_MONTH);
                        //int totalDays = today.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_upload);
        fab.setOnClickListener(v -> {
            File fileRight= new File(AppConstants.IMAGE_PATH + azureResults.getImagePath());
            File fileLeft = new File(AppConstants.IMAGE_PATH + azureResults.getImageId());
            boolean hasError=false;

            if (!mGenderF.isChecked() && !mGenderM.isChecked()){
                mGenderF.setError(getString(R.string.error_field_required));
                hasError=true;
            }

            if(mAge.getText().toString().isEmpty() || mAge.getText().toString()==""){
                mAge.setError(getString(R.string.error_field_required));
                mAge.requestFocus();
                hasError=true;
            }
            else if(!isParsable(mAge.getText().toString()) || Integer.parseInt(mAge.getText().toString())>120){
                mAge.setError("Please enter a valid age");
                mAge.requestFocus();
                hasError=true;
            }
            if (spinVARight.getSelectedItemPosition()==0){
                vaRightText.setError(getString(R.string.error_field_required));
                spinVARight.requestFocus();
                hasError=true;
            }
            if (spinVALeft.getSelectedItemPosition()==0){
                vaLeftText.setError(getString(R.string.error_field_required));
                spinVALeft.requestFocus();
                hasError=true;
            }
            if(!fileRight.exists()){
                imageRightTV.setError(getString(R.string.error_field_required));
                hasError=true;
            }
            if (!fileLeft.exists()){
                imageLeftTV.setError(getString(R.string.error_field_required));
                hasError=true;
            }
/*            else if (spinPinholeRight.getSelectedItemPosition()==0){
                phRightText.setError(getString(R.string.error_field_required));
                spinPinholeRight.requestFocus();
                hasError=true;
            }
            else if (spinPinholeLeft.getSelectedItemPosition()==0){
                phLeftText.setError(getString(R.string.error_field_required));
                spinPinholeLeft.requestFocus();
                hasError=true;
            }*/

            if(!blurryCloseR.isChecked() && !blurryFarR.isChecked() && !rednessR.isChecked() && !headacheR.isChecked() && !eyePainR.isChecked() && !eyeTraumaR.isChecked() && !noComplaintR.isChecked()){
                blurryCloseR.setError(getString(R.string.error_field_required));
                blurryCloseR.requestFocus();
                hasError=true;
            }

            if(!blurryCloseL.isChecked() && !blurryFarL.isChecked() && !rednessL.isChecked() && !headacheL.isChecked() && !eyePainL.isChecked() && !eyeTraumaL.isChecked() && !noComplaintL.isChecked()){
                blurryCloseL.setError(getString(R.string.error_field_required));
                blurryCloseL.requestFocus();
                hasError=true;
            }
            if(!hasError){
                azureResults.setAge(mAge.getText().toString());
                azureResults.setVARight(vaRight);
                azureResults.setVALeft(vaLeft);
                azureResults.setPinholeRight(phRight);
                azureResults.setPinholeLeft(phLeft);
                azureResults.setSex(mGender);
                azureResults.setChwName(mImager);
                if (visituuid_edit != null) {
                    //updateAzureImageDatabase();
                    dbUpdate();
                } else {
                    try {
                        dbInsertion();
                        insertAzureImageDatabase();
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent1= new Intent(uploadImageInfoActivity.this, uploadImageActivity.class);
                startActivity(intent1);
            }

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result Received");
        Log.v(TAG, "Request Code " + String.valueOf(requestCode));
        Log.v(TAG, "Result Code " + String.valueOf(resultCode));

        if (requestCode == IMAGE_CAPTURE_RIGHT || requestCode==IMAGE_CAPTURE_LEFT) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                if (requestCode==IMAGE_CAPTURE_RIGHT){
                    mType="right";
                    mCurrentPhotoPath=azureResults.getImagePath();
                    imageRightTV.setError(null);
                }
                else {
                    mType="left";
                    mCurrentPhotoPath=azureResults.getImageId();
                    imageLeftTV.setError(null);
                }
                if(mType.contains("right")) {
                    Glide.with(this)
                            .load(new File(AppConstants.IMAGE_PATH + mCurrentPhotoPath))
                            .thumbnail(0.25f)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(mImageViewRight);
                }
                else{
                    Glide.with(this)
                            .load(new File(AppConstants.IMAGE_PATH + mCurrentPhotoPath))
                            .thumbnail(0.25f)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(mImageViewLeft);
                }
            }
        }
    }


    private void setscreen(){
        azureResults.setPatientId(patientuuid);
        azureResults.setVisitId(visituuid);
        azureResults.setChwName(sessionManager.getChwname());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String patientSelection = "uuid=?";
        String[] Args = {patientuuid};
        String[] Columns = {"date_of_birth", "gender"};
        Cursor idCursor = db.query("tbl_patient", Columns, patientSelection, Args, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                String age = idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth"));
                try{
                    int year = Integer.parseInt(age.substring(age.length()-4));
                    azureResults.setAge(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)-year));
                    azureResults.setSex(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                }catch(Exception e){
                    azureResults.setAge("");
                }
            } while (idCursor.moveToNext());
            idCursor.close();
        }

        String[] Args1 = {patientuuid, "right"};
        String selection = "patientId = ? AND type = ?";
        String[] Columns1 = {"imageName", "type"};
        Cursor idCursor1 = db.query("tbl_azure_img_uploads", Columns1, selection, Args1, null, null, null);
        if (idCursor1.moveToFirst()) {
            do {
                azureResults.setImagePath(idCursor1.getString(idCursor1.getColumnIndexOrThrow("imageName"))); //imagePath for imageNameRight
            } while (idCursor1.moveToNext());
            idCursor1.close();
        }

        String[] Args2 = {patientuuid, "left"};
        Cursor idCursor2 = db.query("tbl_azure_img_uploads", Columns1, selection, Args2, null, null, null);
        if (idCursor2.moveToFirst()) {
            do {
                azureResults.setImageId(idCursor2.getString(idCursor2.getColumnIndexOrThrow("imageName"))); //imagePath for imageNameRight
            } while (idCursor2.moveToNext());
            idCursor2.close();
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        selection = "encounteruuid = ? AND conceptuuid = ?";
        String[] Columns3 = {"value"};
        String encounterUuid = encounterDAO.getEncounterUuidByVisit(visituuid_edit);
        String[] Args3 = {encounterUuid, UuidDictionary.VARight};
        Cursor idCursor3 = db.query("tbl_obs", Columns3, selection, Args3, null, null, null);
        if (idCursor3.moveToFirst()) {
            do {
                azureResults.setVARight(idCursor3.getString(idCursor3.getColumnIndexOrThrow("value"))); //imagePath for imageNameRight
            } while (idCursor3.moveToNext());
            idCursor3.close();
        }

        String[] Args4 = {encounterUuid, UuidDictionary.VALeft};
        Cursor idCursor4 = db.query("tbl_obs", Columns3, selection, Args4, null, null, null);
        if (idCursor4.moveToFirst()) {
            do {
                azureResults.setVALeft(idCursor4.getString(idCursor4.getColumnIndexOrThrow("value"))); //imagePath for imageNameRight
            } while (idCursor4.moveToNext());
            idCursor4.close();
        }

        String[] Args5 = {encounterUuid, UuidDictionary.PinholeRight};
        Cursor idCursor5 = db.query("tbl_obs", Columns3, selection, Args5, null, null, null);
        if (idCursor5.moveToFirst()) {
            do {
                azureResults.setPinholeRight(idCursor5.getString(idCursor5.getColumnIndexOrThrow("value"))); //imagePath for imageNameRight
            } while (idCursor5.moveToNext());
            idCursor5.close();
        }

        String[] Args6 = {encounterUuid, UuidDictionary.PinholeLeft};
        Cursor idCursor6 = db.query("tbl_obs", Columns3, selection, Args6, null, null, null);
        if (idCursor6.moveToFirst()) {
            do {
                azureResults.setPinholeLeft(idCursor6.getString(idCursor6.getColumnIndexOrThrow("value"))); //imagePath for imageNameRight
            } while (idCursor6.moveToNext());
            idCursor6.close();
        }

        String[] Args7 = {encounterUuid, UuidDictionary.volunteerComplaintRight};
        Cursor idCursor7 = db.query("tbl_obs", Columns3, selection, Args7, null, null, null);
        if (idCursor7.moveToFirst()) {
            do {
                if (idCursor7.getString(idCursor7.getColumnIndexOrThrow("value"))!=null)
                    azureResults.setComplaintsRight(new ArrayList<>(Arrays.asList((idCursor7.getString(idCursor7.getColumnIndexOrThrow("value"))).split(","))));
            } while (idCursor7.moveToNext());
            idCursor7.close();
        }

        String[] Args8 = {encounterUuid, UuidDictionary.volunteerComplaintLeft};
        Cursor idCursor8 = db.query("tbl_obs", Columns3, selection, Args8, null, null, null);
        if (idCursor8.moveToFirst()) {
            do {
                if (idCursor8.getString(idCursor8.getColumnIndexOrThrow("value"))!=null)
                    azureResults.setComplaintsLeft(new ArrayList<>(Arrays.asList((idCursor8.getString(idCursor8.getColumnIndexOrThrow("value"))).split(","))));
            } while (idCursor8.moveToNext());
            idCursor8.close();
        }

    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.upload_image_gender_male:
                if (checked)
                    mGender = "male";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.upload_image_gender_female:
                if (checked)
                    mGender = "female";
                Log.v(TAG, "gender:" + mGender);
                break;
        }
        azureResults.setSex(mGender);
        mGenderM.setError(null);
        mGenderF.setError(null);
    }
/*    public void onRadioButtonClicked2(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.upload_image_mohit:
                if(checked)
                    mImager="Mohit";
                break;
            case R.id.upload_image_person_other:
                if(checked)
                    mImager="Aravind Staff";
                break;
        }
        patient.setChwName(mImager);
    }*/


    public boolean updateAzureImageDatabase() throws DAOException {
        boolean isUpdated =false;
        SQLiteDatabase localdb=AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues=new ContentValues();
        try{
            contentValues.put("imageName", azureResults.getImagePath()); //imageNameRight
            contentValues.put("type", "right");
            localdb.updateWithOnConflict("tbl_azure_img_uploads", contentValues, "visitId = ?", new String[]{visituuid_edit}, SQLiteDatabase.CONFLICT_REPLACE);

            contentValues.put("imageName", azureResults.getImageId());
            contentValues.put("type", "left");
            localdb.updateWithOnConflict("tbl_azure_img_uploads", contentValues, "visitId = ?", new String[]{visituuid_edit}, SQLiteDatabase.CONFLICT_REPLACE);
            localdb.setTransactionSuccessful();
            isUpdated=true;
        }catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally{
            localdb.endTransaction();
        }
        return isUpdated;
    }

    public boolean insertAzureImageDatabase() throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("imageName", azureResults.getImagePath()); //imageNameRight
            contentValues.put("patientId", patientuuid);
            contentValues.put("visitId", visituuid);
            contentValues.put("type", "right");
            //contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_azure_img_uploads", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            contentValues.put("imageName", azureResults.getImageId()); //imageNameLeft
            contentValues.put("type", "left");
            localdb.insertWithOnConflict("tbl_azure_img_uploads", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
        }
        return isInserted;
    }
    public void onDiagnosisRightClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings= new ArrayList<>();
        if(azureResults.getDiagnosisRight()!=null){
            selectedStrings = azureResults.getDiagnosisRight();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        azureResults.setDiagnosisRight(selectedStrings);
    }

    public void onDiagnosisLeftClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(azureResults.getDiagnosisLeft()!=null){
            selectedStrings= azureResults.getDiagnosisLeft();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        azureResults.setDiagnosisLeft(selectedStrings);

    }

    public void onComplaintRightClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(azureResults.getComplaintsRight()!=null){
            selectedStrings= azureResults.getComplaintsRight();
        }
        if(((CheckBox) v).getText().toString()==getString(R.string.no_complaints)){
            if(checked){
                blurryFarR.setEnabled(false);
                blurryCloseR.setEnabled(false);
                rednessR.setEnabled(false);
                eyePainR.setEnabled(false);
                headacheR.setEnabled(false);
                eyeTraumaR.setEnabled(false);
                blurryFarR.setChecked(false);
                blurryCloseR.setChecked(false);
                eyeTraumaR.setChecked(false);
                rednessR.setChecked(false);
                headacheR.setChecked(false);
                eyePainR.setChecked(false);
            }else{
                blurryFarR.setEnabled(true);
                blurryCloseR.setEnabled(true);
                rednessR.setEnabled(true);
                eyePainR.setEnabled(true);
                headacheR.setEnabled(true);
                eyeTraumaR.setEnabled(true);
            }
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
            blurryCloseR.setError(null);
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        azureResults.setComplaintsRight(selectedStrings);
    }

    public void onComplaintLeftClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(azureResults.getComplaintsLeft()!=null){
            selectedStrings= azureResults.getComplaintsLeft();
        }
        if(((CheckBox) v).getText().toString()==getString(R.string.no_complaints)){
            if(checked){
                blurryCloseL.setEnabled(false);
                blurryFarL.setEnabled(false);
                rednessL.setEnabled(false);
                eyePainL.setEnabled(false);
                headacheL.setEnabled(false);
                eyeTraumaL.setEnabled(false);
                blurryFarL.setChecked(false);
                blurryCloseL.setChecked(false);
                eyeTraumaL.setChecked(false);
                rednessL.setChecked(false);
                headacheL.setChecked(false);
                eyePainL.setChecked(false);
            }else{
                blurryCloseL.setEnabled(true);
                blurryFarL.setEnabled(true);
                rednessL.setEnabled(true);
                eyePainL.setEnabled(true);
                headacheL.setEnabled(true);
                eyeTraumaL.setEnabled(true);
            }
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
            blurryCloseL.setError(null);
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        azureResults.setComplaintsLeft(selectedStrings);
    }


    public void setCheckedRight(ArrayList<String> checked){
        for (String item: checked){
/*            if (item.contains("Mature Cataract")) matCatR.setChecked(true);
            else if(item.contains("Immature Cataract"))immatCatR.setChecked(true);
            else if(item.contains("Pterygium")) pterygiumR.setChecked(true);
            else if(item.contains("Corneal Opacity")) cornealOpacityR.setChecked(true);
            else if (item.contains("Corneal Ulcer")) cornealUlcerR.setChecked(true);
            else if(item.contains("Normal")) normalR.setChecked(true);
            else if(item.contains("PC IOL")) pciolR.setChecked(true);*/

            if(item.contains("Blurry Vision Close")) blurryCloseR.setChecked(true);
            else if (item.contains("Blurry Vision Far")) blurryFarR.setChecked(true);
            else if (item.contains("Redness")) rednessR.setChecked(true);
            else if (item.contains("Eye Pain")) eyePainR.setChecked(true);
            else if (item.contains("Headache")) headacheR.setChecked(true);
            else if (item.contains("Eye Trauma")) eyeTraumaR.setChecked(true);
        }

    }
    public void setCheckedLeft(ArrayList<String> checked){
        for (String item: checked){
/*            if (item.contains("Mature Cataract")) matCatL.setChecked(true);
            else if(item.contains("Immature Cataract"))immatCatL.setChecked(true);
            else if(item.contains("Pterygium")) pterygiumL.setChecked(true);
            else if(item.contains("Corneal Opacity")) cornealOpacityL.setChecked(true);
            else if (item.contains("Corneal Ulcer")) cornealUlcerL.setChecked(true);
            else if(item.contains("Normal")) normalL.setChecked(true);
            else if (item.contains("PC IOL")) pciolL.setChecked(true);*/

            if(item.contains("Blurry Vision Close")) blurryCloseL.setChecked(true);
            else if (item.contains("Blurry Vision Far")) blurryFarL.setChecked(true);
            else if (item.contains("Redness")) rednessL.setChecked(true);
            else if (item.contains("Eye Pain")) eyePainL.setChecked(true);
            else if (item.contains("Headache")) headacheL.setChecked(true);
            else if (item.contains("Eye Trauma")) eyeTraumaL.setChecked(true);
        }

    }

    public AlertDialog previewImage(final Activity context, final String path, final String type) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setPositiveButton(R.string.visit_summary_button_download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadImage(path);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.retake_image, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File temp = new File(AppConstants.IMAGE_PATH + path );
                Log.d("tempPathExists", String.valueOf(temp.exists()));
                if (temp.exists()) temp.delete();
                Log.d("tempPathExistsAfter", String.valueOf(temp.exists()));
                int requestCodeRetake;
                if (type.contains("right")) requestCodeRetake=IMAGE_CAPTURE_RIGHT;
                else requestCodeRetake=IMAGE_CAPTURE_LEFT;
                /*Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, path);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH+path);
                cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, type);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);*/
                manageCameraPermissions(path, requestCodeRetake);
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT==Build.VERSION_CODES.M || Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        } else {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        dialog.setView(dialogLayout);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screen_height = displayMetrics.heightPixels;
                int screen_width = displayMetrics.widthPixels;

                ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                Glide.with(context)
                        .load(new File(AppConstants.IMAGE_PATH+path))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .listener(new RequestListener<File, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, File file, Target<GlideDrawable> target, boolean b) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable glideDrawable, File file, Target<GlideDrawable> target, boolean b, boolean b1) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .override(screen_width, screen_height)
                        .into(imageView);
            }
        });

        dialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
        return dialog;

    }

    private void downloadImage(String path){
        ActivityCompat.requestPermissions(uploadImageInfoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(uploadImageInfoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        Toast.makeText(this, "Downloading...", Toast.LENGTH_LONG).show();
        String DIR_NAME="AROMA Photos";
        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + DIR_NAME + "/");

        Log.d("Download Image: ", AppConstants.IMAGE_PATH+path);

        Bitmap bitmap = BitmapFactory.decodeFile(AppConstants.IMAGE_PATH+path);

        FileOutputStream outputStream = null;



        direct.mkdirs();
        File file = Environment.getExternalStorageDirectory();
        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(direct,filename);

        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(outFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    public void manageCameraPermissions(String imageName, int requestCode){

        //if system os >=marshmellow, request runtime permissions
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                //permisssions not enabled, check permissions
                String[] permission= {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permission, PERMISSION_CODE);
            }
            else{
                openCameraNative(imageName, requestCode);
            }
        }
        else{
            //system os < marshmellow
            openCameraNative(imageName, requestCode);
        }

    }

    public void openCameraNative(String imageName, int requestCode) {
        File file = new File(AppConstants.IMAGE_PATH + imageName);
        Log.d("openCamera", String.valueOf(file));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }

    }

    public void dbInsertion(){
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        UuidGenerator uuidGenerator = new UuidGenerator();

        //INSERT PATIENT
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setGender(mGender);

        if (!mAge.getText().toString().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int curYear = calendar.get(Calendar.YEAR);
            int mAgeYear = Integer.parseInt(mAge.getText().toString());
            int birthYear = curYear - mAgeYear; //mAge will just be the year JS
            int curMonth = calendar.get(Calendar.MONTH);
            int birthMonth = curMonth; //There is no birth month JS
            int birthDay = calendar.get(Calendar.DAY_OF_MONTH);

            Locale.setDefault(Locale.ENGLISH);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            calendar.set(birthYear, birthMonth, birthDay);
            String dobString = simpleDateFormat.format(calendar.getTime());
            patientDTO.setDateofbirth(dobString);
        }
        patientDTO.setPatientIdentifier(visilantId);
        patientDTO.setLocationId(sessionManager.getLocationUuid());
        patientDTO.setUuid(patientuuid);
        try {
            patientsDAO.insertPatientToDB(patientDTO, patientuuid);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        //INSERT VISIT
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setPatientuuid(patientuuid);
        visitDTO.setUuid(visituuid);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        visitDTO.setStartdate(thisDate);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        //INSERT ENCOUNTER Adult Initial
        EncounterDTO encounterDTO = new EncounterDTO();
        String encounterUuid = uuidGenerator.UuidGenerator();
        encounterDTO.setPatientuuid(patientuuid);
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setProvideruuid(sessionManager.getCreatorID());
        encounterDTO.setUuid(encounterUuid);
        encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_ADULTINITIAL);
        encounterDTO.setVisituuid(visituuid);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        //inset encounter hospital imaging
        encounterDTO.setUuid(uuidGenerator.UuidGenerator());
        encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_HOSPITAL_IMAGING);
        try{
            encounterDAO.createEncountersToDB(encounterDTO);
        }catch (DAOException e){
            e.printStackTrace();
        }

        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VARight);
        obsDTO.setValue(vaRight);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VALeft);
        obsDTO.setValue(vaLeft);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeRight);
        obsDTO.setValue(phRight);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeLeft);
        obsDTO.setValue(phLeft);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.volunteerComplaintRight);
        obsDTO.setValue(String.valueOf(azureResults.getComplaintsRight()));
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.volunteerComplaintLeft);
        obsDTO.setValue(String.valueOf(azureResults.getComplaintsLeft()));
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public void dbUpdate(){
        PatientsDAO patientsDAO = new PatientsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();

        //Update PATIENT
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setGender(mGender);

        if (!mAge.getText().toString().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int curYear = calendar.get(Calendar.YEAR);
            int mAgeYear = Integer.parseInt(mAge.getText().toString());
            int birthYear = curYear - mAgeYear; //mAge will just be the year JS
            int curMonth = calendar.get(Calendar.MONTH);
            int birthMonth = curMonth; //There is no birth month JS
            int birthDay = calendar.get(Calendar.DAY_OF_MONTH);

            Locale.setDefault(Locale.ENGLISH);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            calendar.set(birthYear, birthMonth, birthDay);
            String dobString = simpleDateFormat.format(calendar.getTime());
            patientDTO.setDateofbirth(dobString);
        }
        patientDTO.setPatientIdentifier(visilantId);
        patientDTO.setLocationId(sessionManager.getLocationUuid());
        patientDTO.setUuid(patientuuid);
        try {
            patientsDAO.updatePatientToDB(patientDTO, patientuuid, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }

        //Update Obs
        String encounterUuid = encounterDAO.getEncounterUuidByVisit(visituuid);

        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VARight);
        obsDTO.setValue(vaRight);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VALeft);
        obsDTO.setValue(vaLeft);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeRight);
        obsDTO.setValue(phRight);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeLeft);
        obsDTO.setValue(phLeft);
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.volunteerComplaintRight);
        obsDTO.setValue(String.valueOf(azureResults.getComplaintsRight()));
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.volunteerComplaintLeft);
        obsDTO.setValue(String.valueOf(azureResults.getComplaintsLeft()));
        obsDTO.setEncounteruuid(encounterUuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        obsDAO.updateObsFromEncounter(obsDTO);
    }

    public static boolean isParsable(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

}

