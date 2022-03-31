package app.insightfuleye.client.activities.physcialExamActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.familyHistoryActivity.FamilyHistoryActivity;
import app.insightfuleye.client.activities.questionNodeActivity.QuestionsAdapter;
import app.insightfuleye.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ImagesPushDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.knowledgeEngine.PhysicalExam;
import app.insightfuleye.client.models.azureResults;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.utilities.FileUtils;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;
import app.insightfuleye.client.utilities.pageindicator.ScrollingPagerIndicator;

public class PhysicalExamActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {
    final static String TAG = PhysicalExamActivity.class.getSimpleName();
    // private SectionsPagerAdapter mSectionsPagerAdapter;

    // private ViewPager mViewPager;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Uri image_uri;

    static String patientUuid;
    static String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;

    ArrayList<String> selectedExamsList;

    SQLiteDatabase localdb;


    String imageName;
    static String baseDir;
    static File filePath;
    String azureType=null;



    String mFileName = "physExam.json";


    PhysicalExam physicalExamMap;

    String physicalString;
    String physicalDisplay;
    Boolean complaintConfirmed = false;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    SessionManager sessionManager;
    RecyclerView physExam_recyclerView;
    QuestionsAdapter adapter;
    String mgender;
    String mAge;
    ScrollingPagerIndicator recyclerViewIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        sessionManager = new SessionManager(this);
/*
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle(R.string.open_peek_acuity);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.peek_acuity_open, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent2 = getPackageManager().getLaunchIntentForPackage("org.peekvision.public.android"); //JS
                if (intent2 != null) {
                    // We found the activity now start the activity
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                } else {
                    // Bring user to the market or let them choose an app?
                    intent2 = new Intent(Intent.ACTION_VIEW);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.setData(Uri.parse("market://details?id=" + "org.peekvision.public.android"));
                    startActivity(intent2);
                }

            }
        });
        alertDialogBuilder.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        //alertDialog.show();

        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        //pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
*/


