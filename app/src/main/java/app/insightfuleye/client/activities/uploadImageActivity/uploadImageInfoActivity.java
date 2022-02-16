package app.insightfuleye.client.activities.uploadImageActivity;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.cameraActivity.CameraActivity;
import app.insightfuleye.client.activities.identificationActivity.IdentificationActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.dto.PatientDTO;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.exception.DAOException;

public class uploadImageInfoActivity extends AppCompatActivity {
    private static final String TAG = uploadImageInfoActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    EditText mAge;
    RadioButton mGenderM;
    RadioButton mGenderF;
    //RadioButton mohit;
    //RadioButton aravindStaff;
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
    String visitId_edit;
    String visitId;
    azureResults patient= new azureResults();
    //make a listview adapter. Never do this. This is such bad coding. I was in a hurry. I apologize to future me
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
    private String prototypeString;

    LinearLayout previewRight;
    LinearLayout previewLeft;
    TextView mPrototype;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image_info);
        setTitle("Upload Image");
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
        //mohit=findViewById(R.id.upload_image_mohit);
        //aravindStaff=findViewById(R.id.upload_image_person_other);
        spinVARight=findViewById(R.id.spinner_varight);
        spinVALeft=findViewById(R.id.spinner_valeft);
        spinPinholeRight=findViewById(R.id.spinner_pinholeright);
        spinPinholeLeft=findViewById(R.id.spinner_pinholeleft);
        mImageViewRight=findViewById(R.id.imageview_right_eye_picture);
        mImageViewLeft=findViewById(R.id.imageview_left_eye_picture);
        vaLeftText=findViewById(R.id.valeft);
        vaRightText=findViewById(R.id.varight);
        phLeftText=findViewById(R.id.pinholeleft);
        phRightText=findViewById(R.id.pinholeright);
//the absolute worst idea. kill me
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

        previewRight=findViewById(R.id.preview_right_eye_picture);
        previewLeft=findViewById(R.id.preview_left_eye_picture);
        mPrototype=findViewById(R.id.upload_image_prototype);


        //load past details to edit
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            prototypeString=intent.getStringExtra("prototype");
            if (intent.hasExtra("visitId")) {
                visitId_edit = intent.getStringExtra("visitId");
                setscreen(visitId_edit);
            }
        }
        String prototypeDisplay="Prototype: " + prototypeString;
        mPrototype.setText(prototypeDisplay);

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

        if(visitId_edit != null){
            //set spinners for edit
            spinVARight.setSelection(vaRightAdapter.getPosition(String.valueOf(patient.getVARight())));
            spinVALeft.setSelection(vaLeftAdapter.getPosition(String.valueOf(patient.getVALeft())));
            spinPinholeRight.setSelection(phRightAdapter.getPosition(String.valueOf(patient.getPinholeRight())));
            spinPinholeLeft.setSelection(phLeftAdapter.getPosition(String.valueOf(patient.getPinholeLeft())));
            //set images for edit
            File imageNameRight=new File(AppConstants.IMAGE_PATH + patient.getImagePath()+ ".jpg");
            File imageNameLeft= new File(AppConstants.IMAGE_PATH + patient.getImageId() +".jpg");
            if (imageNameRight.exists()){
                mImageViewRight.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(imageNameRight)));
            }
            if(imageNameLeft.exists()){
                mImageViewLeft.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(imageNameLeft)));
            }
            mAge.setText(patient.getAge());
            if (patient.getDiagnosisRight()!=null) setCheckedRight(patient.getDiagnosisRight());
            if (patient.getComplaintsRight()!=null) setCheckedRight(patient.getComplaintsRight());
            if (patient.getDiagnosisLeft()!=null) setCheckedLeft(patient.getDiagnosisLeft());
            if (patient.getComplaintsLeft()!= null) setCheckedLeft(patient.getComplaintsLeft());
        }

        if (visitId_edit != null) {
            if (patient.getSex().equals("M")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
                Log.v(TAG, "yes");
            }
//            if(patient.getChwName().equals("Mohit")){
//                mohit.setChecked(true);
//                if(aravindStaff.isChecked())
//                    aravindStaff.setChecked(false);
//            }else{
//                aravindStaff.setChecked(true);
//                if(mohit.isChecked())
//                    mohit.setChecked(false);
//            }

        }
        if (mGenderM.isChecked()) {
            mGender = "M";

        } else {
            mGender = "F";
        }

