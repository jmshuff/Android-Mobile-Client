package app.insightfuleye.client.activities.pastMedicalHistoryActivity;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.insightfuleye.client.activities.questionNodeActivity.QuestionNodeActivity;
import app.insightfuleye.client.activities.questionNodeActivity.QuestionsAdapter;
import app.insightfuleye.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.knowledgeEngine.PhysicalExam;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.models.imageDisplay;
import app.insightfuleye.client.utilities.FileUtils;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;
import app.insightfuleye.client.utilities.pageindicator.ScrollingPagerIndicator;

public class PastMedicalHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {

    String patient = "patient";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;
    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "";


    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;

    String mFileName = "patHist.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";
    String imageName;
    File filePath;

    String mgender;
    SQLiteDatabase localdb, db;

    boolean hasLicense = false;
    String edit_PatHist = "";

//  String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    // CustomExpandableListAdapter adapter;
    //ExpandableListView historyListView;

    String patientHistory;
    String phistory = "";

    boolean flag = false;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials,

    EncounterAdultInitial_LatestVisit;
    RecyclerView pastMedical_recyclerView;
    QuestionsAdapter adapter;
    ScrollingPagerIndicator recyclerViewIndicator;
    String new_result;
    ArrayList<imageDisplay> imageList;
    int scrollPos;

    ArrayList<String> nodeHeaders = new ArrayList<>();
    int complaintSize;
    int patHistSize;
    int physExamSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        e = sharedPreferences.edit();'


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_PatHist = intent.getStringExtra("edit_PatHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            scrollPos=intent.getIntExtra("scrollPos", 0);


            if (edit_PatHist == null)
                new_result = getPastMedicalVisitData();
        }

        boolean past = sessionManager.isReturning();
        imageList = new ArrayList<>();
        if (past && edit_PatHist == null) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
            alertdialog.setTitle(getString(R.string.question_update_details));
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);
            //AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this,R.style.AlertDialogStyle);

            View layoutInflater = LayoutInflater.from(PastMedicalHistoryActivity.this)
                    .inflate(R.layout.past_fam_hist_previous_details, null);
            alertdialog.setView(layoutInflater);
            TextView textView = layoutInflater.findViewById(R.id.textview_details);
            Log.v(TAG, new_result);
            textView.setText(Html.fromHtml(new_result));


//            alertdialog.setMessage(getString(R.string.question_update_details));
            alertdialog.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // allow to edit
                    flag = true;
                }
            });
            alertdialog.setNegativeButton(getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String[] columns = {"value", " conceptuuid"};
                    try {
                        String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                        Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
                        medHistCursor.moveToLast();
                        phistory = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                        medHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        phistory = ""; // if medical history does not exist
                    }

                    // skip
                    flag = false;
                    if (phistory != null && !phistory.isEmpty() && !phistory.equals("null")) {
                        insertDb(phistory);
                    }

                    Intent intent = new Intent(PastMedicalHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    //    intent.putStringArrayListExtra("exams", physicalExams);
                    startActivity(intent);

                }
            });
            AlertDialog alertDialog = alertdialog.create();
            alertDialog.show();

            Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);


            Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
            nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

        }


        setTitle(getString(R.string.title_activity_patient_history));
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_medical_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        pastMedical_recyclerView = findViewById(R.id.pastMedical_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pastMedical_recyclerView.setLayoutManager(linearLayoutManager);
        pastMedical_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(pastMedical_recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }

        });


