package app.insightfuleye.client.activities.familyHistoryActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import app.insightfuleye.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.insightfuleye.client.activities.questionNodeActivity.QuestionsAdapter;
import app.insightfuleye.client.activities.visitSummaryActivity.VisitSummaryActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.models.imageDisplay;
import app.insightfuleye.client.utilities.FileUtils;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;
import app.insightfuleye.client.utilities.pageindicator.ScrollingPagerIndicator;

public class FamilyHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    private float float_ageYear_Month;

    ArrayList<String> physicalExams;
    String mFileName = "famHist.json";
    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    //CustomExpandableListAdapter adapter;
    // ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "", phistory = "", fhistory = "";
    boolean flag = false;
    boolean hasLicense = false;
    SharedPreferences.Editor e;
    SQLiteDatabase localdb, db;
    SessionManager sessionManager;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    private String imageName = null;
    private File filePath;
    ScrollingPagerIndicator recyclerViewIndicator;

    RecyclerView family_history_recyclerView;
    QuestionsAdapter adapter;
    String edit_FamHist = "";
    String new_result;
    ArrayList<imageDisplay> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
        imageList= new ArrayList<>();

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_FamHist = intent.getStringExtra("edit_FamHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            patientName = intent.getStringExtra("name");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");

            if(edit_FamHist == null)
                new_result = getFamilyHistoryVisitData();
        }

        boolean past = sessionManager.isReturning();
        if (past && edit_FamHist == null) {
            MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
            alertdialog.setTitle(getString(R.string.question_update_details));
            //AlertDialog.Builder alertdialog = new AlertDialog.Builder(FamilyHistoryActivity.this,R.style.AlertDialogStyle);
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);

            View layoutInflater = LayoutInflater.from(FamilyHistoryActivity.this)
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
                    // skip
                    flag = false;

                    String[] columns = {"value", " conceptuuid"};

                    try {
                        String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                        String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                        Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
                        famHistCursor.moveToLast();
                        fhistory = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                        famHistCursor.close();
                    } catch (CursorIndexOutOfBoundsException e) {
                        fhistory = ""; // if family history does not exist
                    }

                    if (fhistory != null && !fhistory.isEmpty() && !fhistory.equals("null")) {
                        insertDb(fhistory);
                    }

                    Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);

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

            
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        setTitle(R.string.title_activity_family_history);
        recyclerViewIndicator=findViewById(R.id.recyclerViewIndicator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        family_history_recyclerView = findViewById(R.id.family_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        family_history_recyclerView.setLayoutManager(linearLayoutManager);
        family_history_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(family_history_recyclerView);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();
            }
        });

