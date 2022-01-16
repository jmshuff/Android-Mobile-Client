package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
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

        //load past details to edit
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("visitId")) {
                visitId_edit = intent.getStringExtra("visitId");
                setscreen(visitId_edit);
            }
        }
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
            Log.d("righteyediag", patient.getDiagnosisRight().toString());
            setCheckedRight(patient.getDiagnosisRight());
            setCheckedRight(patient.getComplaintsRight());
            setCheckedLeft(patient.getDiagnosisLeft());
            setCheckedLeft(patient.getComplaintsLeft());
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

        }
        if (mGenderM.isChecked()) {
            mGender = "M";

        } else {
            mGender = "F";
        }

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

        mImageViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        mImageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String[] Columns = {"visitId", "imageName", "imageName2", "VARight", "VALeft", "PinholeRight", "PinholeLeft", "age", "sex", "diagnosisRight", "diagnosisLeft", "complaintsRight", "complaintsLeft"};
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
                patient.setDiagnosisRight(new ArrayList<>( Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisRight"))).split(","))));
                patient.setDiagnosisLeft(new ArrayList<>(Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("diagnosisLeft"))).split(","))));
                patient.setComplaintsRight(new ArrayList<>(Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsRight"))).split(","))));
                patient.setComplaintsLeft(new ArrayList<>( Arrays.asList((idCursor.getString(idCursor.getColumnIndexOrThrow("complaintsLeft"))).split(","))));

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
            contentValues.put("complaintsRight", patient.getComplaintsRight().toString());
            contentValues.put("complaintsLeft", patient.getComplaintsLeft().toString());
            contentValues.put("diagnosisRight", patient.getDiagnosisRight().toString());
            contentValues.put("diagnosisLeft", patient.getDiagnosisLeft().toString());
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
            contentValues.put("creatorId", sessionManager.getChwname());
            contentValues.put("VARight", patient.getVARight());
            contentValues.put("VALeft", patient.getVALeft());
            contentValues.put("PinholeRight", patient.getPinholeRight());
            contentValues.put("PinholeLeft", patient.getPinholeLeft());
            contentValues.put("age", patient.getAge());
            contentValues.put("sex", mGender);
            contentValues.put("complaintsRight", patient.getComplaintsRight().toString());
            contentValues.put("complaintsLeft", patient.getComplaintsLeft().toString());
            contentValues.put("diagnosisRight", patient.getDiagnosisRight().toString());
            contentValues.put("diagnosisLeft", patient.getDiagnosisLeft().toString());

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

            else if(item.contains("Blurry Vision Close")) blurryCloseL.setChecked(true);
            else if (item.contains("Blurry Vision Far")) blurryFarL.setChecked(true);
            else if (item.contains("Redness")) rednessL.setChecked(true);
            else if (item.contains("Eye Pain")) eyePainL.setChecked(true);
            else if (item.contains("Headache")) headacheL.setChecked(true);
            else if (item.contains("Eye Trauma")) eyeTraumaL.setChecked(true);
        }

    }
}