//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                patientHistoryMap = new Node(currentFile); //Load the patient history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the patient history mind map
        }

       /* historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);*/


        mgender = PatientsDAO.fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            patientHistoryMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            patientHistoryMap.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        patientHistoryMap.fetchAge(float_ageYear_Month);

        if (intentTag.equals("edit") || intentTag.equals("return")){
            setScreen();
        }
        adapter = new QuestionsAdapter(this, patientHistoryMap, pastMedical_recyclerView, this.getClass().getSimpleName(), this, false, imageList);
        pastMedical_recyclerView.setAdapter(adapter);

        recyclerViewIndicator.attachToRecyclerView(pastMedical_recyclerView);
        pastMedical_recyclerView.scrollToPosition(scrollPos);
        getMenuHeaders();



       /* historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onListClick(v, groupPosition, childPosition);
                return false;
            }
        });

        //Same fix as before, close all other groups when something is clicked.
        historyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    historyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/
    }


    private void onListClick(View v, int groupPosition, int childPosition, String type) {
        Node parentNode = patientHistoryMap.getOption(groupPosition);
        Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);

        if (!parentNode.getChoiceType().equals("single")
                || (parentNode.getChoiceType().equals("single") && !parentNode.anySubSelected())
                || (parentNode.getChoiceType().equals("single") && type == "right" && !parentNode.anySubRightSelected())
                || (parentNode.getChoiceType().equals("single") && type == "left" && ! parentNode.anySubLeftSelected())) {

            Log.d("listClick", "enter");
            clickedNode.toggleSelected();
            //Nodes and the expandable list act funny, so if anything is clicked, a lot of stuff needs to be updated.
            if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
                Log.d("subSelected", "enter");
                patientHistoryMap.getOption(groupPosition).setSelected(true);
            } else {
                patientHistoryMap.getOption(groupPosition).setUnselected();
            }

            if (parentNode.isBilateral()) {
                if (type == "right") {
                    clickedNode.toggleRightSelected();
                    if (patientHistoryMap.getOption(groupPosition).anySubRightSelected()) {
                        patientHistoryMap.getOption(groupPosition).setRightSelected(true);
                    } else {
                        patientHistoryMap.getOption(groupPosition).setRightUnselected();
                    }
                }
                if (type == "left") {
                    clickedNode.toggleLeftSelected();
                    if (patientHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                        patientHistoryMap.getOption(groupPosition).setLeftSelected(true);
                    } else {
                        patientHistoryMap.getOption(groupPosition).setLeftUnselected();
                    }
                }

                if (type=="both"){
                    if ((clickedNode.isRightSelected() && clickedNode.isLeftSelected()) || (!clickedNode.isRightSelected() && !clickedNode.isLeftSelected())){
                        clickedNode.toggleLeftSelected();
                        if (patientHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                            patientHistoryMap.getOption(groupPosition).setLeftSelected(true);
                        } else {
                            patientHistoryMap.getOption(groupPosition).setLeftUnselected();
                        }
                        clickedNode.toggleRightSelected();
                        if (patientHistoryMap.getOption(groupPosition).anySubRightSelected()) {
                            patientHistoryMap.getOption(groupPosition).setRightSelected(true);
                        } else {
                            patientHistoryMap.getOption(groupPosition).setRightUnselected();
                        }
                    }
                    else if (clickedNode.isRightSelected()){
                        clickedNode.toggleLeftSelected();
                        if (patientHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                            patientHistoryMap.getOption(groupPosition).setLeftSelected(true);
                        } else {
                            patientHistoryMap.getOption(groupPosition).setLeftUnselected();
                        }
                    }else if(clickedNode.isLeftSelected()){
                        clickedNode.toggleRightSelected();
                        if (patientHistoryMap.getOption(groupPosition).anySubRightSelected()) {
                            patientHistoryMap.getOption(groupPosition).setRightSelected(true);
                        } else {
                            patientHistoryMap.getOption(groupPosition).setRightUnselected();
                        }
                    }
                }
            }

            //Toggle main is Selected
            if(parentNode.anySubRightSelected() || parentNode.anySubLeftSelected()){
                clickedNode.setSelected(true);
                parentNode.setSelected(true);
            }

            if(parentNode.isBilateral() && !parentNode.anySubRightSelected() && !parentNode.anySubLeftSelected()){
                parentNode.setUnselected();
                clickedNode.setUnselected();
            }


            if (clickedNode.getInputType() != null) {
                if (!clickedNode.getInputType().equals("camera")) {
                    imageName = UUID.randomUUID().toString();
                    Node.handleQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, null, null);
                }
            }

            Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
            if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
                imageName = UUID.randomUUID().toString();
                Node.subLevelQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
            }

            adapter.notifyDataSetChanged();

        }else if (parentNode.getChoiceType().equals("single")
                && parentNode.anySubSelected()
                && !parentNode.isBilateral()) {
            //check if what is clicked is what's already selected. If so, unselect it.
            if(clickedNode.isSelected()){
                clickedNode.toggleSelected();
                parentNode.setUnselected();
            }
            else {
                //is a second answer was clicked, give an error
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
                //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QuestionNodeActivity.this,R.style.AlertDialogStyle);
                alertDialogBuilder.setMessage(R.string.this_question_only_one_answer);
                alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
            }
        } else {
            if((!clickedNode.isRightSelected() && type=="right") || (!clickedNode.isLeftSelected() && type == "left")  || (!clickedNode.isRightSelected() && !clickedNode.isLeftSelected() && type =="both") || (type =="both" && clickedNode.isRightSelected() && !clickedNode.isLeftSelected() && clickedNode.getOption(groupPosition).anySubLeftSelected()) || (type =="both" && clickedNode.isLeftSelected() && !clickedNode.isRightSelected() && clickedNode.getOption(groupPosition).anySubRightSelected())) { //may need to split into is right selected is left selected
                //is a second answer was clicked, give an error
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
                //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QuestionNodeActivity.this,R.style.AlertDialogStyle);
                alertDialogBuilder.setMessage(R.string.this_question_only_one_answer);
                alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
            }
            else{
                if (type=="right"){
                    clickedNode.toggleRightSelected();
                    parentNode.setRightUnselected();
                }

                if (type=="left") {
                    clickedNode.toggleLeftSelected();
                    parentNode.setLeftUnselected();
                }

                if (type=="both"){
                    if (clickedNode.isRightSelected() && clickedNode.isLeftSelected()){
                        clickedNode.toggleLeftSelected();
                        if (parentNode.anySubLeftSelected()) {
                            parentNode.setLeftSelected(true);
                        } else {
                            parentNode.setLeftUnselected();
                        }
                        clickedNode.toggleRightSelected();
                        if (parentNode.anySubRightSelected()) {
                            parentNode.setRightSelected(true);
                        } else {
                            parentNode.setRightUnselected();
                        }
                    }
                    else if (clickedNode.isRightSelected()){
                        clickedNode.toggleLeftSelected();
                        if (parentNode.anySubLeftSelected()) {
                            parentNode.setLeftSelected(true);
                        } else {
                            parentNode.setLeftUnselected();
                        }
                    }
                    else if(clickedNode.isLeftSelected()){
                        clickedNode.toggleRightSelected();
                        if (parentNode.anySubRightSelected()) {
                            parentNode.setRightSelected(true);
                        } else {
                            parentNode.setRightUnselected();
                        }
                    }

                }

                if(!clickedNode.isRightSelected() && !clickedNode.isLeftSelected()){
                    clickedNode.setUnselected();
                }

                if(!parentNode.anySubRightSelected() && !parentNode.anySubLeftSelected()){
                    parentNode.setUnselected();
                    clickedNode.setUnselected();
                }
            }

        }
        adapter.notifyDataSetChanged();

    }


    private void fabClick() {
        patientHistoryMap.getHistoryConcepts();

        if (patientHistoryMap.anySubSelected()) {
            for (Node node : patientHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String patientString="";
                    patientString = node.generateBilateralLanguage();
                    Log.d("patientString", patientString);
                    String toInsert = node.getLanguage() + " : " + patientString;
                    Log.d("toInsert", toInsert);
                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll("% :", "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (org.apache.commons.lang3.StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + "<br/>";
                    insertionList.add(toInsert);
                }
            }

            if (sessionManager.getCurrentLang().equals("ta")){
                String patHistTamil=patientHistoryMap.generateLanguageTamil();
                patHistTamil = patHistTamil.replace(" - ", " ");
                insertLanguageTamil(patHistTamil);
            }

        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0) {
                insertion = Node.bullet + insertionList.get(i);
            } else {
                insertion = insertion + " " + Node.bullet + insertionList.get(i);
            }
        }

        insertion = insertion.replaceAll("null.", "");

        List<String> imagePathList = patientHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }

        try {
            generateSelected();
        } catch (DAOException e) {
            e.printStackTrace();
        }


        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(insertion);

            Intent intent = new Intent(PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
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

            if (flag == true) {
                // only if OK clicked, collect this new info (old patient)
                if (insertion.length() > 0) {
                    phistory = phistory + insertion;
                } else {
                    phistory = phistory + "";
                }
                insertDb(phistory);
            } else {
                insertDb(insertion); // new details of family history
            }

            flag = false;
            sessionManager.setReturning(false);
            Intent intent = new Intent(PastMedicalHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
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


    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }


    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB));

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                patientHistoryMap.setImagePath(mCurrentPhotoPath);
                Log.i(TAG, mCurrentPhotoPath);
                patientHistoryMap.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if (intentTag.equals("edit")){
            intent = new Intent(
                    PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
        }
        else{
            intent = new Intent(
                    PastMedicalHistoryActivity.this, QuestionNodeActivity.class);
            intentTag = "return";

        }
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
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void fabClickedAtEnd() {
        // patientHistoryMap = node;
        fabClick();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos, String type) {
        onListClick(null, groupPos, childPos, type);
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

    private String getPastMedicalVisitData() {
        String result = "";

        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // String[] columns = {"value"};

        String[] columns = {"value", " conceptuuid"};
        String[] medHistArgs;
        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            if (EncounterAdultInitial_LatestVisit!=null){
                medHistArgs = new String[]{EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
            }
            else{
                medHistArgs = new String[]{encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
            }
            Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            result = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if medical history does not exist
        }

        db.close();

        return result;
    }

    private boolean insertEditDB(String subSelected, String rightSelected, String leftSelected) throws DAOException {
        getEditNodeQueue();
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("visitID", visitUuid);
            contentValues.put("patientID", patientUuid);
            contentValues.put("type", "patHist");
            contentValues.put("questionSubSelected", subSelected);
            contentValues.put("questionRightSelected", rightSelected);
            contentValues.put("questionLeftSelected", leftSelected);
            //contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_edit_node", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
        } finally {
            localdb.endTransaction();

        }
        getEditNodeQueue();
        return isInserted;
    }

    private void updateEditDB(String subSelected, String rightSelected, String leftSelected) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        int updatedCount=0;
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String selection= "visitID=? AND patientID = ? AND type = ?";
        String[] nodeArgs = {visitUuid, patientUuid, "patHist"};


        try {
            contentValues.put("visitID", visitUuid);
            contentValues.put("patientID", patientUuid);
            contentValues.put("type", "patHist");
            contentValues.put("questionSubSelected", subSelected);
            contentValues.put("questionRightSelected", rightSelected);
            contentValues.put("questionLeftSelected", leftSelected);
            //contentValues.put("sync", "false");

            updatedCount = localdb.update("tbl_edit_node", contentValues, selection, nodeArgs);

            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);
        } finally {
            localdb.endTransaction();
        }

        if (updatedCount == 0) {
            try {
                insertEditDB(subSelected, rightSelected, leftSelected);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    public void generateSelected() throws DAOException {
        ArrayList<ArrayList<Integer>> allSelected= new ArrayList<>();
        ArrayList<ArrayList<Integer>> rightSelected = new ArrayList<>();
        ArrayList<ArrayList<Integer>> leftSelected = new ArrayList<>();
        for (int i=0; i< patientHistoryMap.getOptionsList().size(); i++ ){
            allSelected.add(patientHistoryMap.getOption(i).getSubSelected());
            rightSelected.add(patientHistoryMap.getOption(i).getRightSubSelected());
            leftSelected.add(patientHistoryMap.getOption(i).getLeftSubSelected());
        }
        Log.d("AllSelected", String.valueOf(allSelected));
        Log.d("RSelected", String.valueOf(rightSelected));
        Log.d("LSelected", String.valueOf(leftSelected));

        Gson gson = new Gson();
        String inputSub= gson.toJson(allSelected);
        String inputRight = gson.toJson(rightSelected);
        String inputLeft = gson.toJson(leftSelected);

        if(intentTag.equals("edit")){
            updateEditDB(inputSub, inputRight, inputLeft);
        }
        else{
            insertEditDB(inputSub, inputRight, inputLeft);

        }
    }

    private void setScreen() {
        Log.d("setScreen", "enter");
        String allSub = "";
        String rightSub= "";
        String leftSub="";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        String nodeSelection = "visitID=? AND patientID=? AND type=?";
        String[] nodeArgs = {visitUuid, patientUuid, "patHist"};
        String[] columns = {"questionSubSelected", "questionRightSelected", "questionLeftSelected"};
        try{
            Cursor nodeCursor = db.query("tbl_edit_node", columns, nodeSelection, nodeArgs, null, null, null);
            nodeCursor.moveToLast();
            allSub = nodeCursor.getString(nodeCursor.getColumnIndexOrThrow("questionSubSelected"));
            rightSub= nodeCursor.getString(nodeCursor.getColumnIndexOrThrow("questionRightSelected"));
            leftSub= nodeCursor.getString(nodeCursor.getColumnIndexOrThrow("questionLeftSelected"));
            nodeCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {

        }
        db.setTransactionSuccessful();
        db.endTransaction();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ArrayList<Integer>>>() {}.getType();
        ArrayList<ArrayList<Integer>>  allSubSelected = gson.fromJson(allSub, type);
        ArrayList<ArrayList<Integer>>  rightSubSelected = gson.fromJson(rightSub, type);
        ArrayList<ArrayList<Integer>>  leftSubSelected = gson.fromJson(leftSub, type);
        Log.d("allSelectedSet", String.valueOf(allSubSelected) + " " + String.valueOf(rightSubSelected) + " " + String.valueOf(leftSubSelected));

        if(allSubSelected!=null) {
            if (allSubSelected.size() == patientHistoryMap.getOptionsList().size()) {
                for (int i = 0; i < allSubSelected.size(); i++) {
                    if (!patientHistoryMap.getOption(i).isBilateral()) {
                        for (int j = 0; j < allSubSelected.get(i).size(); j++) {
                            patientHistoryMap.getOption(i).getOption(allSubSelected.get(i).get(j)).setSelected(true);
                            if (patientHistoryMap.getOption(i).anySubSelected()) {
                                patientHistoryMap.getOption(i).setSelected(true);
                            }
                        }

                    } else {
                        for (int j = 0; j < rightSubSelected.get(i).size(); j++) {
                            Log.d("arraylistR", String.valueOf(i) + " " + String.valueOf(j));
                            patientHistoryMap.getOption(i).getOption(rightSubSelected.get(i).get(j)).setRightSelected(true);
                            if (patientHistoryMap.getOption(i).anySubRightSelected()) {
                                patientHistoryMap.getOption(i).setRightSelected(true);
                            }

                            patientHistoryMap.getOption(i).getOption(rightSubSelected.get(i).get(j)).setSelected(true);
                            if (patientHistoryMap.getOption(i).anySubSelected()) {
                                patientHistoryMap.getOption(i).setSelected(true);
                            }
                        }
                        for (int j = 0; j < leftSubSelected.get(i).size(); j++) {
                            Log.d("arraylistL", String.valueOf(i) + " " + String.valueOf(j));
                            patientHistoryMap.getOption(i).getOption(leftSubSelected.get(i).get(j)).setLeftSelected(true);
                            if (patientHistoryMap.getOption(i).anySubLeftSelected()) {
                                patientHistoryMap.getOption(i).setLeftSelected(true);
                            }

                            patientHistoryMap.getOption(i).getOption(leftSubSelected.get(i).get(j)).setSelected(true);
                            if (patientHistoryMap.getOption(i).anySubSelected()) {
                                patientHistoryMap.getOption(i).setSelected(true);
                            }
                        }


                    }
                }

            }
        }

    }

    public ArrayList<ArrayList<String>> getEditNodeQueue() throws DAOException {
        //get unsynced images from local storage
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ArrayList<ArrayList<String>> editQueue = new ArrayList<>();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_edit_node", null);
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("patientID")));
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("visitID")));
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("questionSubSelected")));
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("questionRightSelected")));
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("questionLeftSelected")));
                    temp.add(idCursor.getString(idCursor.getColumnIndexOrThrow("type")));
                    editQueue.add(temp);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        Log.d("editqueue", String.valueOf(editQueue));
        return editQueue;
    }

    public void getMenuHeaders(){
        boolean hasLicense = false;
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        String famFileName = "famHist.json";
        //String patFileName = "patHist.json";
        String physFileName = "physExam.json";
        JSONObject patFile, famFile, physFile;
        Node famHistoryMap= null;
        PhysicalExam physExamMap= null;

        ArrayList<String> physExamsTemp = new ArrayList<>();
        ArrayList<String> selectedComplaintsList= new ArrayList<>();

        Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
        physExamsTemp.clear();
        if (selectedExams != null)
            physExamsTemp.addAll(selectedExams);

        Set<String> selectedComplaints = sessionManager.getComplaints(patientUuid);
        selectedComplaintsList.clear();
        if (selectedComplaints != null)
            selectedComplaintsList.addAll(selectedComplaints);

        ArrayList<Node> complaintsNodes = new ArrayList<>();
        JSONObject tempFile = null;
        for (int i = 0; i < selectedComplaintsList.size(); i++) {
            if (hasLicense) {
                try {
                    tempFile = new JSONObject(FileUtils.readFile(selectedComplaintsList.get(i) + ".json", this));
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                String fileLocation = "engines/" + selectedComplaintsList.get(i) + ".json";
                tempFile = FileUtils.encodeJSON(this, fileLocation);
            }
            Node tempNode = new Node(tempFile);
            complaintsNodes.add(tempNode);
        }



        if (hasLicense) {
            try {
                //famFile = new JSONObject(FileUtils.readFileRoot(famFileName, this));
                //famHistoryMap = new Node(famFile); //Load the patient history mind map

                famFile= new JSONObject(FileUtils.readFileRoot(famFileName, this));
                famHistoryMap = new Node(famFile);
                physFile = new JSONObject(FileUtils.readFileRoot(physFileName,this));
                physExamMap= new PhysicalExam(physFile, physExamsTemp);
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            famHistoryMap = new Node(FileUtils.encodeJSON(this, famFileName)); //Load the patient history mind map
            physExamMap = new PhysicalExam(FileUtils.encodeJSON(this, physFileName), physExamsTemp); //Load the patient history mind map
        }

        complaintSize=0;
        for (Node complaint : complaintsNodes){
            for (int i=0; i< complaint.getOptionsList().size(); i++){
                nodeHeaders.add(complaint.getOption(i).getText());
                complaintSize++;
            }
        }

        for (int i = 0; i < famHistoryMap.getOptionsList().size(); i++){
            nodeHeaders.add(famHistoryMap.getOption(i).getText());
        }

        for (int i = 0; i < patientHistoryMap.getOptionsList().size(); i++){
            nodeHeaders.add(patientHistoryMap.getOption(i).getText());
        }

        for (int i = 0; i < physExamMap.getTotalNumberOfExams(); i++){
            Log.d("totExam", String.valueOf(i));
            nodeHeaders.add(physExamMap.getExamNode(i).getText()); //will have to fix for physExam
        }
        Log.d("NodeHeaders", String.valueOf(nodeHeaders));
        patHistSize = patientHistoryMap.getOptionsList().size();
        physExamSize = physExamMap.getTotalNumberOfExams();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        for (int i=0; i < nodeHeaders.size(); i++){
            menu.add(0, Menu.FIRST+i, Menu.NONE, nodeHeaders.get(i));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_node_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId()-1; //subtract 1 because it starts at 1 not 0
        Log.d("menuId", String.valueOf(id));
        if (0 <= id && id < complaintSize){
            Intent intent = new Intent(PastMedicalHistoryActivity.this, QuestionNodeActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("scrollPos", id);
            startActivity(intent);
        }
        else if(complaintSize <= id && id < (complaintSize + patHistSize)){
            Intent intent = new Intent(PastMedicalHistoryActivity.this, PastMedicalHistoryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("scrollPos", id-complaintSize);
            startActivity(intent);
        }
        else if((complaintSize + patHistSize) <= id && id < (complaintSize + patHistSize + physExamSize)){
            Intent intent = new Intent(PastMedicalHistoryActivity.this, PhysicalExamActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("scrollPos", id-complaintSize-patHistSize);
            startActivity(intent);
        }
        return true;
    }

    private boolean insertLanguageTamil(String patHistTamil) {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("visitID", visitUuid);
            contentValues.put("patientID", patientUuid);
            contentValues.put("type", "patHistTamil");
            contentValues.put("inputString", patHistTamil);
            //contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_tamil_summary", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
        } finally {
            localdb.endTransaction();

        }
        return isInserted;
    }

}

