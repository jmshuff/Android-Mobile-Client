package app.insightfuleye.client.activities.uploadImageActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.identificationActivity.IdentificationActivity;
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
    Spinner mVARight;
    Spinner mVALeft;
    Spinner mPinholeRight;
    Spinner mPinholeLeft;
    ImageView mImageViewRight;
    ImageView mImageViewLeft;
    private String mGender;
    ImagesDAO imagesDAO = new ImagesDAO();
    private String mCurrentPhotoPath;
    Context context;
    String patientID_edit;
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
        mVARight=findViewById(R.id.spinner_varight);
        mVALeft=findViewById(R.id.spinner_valeft);
        mPinholeRight=findViewById(R.id.spinner_pinholeright);
        mPinholeLeft=findViewById(R.id.spinner_pinholeleft);
        mImageViewRight=findViewById(R.id.imageview_right_eye_picture);
        mImageViewLeft=findViewById(R.id.imageview_left_eye_picture);

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

        mVARight.setAdapter(vaRightAdapter);
        mVALeft.setAdapter(vaLeftAdapter);
        mPinholeRight.setAdapter(phRightAdapter);
        mPinholeLeft.setAdapter(phLeftAdapter);

        if(patientID_edit != null){
            mVARight.setSelection(vaRightAdapter.getPosition(String.valueOf(patient.getVARight())));
            mVALeft.setSelection(vaLeftAdapter.getPosition(String.valueOf(patient.getVALeft())));
            mPinholeRight.setSelection(phRightAdapter.getPosition(String.valueOf(patient.getPinholeRight())));
            mPinholeLeft.setSelection(phLeftAdapter.getPosition(String.valueOf(patient.getPinholeLeft())));
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
    }



    private void setscreen(String patientId){

    }



}
