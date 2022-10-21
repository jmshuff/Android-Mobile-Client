package app.insightfuleye.client.activities.traumaHistoryActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import app.insightfuleye.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;

public class traumaHistoryActivity extends AppCompatActivity {
    SessionManager sessionManager=null;
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;
    int scrollPos;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    private String encounterVitals;
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";

    EditText mTraumaOccur;
    EditText mModeInjury;
    EditText mOtcType;
    EditText mOtcFrequency;
    EditText mTemType;
    EditText mTemFrequency;
    RadioGroup otcGroup;
    RadioGroup temGroup;
    RadioButton mOtcYes;
    RadioButton mOtcNo;
    RadioButton mTemYes;
    RadioButton mTemNo;
    RadioGroup doctorConsultGroup;
    RadioButton mDocYes;
    RadioButton mDocNo;
    boolean otcUsed;
    boolean temUsed;
    boolean docConsulted;
    TextInputLayout otcTypeLayout;
    TextInputLayout otcFreqLayout;
    TextInputLayout temTypeLayout;
    TextInputLayout temFreqLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trauma_history);
        setTitle("Trauma History");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

/*        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            scrollPos=intent.getIntExtra("scrollPos", 0);
        }

        mTraumaOccur=findViewById(R.id.trauma_occurrence);
        mModeInjury=findViewById(R.id.mode_of_injury);
        mModeInjury.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120), inputFilter_Name});
        mOtcType=findViewById(R.id.otc_type);
        mOtcType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120), inputFilter_Name});
        mOtcFrequency=findViewById(R.id.otc_frequency);
        mTemType=findViewById(R.id.tem_type);
        mTemType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120), inputFilter_Name});
        mTemFrequency=findViewById(R.id.tem_frequency);
        otcGroup=findViewById(R.id.radioGrp_otc);
        temGroup=findViewById(R.id.radioGrp_traditional);
        doctorConsultGroup=findViewById(R.id.radioGrp_drConsult);
        mOtcYes=findViewById(R.id.otc_yes);
        mOtcNo=findViewById(R.id.otc_no);
        mTemYes=findViewById(R.id.traditional_yes);
        mTemNo=findViewById(R.id.traditional_no);
        mDocYes=findViewById(R.id.doctor_yes_trauma);
        mDocNo=findViewById(R.id.doctor_no_trauma);

        otcTypeLayout=findViewById(R.id.otc_type_layout);
        otcFreqLayout=findViewById(R.id.otc_frequency_layout);
        temTypeLayout=findViewById(R.id.tem_type_layout);
        temFreqLayout=findViewById(R.id.tem_frequency_layout);
        otcTypeLayout.setVisibility(View.GONE);
        otcFreqLayout.setVisibility(View.GONE);
        temTypeLayout.setVisibility(View.GONE);
        temFreqLayout.setVisibility(View.GONE);

        mOtcYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otcFreqLayout.setVisibility(View.VISIBLE);
                otcTypeLayout.setVisibility(View.VISIBLE);
                onRadioButtonClicked(view);
            }
        });

        mOtcNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otcFreqLayout.setVisibility(View.GONE);
                otcTypeLayout.setVisibility(View.GONE);
                onRadioButtonClicked(view);
            }
        });

        mTemYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temFreqLayout.setVisibility(View.VISIBLE);
                temTypeLayout.setVisibility(View.VISIBLE);
                onRadioButtonClicked(view);
            }
        });

        mTemNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temTypeLayout.setVisibility(View.GONE);
                temFreqLayout.setVisibility(View.GONE);
                onRadioButtonClicked(view);
            }
        });

        mDocYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });

        mDocNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            fabClick();
        });
    }

    private InputFilter inputFilter_Name = new InputFilter() { //filter input for name fields
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && BlockCharacterSet_Name.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.traditional_yes:
                if (checked)
                    temUsed=true;
                break;
            case R.id.traditional_no:
                if (checked)
                    temUsed=false;
                break;

            case R.id.otc_yes:
                if (checked)
                    otcUsed=true;
                break;
            case R.id.otc_no:
                if (checked)
                    otcUsed=false;
                break;

            case R.id.doctor_yes_trauma:
                if (checked)
                    docConsulted = true;
                break;
            case R.id.doctor_no_trauma:
                if (checked)
                    docConsulted=false;
                break;
        }

    }

    private void fabClick() {
        String tHist="";
        if(!mModeInjury.getText().toString().isEmpty()){
            tHist += Node.bullet + " Trauma: " + mModeInjury.getText().toString() + "<br/>";
        }
        if(!mTraumaOccur.getText().toString().isEmpty()){
            tHist+= Node.bullet + " Occurred: " + mTraumaOccur.getText().toString() + " day(s) ago<br/>";
        }
        if(docConsulted==true)
            tHist+= Node.bullet + " Doctor consulted<br/>";
        else
            tHist+= Node.bullet + " No doctor consulted<br/>";
        if(otcUsed){
            if(!mOtcType.getText().toString().isEmpty()){
                tHist+= Node.bullet + " OTC Medication: " + mOtcType.getText().toString() + "<br/>";
            }
            if(!mOtcFrequency.getText().toString().isEmpty()){
                tHist+= Node.bullet + "";
            }
        }
        if(temUsed){
            if(!mTemType.getText().toString().isEmpty()){
                tHist+= Node.bullet + " Traditional Medication: " + mTemType.getText().toString() + "<br/>";
            }
            if(!mTemFrequency.getText().toString().isEmpty()){
                tHist+= Node.bullet + " ";
            }
        }

        Log.d("trauma", tHist);


        if (intentTag != null && intentTag.equals("edit")) {

            updateDb(tHist);

            Intent intent = new Intent(traumaHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("hasPrescription", "false");
            startActivity(intent);
        } else {
            insertDb(tHist);
            sessionManager.setReturning(false);
            Intent intent = new Intent(traumaHistoryActivity.this, PastMedicalHistoryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }
    }

    public boolean insertDb(String tHistory) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.TRAUMA_HISTORY);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(tHistory));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;

    }

    public void updateDb(String tHistory) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.TRAUMA_HISTORY);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(tHistory);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.TRAUMA_HISTORY));
            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }
        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


}