        //select exams
        selectedExamsList = new ArrayList<>();
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            selectedExamsList.clear();
            if (selectedExams != null)
                selectedExamsList.addAll(selectedExams);
            filePath = new File(AppConstants.IMAGE_PATH);
        }


        if ((selectedExamsList == null) || selectedExamsList.isEmpty()) {
            Log.d(TAG, "No additional exams were triggered");
            physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, mFileName), selectedExamsList);
        } else {
            Set<String> selectedExamsWithoutDuplicates = new LinkedHashSet<>(selectedExamsList);
            Log.d(TAG, selectedExamsList.toString());
            selectedExamsList.clear();
            selectedExamsList.addAll(selectedExamsWithoutDuplicates);
            Log.d(TAG, selectedExamsList.toString());
            for (String string : selectedExamsList)
                Log.d(TAG, string);

            boolean hasLicense = false;
//            if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
            if (!sessionManager.getLicenseKey().isEmpty())
                hasLicense = true;

            if (hasLicense) {
                try {
                    JSONObject currentFile = null;
                    currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                    physicalExamMap = new PhysicalExam(currentFile, selectedExamsList);
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, mFileName), selectedExamsList);


            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_exam);
        setTitle(getString(R.string.title_activity_physical_exam));

        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setTitle(patientName + ": " + getTitle());
        physExam_recyclerView = findViewById(R.id.physExam_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        physExam_recyclerView.setLayoutManager(linearLayoutManager);
        physExam_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(physExam_recyclerView);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
       /* mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), physicalExamMap);
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }*/

        /*TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorHeight(15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.amber));
            tabLayout.setTabTextColors(getColor(R.color.white), getColor(R.color.amber));
        } else {
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.amber));
            tabLayout.setTabTextColors(getResources().getColor(R.color.white), getResources().getColor(R.color.amber));
        }
        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
         */

      /*
      Commented to avoid crash...
        Log.e(TAG, "PhyExam: " + physicalExamMap.getTotalNumberOfExams());*/

        mgender = PatientsDAO.fetch_gender(patientUuid);
        mAge= PatientsDAO.fetch_age(patientUuid);
        if (mgender.equalsIgnoreCase("M")) {
            physicalExamMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            physicalExamMap.fetchItem("1");
        }
        physicalExamMap.refresh(selectedExamsList); //refreshing the physical exam nodes with updated json

        // flaoting value of age is passed to Node for comparison...
        physicalExamMap.fetchAge(float_ageYear_Month);
        physicalExamMap.refresh(selectedExamsList); //refreshing the physical exam nodes with updated json

        adapter = new QuestionsAdapter(this, physicalExamMap, physExam_recyclerView, this.getClass().getSimpleName(), this, false);
        physExam_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(physExam_recyclerView);

    }

    private boolean insertDb(String value) {
        Log.i(TAG, "insertDb: ");

        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(value));

        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }



        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VARight);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVARight());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VALeft);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVALeft());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeRight);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getPinholeRight());

        try {
            isInserted=obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PinholeLeft);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getPinholeLeft());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VolunteerReferral);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVolunteerReferral());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VolunteerReferralLocation);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVolunteerReferralLocation());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VolunteerDiagnosisRight);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVolunteerDiagnosisRight());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.VolunteerDiagnosisLeft);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(physicalExamMap.getVolunteerDiagnosisLeft());

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }


    @SuppressLint("LongLogTag")
    @Override
    public void fabClickedAtEnd() {

        complaintConfirmed = physicalExamMap.areRequiredAnswered();

        if (complaintConfirmed) {
            physicalExamMap.getPhysicalConcepts();
            physicalDisplay=physicalExamMap.generateFindings();
            physicalString = physicalExamMap.generateTable();


            List<String> imagePathList = physicalExamMap.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase();
                }
            }
            //update azure database
            try {
                updateAzureImageDatabase();
            } catch (DAOException e) {
                e.printStackTrace();
            }
            //Print Queue
            ImagesDAO imagesDAO = new ImagesDAO();
            List<azureResults> azureQueue= new ArrayList<>();
            try {
                azureQueue=imagesDAO.getAzureImageQueue();
                Log.d("AzureQueue", azureQueue.toString());
            } catch (DAOException e) {
                e.printStackTrace();
            }
            //upload all images to Azure
            ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
            try {
                imagesPushDAO.azureImagePush();
            } catch (DAOException e) {
                e.printStackTrace();
            }


            if (intentTag != null && intentTag.equals("edit")) {
                updateDatabase(physicalString);
                //updateVAConcepts();
                Intent intent = new Intent(PhysicalExamActivity.this, VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent.putExtra("tag", intentTag);
                intent.putExtra("hasPrescription", "false");
                intent.putExtra("physicalDisplay", physicalDisplay);

                for (String exams : selectedExamsList) {
                    Log.i(TAG, "onClick:++ " + exams);
                }
                // intent.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent);
            } else {
                boolean obsId = insertDb(physicalString);
                Log.i(TAG, "In inserted"+ obsId);
                Intent intent1 = new Intent(PhysicalExamActivity.this, VisitSummaryActivity.class); // earlier visitsummary
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent1.putExtra("state", state);
                intent1.putExtra("name", patientName);
                intent1.putExtra("tag", intentTag);
                intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent1.putExtra("hasPrescription", "false");
                intent1.putExtra("physicalDisplay", physicalDisplay);
                // intent1.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent1);
            }

        } else {
            questionsMissing();
        }

    }



    @Override
    public void onChildListClickEvent(int groupPosition, int childPos, int physExamPos, String type) {
        Node question = physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).getOption(childPos);
        //Log.d("Clicked", question.language());
        question.toggleSelected();
        if (physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).anySubSelected()) {
            physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setSelected(true);
        } else {
            physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setUnselected();
        }

        if (physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).isBilateral()){
            Log.d("QuestionisBilateral", "true");
            if(type=="right" || type=="both"){
                //Log.d("SetRSelect", "true");
                question.toggleRightSelected();
                if (physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).anySubRightSelected()) {
                    physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setRightSelected(true);
                } else {
                    physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setRightUnselected();
                }
            }
            if(type=="left" || type=="both"){
                question.toggleLeftSelected();
                //Log.d("SetLSelect", "true");
                if (physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).anySubLeftSelected()) {
                    physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setLeftSelected(true);
                } else {
                    physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setLeftUnselected();
                }
            }
        }
        adapter.notifyDataSetChanged();




        if (question.getInputType() != null && question.isSelected()) {

            if (question.getInputType().equals("camera")) {
                if (!filePath.exists()) {
                    boolean res = filePath.mkdirs();
                    Log.i("RES>", "" + filePath + " -> " + res);
                }
                imageName = UUID.randomUUID().toString();
                Log.d("azureimagename", imageName);
                if (physicalExamMap.getExamNode(physExamPos).getText().toLowerCase().contains("right")){
                    azureType="right";
                }
                if (physicalExamMap.getExamNode(physExamPos).getText().toLowerCase().contains("left")){
                    azureType="left";
                }
            Log.d("azuretype", azureType);
                    //Node.handleQuestion(question, this, adapter, filePath.toString(), imageName);
                    manageCameraPermissions(imageName);

            } else {
                Node.handleQuestion(question, this, adapter, null, null);
            }


        }

        if (!question.isTerminal() && question.isSelected()) {
            Node.subLevelQuestion(question, this, adapter, filePath.toString(), imageName);
        }

        //JS, get values for physical exam questions
        /*Node category= physicalExamMap.getExamNode(physExamPos);
        //category equals the first branch in free mind
        if (category.getText().equals("VA right")){
            String VAright=category.formConceptLanguage();
            physicalExamMap.setVARight(VAright);
        }
        if (category.getText().equals("VA left")){
            String Valeft=category.formConceptLanguage();
            physicalExamMap.setVALeft(Valeft);
        }

        if (category.getText().equals("Pinhole right")){
            String Pinholeright=category.formConceptLanguage();
            physicalExamMap.setPinholeRight(Pinholeright);
        }
        if (category.getText().equals("Pinhole left")){
            String Pinholeleft=category.formConceptLanguage();
            physicalExamMap.setPinholeLeft(Pinholeleft);
        }

        if (category.getText().equals("Referral")){
            String volunteerReferral=category.formConceptLanguage();
            physicalExamMap.setVolunteerReferral(volunteerReferral);
            //physicalExamMap.setVolunteerReferral(volunteerReferral.split(" ")[0]);
            if (volunteerReferral.split(" ").length > 1){
                physicalExamMap.setVolunteerReferralLocation(volunteerReferral.split(" ")[1]);
            }


        }

        if (category.getText().equals("Referral Reason")){
            String volunteerReferralReason=category.formConceptLanguage();
            String diagnosisRight= "";
            String diagnosisLeft="";
            Log.d("Referral Reason", volunteerReferralReason);
            String[] reasons=volunteerReferralReason.split("\\. - ");
            Log.d("Reasons", Arrays.toString(reasons));
            for (String reason : reasons){
                Log.d("Diagnosis", reason);
                if (reason.toLowerCase().contains("right")){
                    diagnosisRight=diagnosisRight.concat(reason.split("-")[0]);
                    diagnosisRight=diagnosisRight.concat(",");
                }
                if (reason.toLowerCase().contains("left")){
                    diagnosisLeft=diagnosisLeft.concat(reason.split("-")[0]);
                    diagnosisLeft=diagnosisLeft.concat(",");
                }
            }
            //remove last comma character
            if (diagnosisRight.length()>1){
                diagnosisRight=diagnosisRight.substring(0, diagnosisRight.length()-1);
            }
            if (diagnosisLeft.length()>1){
                diagnosisLeft=diagnosisLeft.substring(0,diagnosisLeft.length()-1);
            }
            physicalExamMap.setVolunteerDiagnosisRight(diagnosisRight);
            physicalExamMap.setVolunteerDiagnosisLeft(diagnosisLeft);

            Log.d("DiagnosisRight",diagnosisRight);
            Log.d("DiagnosisLeft", diagnosisLeft);
        }
        */


    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PhysicalExam exams;

        public SectionsPagerAdapter(FragmentManager fm, PhysicalExam inputNode) {
            super(fm);
            this.exams = inputNode;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, exams, patientUuid, visitUuid);
        }

        @Override
        public int getCount() {
            return exams.getTotalNumberOfExams();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return exams.getTitle(position);
            return String.valueOf(position + 1);
        }
    }

    private void updateDatabase(String string) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION));

            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.VARight);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getVARight());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.VARight));
            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.VALeft);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getVALeft());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.VALeft));
            obsDAO.updateObs(obsDTO);


            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.PinholeRight);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getPinholeRight());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PinholeRight));
            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.PinholeLeft);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getPinholeLeft());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PinholeLeft));
            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.VolunteerReferral);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getVolunteerReferral());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.VolunteerReferral));
            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.VolunteerDiagnosisRight);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getVolunteerDiagnosisRight());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.VolunteerDiagnosisRight));
            obsDAO.updateObs(obsDTO);

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.VolunteerReferralLocation);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(physicalExamMap.getVolunteerReferralLocation());
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.VolunteerReferralLocation));
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


    public void questionsMissing() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(R.string.question_answer_all_phy_exam);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void updateImageDatabase() {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_CODE) {
            if (resultCode == RESULT_OK) {
                //String mCurrentPhotoPath = data.getStringExtra("RESULT");
                String mCurrentPhotoPath= AppConstants.IMAGE_PATH + imageName + ".jpg";
                physicalExamMap.setImagePath(mCurrentPhotoPath);
                Log.i(TAG, mCurrentPhotoPath);
                //physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                updateImageDatabase();
                //uploadAzureImage(filePath.getAbsolutePath(), imageName);
                //instead of uploading it now, let's queue it now and upload everything later
                try {
                    insertAzureImageDatabase(azureType, imageName+".jpg");
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                //Test, code to print list of queded images for testing
                ImagesDAO imagesDAO = new ImagesDAO();
                List<azureResults> azureQueue= new ArrayList<>();
                try {
                    azureQueue=imagesDAO.getAzureImageQueue();
                    Log.d("AzureQueue", azureQueue.toString());
                } catch (DAOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public static PhysicalExam exam_list;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        CustomExpandableListAdapter adapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, PhysicalExam exams, String patientUuid, String visitUuid) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("patientUuid", patientUuid);
            args.putString("visitUuid", visitUuid);
            exam_list = exams;
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_physical_exam, container, false);

            final ImageView imageView = rootView.findViewById(R.id.physical_exam_image_view);

            TextView textView = rootView.findViewById(R.id.physical_exam_text_view);
            ExpandableListView expandableListView = rootView.findViewById(R.id.physical_exam_expandable_list_view);

            int viewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final String patientUuid1 = getArguments().getString("patientUuid");
            final String visitUuid1 = getArguments().getString("visitUuid");
            final Node viewNode = exam_list.getExamNode(viewNumber - 1);
            final String parent_name = exam_list.getExamParentNodeName(viewNumber - 1);
            String nodeText = parent_name + " : " + viewNode.findDisplay();

            textView.setText(nodeText);
            Node displayNode = viewNode.getOption(0);

            if (displayNode.isAidAvailable()) {
                String type = displayNode.getJobAidType();
                if (type.equals("video")) {
                    imageView.setVisibility(View.GONE);
                } else if (type.equals("image")) {
                    imageView.setVisibility(View.VISIBLE);
                    String drawableName = "physicalExamAssets/" + displayNode.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = getContext().getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        imageView.setImageDrawable(d);
                        imageView.setMinimumHeight(500);
                        imageView.setMinimumWidth(500);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        imageView.setVisibility(View.GONE);
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }


            adapter = new CustomExpandableListAdapter(getContext(), viewNode, this.getClass().getSimpleName());
            expandableListView.setAdapter(adapter);


            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {


                    return false;
                }
            });

            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });

            expandableListView.expandGroup(0);


            return rootView;
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(
                PhysicalExamActivity.this, FamilyHistoryActivity.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
        intent.putExtra("state", state);
        intent.putExtra("name", patientName);
        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
        if (intentTag != null) {
            intent.putExtra("tag", intentTag);
        }
        //intent.putStringArrayListExtra("complaints", selection);

        startActivity(intent);

    }

    public void AnimateView(View v) {

        int fadeInDuration = 500; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        if (v != null) {
            v.setAnimation(animation);
        }


    }

    public void bottomUpAnimation(View v) {

        if (v != null) {
            v.setVisibility(View.VISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            v.startAnimation(bottomUp);
        }

    }

    /*private void uploadAzureImage(String filePath,String imageName) {
        File file = new File(filePath+"/"+imageName+".jpg");
        Log.d("Azure file", file.getName());
        Retrofit retrofit = AzureNetworkClient.getRetrofit();

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part parts = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody creatorId = RequestBody.create(MediaType.parse("text/plain"), sessionManager.getChwname());
        RequestBody visitId= RequestBody.create(MediaType.parse("text/plain"), visitUuid);
        RequestBody patientId= RequestBody.create(MediaType.parse("text/plain"), patientUuid);
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), azureType);

        AzureUploadAPI uploadApis = retrofit.create(AzureUploadAPI.class);


        Call call = uploadApis.uploadImage(parts, creatorId, visitId, patientId, type);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("Azure", response.toString());
                if (response.message()=="OK"){
                    Log.d("Azure", "success");
                }
                else{
                    //add to queue
                    Log.d("Azure", "add to queue");
                    try {
                        boolean isInserted=insertAzureImageDatabase(azureType, file.getName());
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("Azure", t.toString());
                try {
                    boolean isInserted=insertAzureImageDatabase(azureType, file.getName());
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

     */

    public boolean insertAzureImageDatabase(String type, String imageName) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("imageName", imageName);
            contentValues.put("patientId", patientUuid);
            contentValues.put("visitId", visitUuid);
            contentValues.put("creatorId", sessionManager.getChwname());
            contentValues.put("type", type);
            contentValues.put("VARight", "");
            contentValues.put("VALeft", "");
            contentValues.put("PinholeRight", "");
            contentValues.put("PinholeLeft", "");
            contentValues.put("age", "");
            contentValues.put("sex", "");
            contentValues.put("complaints", "");

            //contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_azure_uploads", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
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
    public boolean updateAzureImageDatabase() throws DAOException{
        boolean isUpdated =false;
        SQLiteDatabase localdb=AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues=new ContentValues();
        PatientsDAO patientsDAO= new PatientsDAO();
        String mAge=patientsDAO.fetch_age(patientUuid);
        String mGender=patientsDAO.fetch_gender(patientUuid);
        try{
            contentValues.put("VARight", physicalExamMap.getVARight());
            contentValues.put("VALeft", physicalExamMap.getVALeft());
            contentValues.put("PinholeRight", physicalExamMap.getPinholeRight());
            contentValues.put("PinholeLeft", physicalExamMap.getPinholeLeft());
            contentValues.put("age", mAge);
            contentValues.put("sex", mGender);
            localdb.updateWithOnConflict("tbl_azure_uploads", contentValues, "visitId = ?", new String[]{visitUuid}, SQLiteDatabase.CONFLICT_REPLACE);
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

    public void manageCameraPermissions(String imageName){

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
                openCameraNative(imageName);
            }
        }
        else{
            //system os < marshmellow
            openCameraNative(imageName);
        }

    }

    public void openCameraNative(String imageName) {
        String imagePath= AppConstants.IMAGE_PATH + imageName + ".jpg";
        File file = new File(AppConstants.IMAGE_PATH + imageName + ".jpg");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (file != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE);
            }
        }

    }



}