//        if(mohit.isChecked()){
//            mImager="Mohit";
//        } else{
//            mImager="Aravind Staff";
//        }

     previewRight.setVisibility(View.GONE);

     previewLeft.setVisibility(View.GONE);

        spinVARight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vaRight = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinVALeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vaLeft = parent.getItemAtPosition(position).toString();
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

//        mohit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onRadioButtonClicked2(v);
//            }
//        });
//
//        aravindStaff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onRadioButtonClicked2(v);
//            }
//        });

        mImageViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (patient.getImagePath()!=null){
                    previewImage(uploadImageInfoActivity.this, patient.getImagePath(), "right");

                }else {
                    String imageNameRight = UUID.randomUUID().toString();
                    File filePath = new File(AppConstants.IMAGE_PATH + imageNameRight);
                    if (!filePath.exists()) {
                        filePath.mkdir();
                    }
                    patient.setImagePath(imageNameRight);
                    Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                    // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageNameRight);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                    cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, "right");
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                }
            }
        });

        mImageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (patient.getImageId()!=null){
                    previewImage(uploadImageInfoActivity.this, patient.getImageId(), "left");
                }

                else {
                    String imageNameLeft = UUID.randomUUID().toString();
                    File filePath = new File(AppConstants.IMAGE_PATH + imageNameLeft);
                    if (!filePath.exists()) {
                        filePath.mkdir();
                    }
                    patient.setImageId(imageNameLeft);
                    Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                    // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageNameLeft);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                    cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, "left");
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            patient.setAge(mAge.getText().toString());
            Log.d("Age", patient.getAge());
            patient.setVARight(vaRight);
            patient.setVALeft(vaLeft);
            patient.setPinholeRight(phRight);
            patient.setPinholeLeft(phLeft);
            patient.setSex(mGender);
            patient.setChwName(prototypeString);
            if (visitId_edit != null) {
                try {
                    updateAzureImageDatabase();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    insertAzureImageDatabase();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
            Intent intent1= new Intent(uploadImageInfoActivity.this, uploadImageActivity.class);
            startActivity(intent1);
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                mType= data.getStringExtra("Type");
                Log.d("mType", mType);
                if(mType.contains("right")) {
                    Log.d("mType","right");
                    Glide.with(this)
                            .load(new File(mCurrentPhotoPath))
                            .thumbnail(0.25f)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(mImageViewRight);
                }
                else{
                    Glide.with(this)
                            .load(new File(mCurrentPhotoPath))
                            .thumbnail(0.25f)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(mImageViewLeft);
                }
            }
        }
    }


    private void setscreen(String visitId){

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "visitId=?";
        String[] Args = {visitId};
        String[] Columns = {"visitId", "imageName", "imageName2", "VARight", "VALeft", "PinholeRight", "PinholeLeft", "age", "sex","creatorId", "diagnosisRight", "diagnosisLeft", "complaintsRight", "complaintsLeft"};
        Cursor idCursor = db.query("tbl_azure_additional_docs", Columns, patientSelection, Args, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient.setVisitId(idCursor.getString(idCursor.getColumnIndexOrThrow("visitId")));
                patient.setSex(idCursor.getString(idCursor.getColumnIndexOrThrow("sex")));
                patient.setVARight(idCursor.getString(idCursor.getColumnIndexOrThrow("VARight")));
                patient.setVALeft(idCursor.getString(idCursor.getColumnIndexOrThrow("VALeft")));
                patient.setPinholeRight(idCursor.getString(idCursor.getColumnIndexOrThrow("PinholeRight")));
                patient.setPinholeLeft(idCursor.getString(idCursor.getColumnIndexOrThrow("PinholeLeft")));
                patient.setAge(idCursor.getString(idCursor.getColumnIndexOrThrow("age")));
                patient.setImagePath(idCursor.getString(idCursor.getColumnIndexOrThrow("imageName"))); //imagePath for imageNameRight
                patient.setImageId(idCursor.getString(idCursor.getColumnIndexOrThrow("imageName2"))); //imageID for imageNameLeft
                patient.setChwName(idCursor.getString(idCursor.getColumnIndexOrThrow("creatorId")));
                if (idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisRight"))!=null)patient.setDiagnosisRight(new ArrayList<>( Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisRight"))).split(","))));
                if (idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisLeft"))!=null)patient.setDiagnosisLeft(new ArrayList<>(Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisLeft"))).split(","))));
                if (idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsRight"))!=null)patient.setComplaintsRight(new ArrayList<>(Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsRight"))).split(","))));
                if (idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsLeft"))!=null)patient.setComplaintsLeft(new ArrayList<>( Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsLeft"))).split(","))));

            } while (idCursor.moveToNext());
            idCursor.close();
        }

    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.upload_image_gender_male:
                if (checked)
                    mGender = "M";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.upload_image_gender_female:
                if (checked)
                    mGender = "F";
                Log.v(TAG, "gender:" + mGender);
                break;
        }
        patient.setSex(mGender);
        mGenderM.setError(null);
        mGenderF.setError(null);
    }
