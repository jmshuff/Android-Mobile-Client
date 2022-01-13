package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.cameraActivity.CameraActivity;
import app.insightfuleye.client.activities.identificationActivity.IdentificationActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.dto.PatientDTO;
import app.insightfuleye.client.utilities.SessionManager;

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
    Context context;
    String patientID_edit;
    String patientId;
    azureResults patient= new azureResults();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    //load past details to edit
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                patientID_edit = intent.getStringExtra("patientUuid");
                setscreen(patientID_edit);
            }
        }
            //Age need to convert from Birthday
        // mAge=patient.getAge();
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

        if(patientID_edit != null){
            spinVARight.setSelection(vaRightAdapter.getPosition(String.valueOf(patient.getVARight())));
            spinVALeft.setSelection(vaLeftAdapter.getPosition(String.valueOf(patient.getVALeft())));
            spinPinholeRight.setSelection(phRightAdapter.getPosition(String.valueOf(patient.getPinholeRight())));
            spinPinholeLeft.setSelection(phLeftAdapter.getPosition(String.valueOf(patient.getPinholeLeft())));
        }

        if (patientID_edit != null) {
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
                String imageName = UUID.randomUUID().toString();
                File filePath = new File(AppConstants.IMAGE_PATH + imageName);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });

        mImageViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageName = UUID.randomUUID().toString();
                File filePath = new File(AppConstants.IMAGE_PATH + imageName);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(uploadImageInfoActivity.this, CameraActivity.class);
                // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
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
            if (patientID_edit != null) {
                onUploadUpdateClicked(patient);
            } else {
                onUploadCreateClicked();
            }
        });

    }


    private void setscreen(String patientId){

    }

    public void onUploadUpdateClicked(azureResults patient){

    }
    public void onUploadCreateClicked(){

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
        mGenderM.setError(null);
        mGenderF.setError(null);
    }



}