//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                familyHistoryMap = new Node(currentFile); //Load the family history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            familyHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the family history mind map
        }

        //  familyListView = findViewById(R.id.family_history_expandable_list_view);

        if (intentTag.equals("edit")){
            setScreen();
        }
        adapter = new QuestionsAdapter(this, familyHistoryMap, family_history_recyclerView, this.getClass().getSimpleName(), this, false, imageList);
        family_history_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(family_history_recyclerView);
        /*adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);*/

        /*familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });*/
    }

    private String getFamilyHistoryVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            result = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if family history does not exist
        }

        db.close();
        return result;
    }

    private void onListClick(View v, int groupPosition, int childPosition, String type) {
        Node parentNode= familyHistoryMap.getOption(groupPosition);
        Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
        if ( !parentNode.getChoiceType().equals("single")
                || (parentNode.getChoiceType().equals("single") && !parentNode.anySubSelected())
                || (parentNode.getChoiceType().equals("single") && type == "right" && !parentNode.anySubRightSelected())
                || (parentNode.getChoiceType().equals("single") && type == "left" && ! parentNode.anySubLeftSelected())) {
            if (!parentNode.isBilateral()) {
                Log.i(TAG, "onChildClick: " + clickedNode.toString());
                clickedNode.toggleSelected();
                if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
                    familyHistoryMap.getOption(groupPosition).setSelected(true);
                } else {
                    familyHistoryMap.getOption(groupPosition).setUnselected();
                }
            }
            if (parentNode.isBilateral()) {
                if (type == "right" || type == "both") {
                    clickedNode.toggleRightSelected();
                    if (familyHistoryMap.getOption(groupPosition).anySubRightSelected()) {
                        familyHistoryMap.getOption(groupPosition).setRightSelected(true);
                    } else {
                        familyHistoryMap.getOption(groupPosition).setRightUnselected();
                    }
                }
                if (type == "left" || type == "both") {
                    clickedNode.toggleLeftSelected();
                    if (familyHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                        familyHistoryMap.getOption(groupPosition).setLeftSelected(true);
                    } else {
                        familyHistoryMap.getOption(groupPosition).setLeftUnselected();
                    }
                }
                if (familyHistoryMap.getOption(groupPosition).anySubRightSelected() || familyHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                    familyHistoryMap.getOption(groupPosition).setSelected(true);
                }
                if (familyHistoryMap.getOption(groupPosition).isBilateral() && !familyHistoryMap.getOption(groupPosition).anySubRightSelected() && !familyHistoryMap.getOption(groupPosition).anySubLeftSelected()) {
                    familyHistoryMap.getOption(groupPosition).setUnselected();
                }
            }

            if (clickedNode.getInputType() != null) {
                if (!clickedNode.getInputType().equals("camera")) {
                    Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
                }
            }
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
                Log.i("RES>", "" + filePath + " -> " + res);
            }

            imageName = UUID.randomUUID().toString();

            if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                    familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
                Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
            }
        } else if (parentNode.getChoiceType().equals("single")
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

    private void onFabClick() {
        if (familyHistoryMap.anySubSelected()) {
            for (Node node : familyHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = node.generateBilateralLanguage();
                    String toInsert = node.getLanguage() + familyString;

                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll("% : ", "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    insertionList.add(toInsert);
                }
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

        List<String> imagePathList = familyHistoryMap.getImagePathList();

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

            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
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
                    fhistory = fhistory + insertion;
                } else {
                    fhistory = fhistory + "";
                }
                insertDb(fhistory);
            } else {
                insertDb(insertion); // new details of family history
            }

            flag = false;
            sessionManager.setReturning(false);
            Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
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

    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(app.insightfuleye.client.utilities.StringUtils.getValue(value));
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

    private void updateDatabase(String string) {

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB));

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
    public void onBackPressed() {
        Intent intent = new Intent(
                FamilyHistoryActivity.this, PastMedicalHistoryActivity.class);
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

    @Override
    public void fabClickedAtEnd() {
        onFabClick();

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

    private boolean insertEditDB(String subSelected, String rightSelected, String leftSelected) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("visitID", visitUuid);
            contentValues.put("patientID", patientUuid);
            contentValues.put("type", "famHist");
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
        return isInserted;
    }

    private void updateEditDB(String subSelected, String rightSelected, String leftSelected) throws DAOException {
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        int updatedCount=0;
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String selection= "visitID=? AND patientID = ? AND type = ?";
        String[] nodeArgs = {visitUuid, patientUuid, "famHist"};


        try {
            contentValues.put("visitID", visitUuid);
            contentValues.put("patientID", patientUuid);
            contentValues.put("type", "famHist");
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
        for (int i=0; i< familyHistoryMap.getOptionsList().size(); i++ ){
            allSelected.add(familyHistoryMap.getOption(i).getSubSelected());
            rightSelected.add(familyHistoryMap.getOption(i).getRightSubSelected());
            leftSelected.add(familyHistoryMap.getOption(i).getLeftSubSelected());
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
        String[] nodeArgs = {visitUuid, patientUuid, "famHist"};
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
            if (allSubSelected.size() == familyHistoryMap.getOptionsList().size()) {
                for (int i = 0; i < allSubSelected.size(); i++) {
                    if (!familyHistoryMap.getOption(i).isBilateral()) {
                        for (int j = 0; j < allSubSelected.get(i).size(); j++) {
                            familyHistoryMap.getOption(i).getOption(allSubSelected.get(i).get(j)).setSelected(true);
                            if (familyHistoryMap.getOption(i).anySubSelected()) {
                                familyHistoryMap.getOption(i).setSelected(true);
                            }
                        }

                    } else {
                        for (int j = 0; j < rightSubSelected.get(i).size(); j++) {
                            Log.d("arraylistR", String.valueOf(i) + " " + String.valueOf(j));
                            familyHistoryMap.getOption(i).getOption(rightSubSelected.get(i).get(j)).setRightSelected(true);
                            if (familyHistoryMap.getOption(i).anySubRightSelected()) {
                                familyHistoryMap.getOption(i).setRightSelected(true);
                            }

                            familyHistoryMap.getOption(i).getOption(rightSubSelected.get(i).get(j)).setSelected(true);
                            if (familyHistoryMap.getOption(i).anySubSelected()) {
                                familyHistoryMap.getOption(i).setSelected(true);
                            }
                        }
                        for (int j = 0; j < leftSubSelected.get(i).size(); j++) {
                            Log.d("arraylistL", String.valueOf(i) + " " + String.valueOf(j));
                            familyHistoryMap.getOption(i).getOption(leftSubSelected.get(i).get(j)).setLeftSelected(true);
                            if (familyHistoryMap.getOption(i).anySubLeftSelected()) {
                                familyHistoryMap.getOption(i).setLeftSelected(true);
                            }

                            familyHistoryMap.getOption(i).getOption(leftSubSelected.get(i).get(j)).setSelected(true);
                            if (familyHistoryMap.getOption(i).anySubSelected()) {
                                familyHistoryMap.getOption(i).setSelected(true);
                            }
                        }


                    }
                }

            }
        }

    }
}