//    public void onRadioButtonClicked2(View view) {
//        boolean checked = ((RadioButton) view).isChecked();
//        switch (view.getId()) {
//            case R.id.upload_image_mohit:
//                if(checked)
//                    mImager="Mohit";
//                break;
//            case R.id.upload_image_person_other:
//                if(checked)
//                    mImager="Aravind Staff";
//                break;
//        }
//        patient.setChwName(mPrototype);
//    }


    public boolean updateAzureImageDatabase() throws DAOException {
        boolean isUpdated =false;
        SQLiteDatabase localdb=AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues=new ContentValues();
        try{
            contentValues.put("VARight", patient.getVARight());
            contentValues.put("VALeft", patient.getVALeft());
            contentValues.put("PinholeRight", patient.getPinholeRight());
            contentValues.put("PinholeLeft", patient.getPinholeLeft());
            contentValues.put("age", patient.getAge());
            contentValues.put("sex", mGender);
            contentValues.put("imageName", patient.getImagePath()); //imageNameRight
            contentValues.put("imageName2", patient.getImageId()); //imageNameLeft
            if (patient.getComplaintsRight()!=null) contentValues.put("complaintsRight", patient.getComplaintsRight().toString());
            if (patient.getComplaintsLeft()!=null) contentValues.put("complaintsLeft", patient.getComplaintsLeft().toString());
            if (patient.getDiagnosisRight()!= null) contentValues.put("diagnosisRight", patient.getDiagnosisRight().toString());
            if (patient.getDiagnosisLeft()!=null) contentValues.put("diagnosisLeft", patient.getDiagnosisLeft().toString());
            localdb.updateWithOnConflict("tbl_azure_additional_docs", contentValues, "visitId = ?", new String[]{visitId_edit}, SQLiteDatabase.CONFLICT_REPLACE);
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
        Gson gson = new Gson();
        try {
            contentValues.put("imageName", patient.getImagePath()); //imageNameRight
            contentValues.put("imageName2", patient.getImageId()); //imageNmaeLeft
            contentValues.put("patientId", "Hospital-Upload");
            contentValues.put("visitId", UUID.randomUUID().toString());
            //contentValues.put("creatorId", sessionManager.getChwname());
            contentValues.put("creatorId", mImager);
            contentValues.put("VARight", patient.getVARight());
            contentValues.put("VALeft", patient.getVALeft());
            contentValues.put("PinholeRight", patient.getPinholeRight());
            contentValues.put("PinholeLeft", patient.getPinholeLeft());
            contentValues.put("age", patient.getAge());
            contentValues.put("sex", mGender);
            if(patient.getComplaintsRight()!=null) contentValues.put("complaintsRight", patient.getComplaintsRight().toString());
            if (patient.getComplaintsLeft()!=null) contentValues.put("complaintsLeft", patient.getComplaintsLeft().toString());
            if (patient.getDiagnosisRight()!=null) contentValues.put("diagnosisRight", patient.getDiagnosisRight().toString());
            if (patient.getComplaintsLeft()!=null) contentValues.put("diagnosisLeft", patient.getDiagnosisLeft().toString());

            //contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_azure_additional_docs", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
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
        if(patient.getDiagnosisRight()!=null){
            selectedStrings =patient.getDiagnosisRight();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        patient.setDiagnosisRight(selectedStrings);
        Log.d("SelectDiagR", patient.getDiagnosisRight().toString());
    }

    public void onDiagnosisLeftClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(patient.getDiagnosisLeft()!=null){
            selectedStrings=patient.getDiagnosisLeft();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        patient.setDiagnosisLeft(selectedStrings);

    }

    public void onComplaintRightClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(patient.getComplaintsRight()!=null){
            selectedStrings=patient.getComplaintsRight();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        patient.setComplaintsRight(selectedStrings);
    }

    public void onComplaintLeftClicked(View v){
        boolean checked = ((CheckBox) v).isChecked();
        ArrayList<String> selectedStrings = new ArrayList<String>();
        if(patient.getComplaintsLeft()!=null){
            selectedStrings=patient.getComplaintsLeft();
        }
        if(checked){
            selectedStrings.add(((CheckBox) v).getText().toString());
        }
        else{
            selectedStrings.remove(((CheckBox) v).getText().toString());
        }
        patient.setComplaintsLeft(selectedStrings);
    }


    public void setCheckedRight(ArrayList<String> checked){
        for (String item: checked){
            if (item.contains("Mature Cataract")) matCatR.setChecked(true);
            else if(item.contains("Immature Cataract"))immatCatR.setChecked(true);
            else if(item.contains("Pterygium")) pterygiumR.setChecked(true);
            else if(item.contains("Corneal Opacity")) cornealOpacityR.setChecked(true);
            else if (item.contains("Corneal Ulcer")) cornealUlcerR.setChecked(true);
            else if(item.contains("Normal")) normalR.setChecked(true);
            else if(item.contains("PC IOL")) pciolR.setChecked(true);

            else if(item.contains("Blurry Vision Close")) blurryCloseR.setChecked(true);
            else if (item.contains("Blurry Vision Far")) blurryFarR.setChecked(true);
            else if (item.contains("Redness")) rednessR.setChecked(true);
            else if (item.contains("Eye Pain")) eyePainR.setChecked(true);
            else if (item.contains("Headache")) headacheR.setChecked(true);
            else if (item.contains("Eye Trauma")) eyeTraumaR.setChecked(true);
        }

    }
    public void setCheckedLeft(ArrayList<String> checked){
        for (String item: checked){
            if (item.contains("Mature Cataract")) matCatL.setChecked(true);
            else if(item.contains("Immature Cataract"))immatCatL.setChecked(true);
            else if(item.contains("Pterygium")) pterygiumL.setChecked(true);
            else if(item.contains("Corneal Opacity")) cornealOpacityL.setChecked(true);
            else if (item.contains("Corneal Ulcer")) cornealUlcerL.setChecked(true);
            else if(item.contains("Normal")) normalL.setChecked(true);
            else if (item.contains("PC IOL")) pciolL.setChecked(true);

            else if(item.contains("Blurry Vision Close")) blurryCloseL.setChecked(true);
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
                File temp = new File(path);
                if (temp.exists()) temp.delete();
                Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, path);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH+path);
                cameraIntent.putExtra(CameraActivity.SET_EYE_TYPE, type);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
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
                        .load(new File(AppConstants.IMAGE_PATH+path+".jpg"))
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

        Log.d("Download Image: ", AppConstants.IMAGE_PATH+path+".jpg");

        Bitmap bitmap = BitmapFactory.decodeFile(AppConstants.IMAGE_PATH+path+".jpg");

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

        /*
        File oldFile=new File(AppConstants.IMAGE_PATH+path+".jpg");
        File newFile = new File(direct+path+".jpg");
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(oldFile).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputChannel != null) {
                try {
                    inputChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputChannel != null) {
                try {
                    outputChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

         */

/*
        InputStream in = null;
        OutputStream out = null;
        try {
                in = new FileInputStream(AppConstants.IMAGE_PATH + path + ".jpg");
                out = new FileOutputStream(direct + path + ".jpg");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                // write the output file (You have now copied the file)
                out.flush();
                out.close();
                out = null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

 */

}

