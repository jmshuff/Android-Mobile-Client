package app.insightfuleye.client.activities.questionNodeActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import app.insightfuleye.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.models.imageDisplay;
import app.insightfuleye.client.utilities.FileUtils;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;
import app.insightfuleye.client.utilities.pageindicator.ScrollingPagerIndicator;


public class QuestionNodeActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {
    final String TAG = "Question Node Activity";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    String mgender;

    String imageName;
    File filePath;
    Boolean complaintConfirmed = false;
    SessionManager sessionManager = null;
    private float float_ageYear_Month;
    String azureType=null;


    //    Knowledge mKnowledge; //Knowledge engine
    // ExpandableListView questionListView;
    String mFileName = "knowledge.json"; //knowledge engine file
    //    String mFileName = "DemoBrain.json";
    int complaintNumber = 0; //assuming there is at least one complaint, starting complaint number
    HashMap<String, String> complaintDetails; //temporary storage of complaint findings
    ArrayList<String> complaints; //list of complaints going to be used
    List<Node> complaintsNodes; //actual nodes to be used
    ArrayList<String> physicalExams;
    Node currentNode;
    // CustomExpandableListAdapter adapter;
    QuestionsAdapter adapter;
    boolean nodeComplete = false;

    int lastExpandedPosition = -1;
    String insertion = "";
    private SharedPreferences prefs;
    private String encounterVitals;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;

    private List<Node> optionsList = new ArrayList<>();
    Node assoSympNode;
    private JSONObject assoSympObj = new JSONObject();
    private JSONArray assoSympArr = new JSONArray();
    private JSONObject finalAssoSympObj = new JSONObject();
    ScrollingPagerIndicator recyclerViewIndicator;
    ArrayList<imageDisplay> imageList;


    FloatingActionButton fab;
    RecyclerView question_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        filePath = new File(AppConstants.IMAGE_PATH);
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            complaints = intent.getStringArrayListExtra("complaints");
        }
        complaintDetails = new HashMap<>();
        physicalExams = new ArrayList<>();
        complaintsNodes = new ArrayList<>();
        imageList= new ArrayList<>();

        boolean hasLicense = false;
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        JSONObject currentFile = null;
        for (int i = 0; i < complaints.size(); i++) {
            if (hasLicense) {
                try {
                    currentFile = new JSONObject(FileUtils.readFile(complaints.get(i) + ".json", this));
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                String fileLocation = "engines/" + complaints.get(i) + ".json";
                currentFile = FileUtils.encodeJSON(this, fileLocation);
            }
            Node currentNode = new Node(currentFile);
            complaintsNodes.add(currentNode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // questionListView = findViewById(R.id.complaint_question_expandable_list_view);

        fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClick();
            }
        });
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        question_recyclerView = findViewById(R.id.question_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        question_recyclerView.setLayoutManager(linearLayoutManager);

        question_recyclerView.setNestedScrollingEnabled(true);
        question_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(question_recyclerView);

        setupQuestions(complaintNumber);
        //In the event there is more than one complaint, they will be prompted one at a time.

 /*       questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onListClicked(v, groupPosition, childPosition);
                return false;
            }
        });
        //Not a perfect method, but closes all other questions when a new one is clicked.
        //Expandable Lists in Android are broken, so this is a band-aid fix.
        questionListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    questionListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/

    }


    public void onListClicked(View v, int groupPosition, int childPosition, String type) {
        Log.e(TAG, "CLICKED: " + currentNode.getOption(groupPosition).toString());

        //if it's a multi-choice or if nothing is selected, proceed normally
        if ( !currentNode.getOption(groupPosition).getChoiceType().equals("single")
                || (currentNode.getOption(groupPosition).getChoiceType().equals("single") && !currentNode.getOption(groupPosition).anySubSelected())
                || (currentNode.getOption(groupPosition).getChoiceType().equals("single") && type == "right" && !currentNode.getOption(groupPosition).anySubRightSelected())
                || (currentNode.getOption(groupPosition).getChoiceType().equals("single") && type == "left" && !currentNode.getOption(groupPosition).anySubLeftSelected())) {
            Node question = currentNode.getOption(groupPosition).getOption(childPosition);
            if (!currentNode.getOption(groupPosition).isBilateral()){
               question.toggleSelected();
               if (currentNode.getOption(groupPosition).anySubSelected()) {
                   currentNode.getOption(groupPosition).setSelected(true);
               } else {
                   currentNode.getOption(groupPosition).setUnselected();
               }
               //Log.d("CurrentNode", currentNode.getOption(groupPosition).getOption(childPosition).getText());
           }

            if(currentNode.getOption(groupPosition).isBilateral()){
                if(type=="right" || type=="both"){
                    question.toggleRightSelected();
                    if (currentNode.getOption(groupPosition).anySubRightSelected()) {
                        currentNode.getOption(groupPosition).setRightSelected(true);
                    } else {
                        currentNode.getOption(groupPosition).setRightUnselected();
                    }
                }
                if(type=="left" || type=="both"){
                    question.toggleLeftSelected();
                    if (currentNode.getOption(groupPosition).anySubLeftSelected()) {
                        currentNode.getOption(groupPosition).setLeftSelected(true);
                    } else {
                        currentNode.getOption(groupPosition).setLeftUnselected();
                    }
                }

                //Toggle main is Selected
                if(currentNode.getOption(groupPosition).anySubRightSelected() || currentNode.getOption(groupPosition).anySubLeftSelected()){
                    question.setSelected(true);
                    currentNode.getOption(groupPosition).setSelected(true);

                }
                if(!currentNode.getOption(groupPosition).anySubRightSelected() && !currentNode.getOption(groupPosition).anySubLeftSelected()){
                    currentNode.getOption(groupPosition).setUnselected();
                    question.setUnselected();
                }

            }

            if (!question.getInputType().isEmpty() && question.isSelected()) {
                if (question.getInputType().equals("camera")) {
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    imageName = UUID.randomUUID().toString();

                    if (currentNode.getOption(childPosition).getText().toLowerCase().contains("right")){
                        azureType="right";
                    }
                    if (currentNode.getOption(childPosition).getText().toLowerCase().contains("left")){
                        azureType="left";
                    }

                    for (imageDisplay temp : imageList){
                        File file = new File (temp.getImagePath());
                        if (!file.exists()){
                            imageList.remove(temp);
                        }
                    }
                    imageDisplay imageInfo= new imageDisplay(AppConstants.IMAGE_PATH + imageName + ".jpg", groupPosition);
                    imageList.add(imageInfo);

                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                } else {
                    Node.handleQuestion(question, QuestionNodeActivity.this, adapter, null, null);
                }
            }


            if (!question.isTerminal() && question.isSelected()) {
                Node.subLevelQuestion(question, QuestionNodeActivity.this, adapter, filePath.toString(), imageName);
                //If the knowledgeEngine is not terminal, that means there are more questions to be asked for this branch.
            }
            //if it's not bilateral, single choice, and something is already selected
        } else if (currentNode.getOption(groupPosition).getChoiceType().equals("single")
                && currentNode.getOption(groupPosition).anySubSelected()
                && !currentNode.getOption(groupPosition).isBilateral()) {
            Node question = currentNode.getOption(groupPosition).getOption(childPosition);
            //check if what is clicked is what's already selected. If so, unselect it.
            if(question.isSelected()){
                question.toggleSelected();
                currentNode.getOption(groupPosition).setUnselected();
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
            Node question = currentNode.getOption(groupPosition).getOption(childPosition);
            if((!question.isRightSelected() && type=="right") || (!question.isLeftSelected() && type == "left")  || (!question.isRightSelected() && !question.isLeftSelected() && type =="both") || (type =="both" && question.isRightSelected() && !question.isLeftSelected() && currentNode.getOption(groupPosition).anySubLeftSelected()) || (type =="both" && question.isLeftSelected() && !question.isRightSelected() && currentNode.getOption(groupPosition).anySubRightSelected())) { //may need to split into is right selected is left selected
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
                    question.toggleRightSelected();
                    currentNode.getOption(groupPosition).setRightUnselected();
                }

                if (type=="left") {
                    question.toggleLeftSelected();
                    currentNode.getOption(groupPosition).setLeftUnselected();
                }

                if (type=="both"){
                    if (question.isRightSelected() && question.isLeftSelected()){
                        question.toggleLeftSelected();
                        if (currentNode.getOption(groupPosition).anySubLeftSelected()) {
                            currentNode.getOption(groupPosition).setLeftSelected(true);
                        } else {
                            currentNode.getOption(groupPosition).setLeftUnselected();
                        }
                        question.toggleRightSelected();
                        if (currentNode.getOption(groupPosition).anySubRightSelected()) {
                            currentNode.getOption(groupPosition).setRightSelected(true);
                        } else {
                            currentNode.getOption(groupPosition).setRightUnselected();
                        }
                    }
                    else if (question.isRightSelected()){
                        question.toggleLeftSelected();
                        if (currentNode.getOption(groupPosition).anySubLeftSelected()) {
                            currentNode.getOption(groupPosition).setLeftSelected(true);
                        } else {
                            currentNode.getOption(groupPosition).setLeftUnselected();
                        }
                    }
                    else if(question.isLeftSelected()){
                        question.toggleRightSelected();
                        if (currentNode.getOption(groupPosition).anySubRightSelected()) {
                            currentNode.getOption(groupPosition).setRightSelected(true);
                        } else {
                            currentNode.getOption(groupPosition).setRightUnselected();
                        }
                    }

                }

                if(!question.isRightSelected() && !question.isLeftSelected()){
                    question.setUnselected();
                }

                if(!currentNode.getOption(groupPosition).anySubRightSelected() && !currentNode.getOption(groupPosition).anySubLeftSelected()){
                    currentNode.getOption(groupPosition).setUnselected();
                    question.setUnselected();
                }
            }

        }
        //adapter.updateNode(currentNode);
        adapter.notifyDataSetChanged();
    }

    /**
     * Summarizes the information of the current complaint knowledgeEngine.
     * Then has that put into the database, and then checks to see if there are more complaint nodes.
     * If there are more, presents the user with the next set of questions.
     * All exams are also stored into a string, which will be passed through the activities to the Physical Exam Activity.
     */
    private void fabClick() {
        nodeComplete = true;
        complaintConfirmed=true;
        if (!complaintConfirmed) {
            questionsMissing();
        } else {
            List<String> imagePathList = currentNode.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }

            String complaintString = currentNode.generateLanguage();
            currentNode.generateTableResults();

            if (complaintString != null && !complaintString.isEmpty()) {
                //     String complaintFormatted = complaintString.replace("?,", "?:");

                String complaint = currentNode.getText();
                //    complaintDetails.put(complaint, complaintFormatted);

//                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
            } else {
                String complaint = currentNode.getText();
                if (!complaint.equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) {
//                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                }
            }
            ArrayList<String> selectedAssociatedComplaintsList = currentNode.getSelectedAssociations();
            if (selectedAssociatedComplaintsList != null && !selectedAssociatedComplaintsList.isEmpty()) {
                for (String associatedComplaint : selectedAssociatedComplaintsList) {
                    if (!complaints.contains(associatedComplaint)) {
                        complaints.add(associatedComplaint);
                        String fileLocation = "engines/" + associatedComplaint + ".json";
                        JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
                        Node currentNode = new Node(currentFile);
                        complaintsNodes.add(currentNode);
                    }
                }
            }

            ArrayList<String> childNodeSelectedPhysicalExams = currentNode.getPhysicalExamList();
            if (!childNodeSelectedPhysicalExams.isEmpty())
                physicalExams.addAll(childNodeSelectedPhysicalExams); //For Selected child nodes

            ArrayList<String> rootNodePhysicalExams = parseExams(currentNode);
            if (rootNodePhysicalExams != null && !rootNodePhysicalExams.isEmpty())
                physicalExams.addAll(rootNodePhysicalExams); //For Root Node

            if (complaintNumber < complaints.size() - 1) {
                complaintNumber++;
                setupQuestions(complaintNumber);
                complaintConfirmed = false;
            } else if (complaints.size() >= 1 && complaintNumber == complaints.size() - 1 && !optionsList.isEmpty()) {
                complaintNumber++;
                removeDuplicateSymptoms();
                complaintConfirmed = false;
            } else {
                if (intentTag != null && intentTag.equals("edit")) {
                    Log.i(TAG, "fabClick: update" + insertion);
                    updateDatabase(insertion);
                    Intent intent = new Intent(QuestionNodeActivity.this, PhysicalExamActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("tag", intentTag);

                    Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
                    sessionManager.setVisitSummary(patientUuid, selectedExams);

                    startActivity(intent);
                } else {
                    Log.i(TAG, "fabClickInsertion: " + insertion);
                    insertDb(insertion);
                    Intent intent = new Intent
                            (QuestionNodeActivity.this, PastMedicalHistoryActivity.class);
                    intent.putExtra("patientUuid", patientUuid);
                    intent.putExtra("visitUuid", visitUuid);
                    intent.putExtra("encounterUuidVitals", encounterVitals);
                    intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                    intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                    intent.putExtra("state", state);
                    intent.putExtra("name", patientName);
                    intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                    intent.putExtra("tag", intentTag);
                    Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
                    sessionManager.setVisitSummary(patientUuid, selectedExams);

                    startActivity(intent);
                }


            }


        }

        // question_recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        //question_recyclerView.notifyAll();
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);

    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     *
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private boolean insertDb(String value) {

        Log.i(TAG, "insertDb: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue1(value));

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
/*
    private void uploadAzureImage(String filePath,String imageName) {
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

    private void updateDatabase(String string) {
        Log.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
//        }
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));

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

    /**
     * Sets up the complaint knowledgeEngine's questions.
     *
     * @param complaintIndex Index of complaint being displayed to user.
     */
    private void setupQuestions(int complaintIndex) {
        nodeComplete = false;

        if (complaints.size() >= 1) {
            getAssociatedSymptoms(complaintIndex);
        } else {
            currentNode = complaintsNodes.get(complaintIndex);
        }

        mgender = PatientsDAO.fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            currentNode.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            currentNode.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        currentNode.fetchAge(float_ageYear_Month);


        adapter = new QuestionsAdapter(this, currentNode, question_recyclerView, this.getClass().getSimpleName(), this, false, imageList);
        question_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);
      /*  adapter = new CustomExpandableListAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);*/
        setTitle(patientName + ": " + currentNode.findDisplay());

    }

    private void getAssociatedSymptoms(int complaintIndex) {

        List<Node> assoComplaintsNodes = new ArrayList<>();
        assoComplaintsNodes.addAll(complaintsNodes);

        for (int i = 0; i < complaintsNodes.get(complaintIndex).size(); i++) {

            if ((complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("Associated symptoms"))
                    || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("जुड़े लक्षण")) || (complaintsNodes.get(complaintIndex).getOptionsList().get(i).getText()
                    .equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ"))) {

                optionsList.addAll(complaintsNodes.get(complaintIndex).getOptionsList().get(i).getOptionsList());

                assoComplaintsNodes.get(complaintIndex).getOptionsList().remove(i);
                currentNode = assoComplaintsNodes.get(complaintIndex);
                Log.e("CurrentNode", "" + currentNode);

            } else {
                currentNode = complaintsNodes.get(complaintIndex);
            }
        }
    }

    public void setRecyclerViewIndicator() {
        question_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(question_recyclerView);
    }

    private void removeDuplicateSymptoms() {

        nodeComplete = false;

        HashSet<String> hashSet = new HashSet<>();

        List<Node> finalOptionsList = new ArrayList<>(optionsList);

        if (optionsList.size() != 0) {

            for (int i = 0; i < optionsList.size(); i++) {

                if (hashSet.contains(optionsList.get(i).getText())) {

                    finalOptionsList.remove(optionsList.get(i));

                } else {
                    hashSet.add(optionsList.get(i).getText());
                }
            }

            try {
                assoSympObj.put("id", "ID_294177528");
                assoSympObj.put("text", "Associated symptoms");
                assoSympObj.put("display", "Do you have the following symptom(s)?");
                assoSympObj.put("display-hi", "क्या आपको निम्नलिखित लक्षण हैं?");
                assoSympObj.put("display-or", "ତମର ଏହି ଲକ୍ଷଣ ସବୁ ଅଛି କି?");
                assoSympObj.put("pos-condition", "c.");
                assoSympObj.put("neg-condition", "s.");
                assoSympArr.put(0, assoSympObj);
                finalAssoSympObj.put("id", "ID_844006222");
                finalAssoSympObj.put("text", "Associated symptoms");
                finalAssoSympObj.put("display-or", "ପେଟଯନ୍ତ୍ରଣା");
                finalAssoSympObj.put("display-hi", "जुड़े लक्षण");
                finalAssoSympObj.put("perform-physical-exam", "");
                finalAssoSympObj.put("options", assoSympArr);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            assoSympNode = new Node(finalAssoSympObj);
            assoSympNode.getOptionsList().get(0).setOptionsList(finalOptionsList);
            assoSympNode.getOptionsList().get(0).setTerminal(false);

            currentNode = assoSympNode;

            mgender = PatientsDAO.fetch_gender(patientUuid);

            if (mgender.equalsIgnoreCase("M")) {
                currentNode.fetchItem("0");
            } else if (mgender.equalsIgnoreCase("F")) {
                currentNode.fetchItem("1");
            }

            // flaoting value of age is passed to Node for comparison...
            currentNode.fetchAge(float_ageYear_Month);

            adapter = new QuestionsAdapter(this, currentNode, question_recyclerView, this.getClass().getSimpleName(), this, true, imageList);
            question_recyclerView.setAdapter(adapter);
            setTitle(patientName + ": " + currentNode.getText());

        }
    }

    //Dialog Alert forcing user to answer all questions.
    //Can be removed if necessary
    //TODO: Add setting to allow for all questions unrequired..addAll(Arrays.asList(splitExams))
    public void questionsMissing() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0)));
        alertDialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                complaintConfirmed = true;
                dialog.dismiss();
                fabClick();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.generic_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        //alertDialog.show();
    }


    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) {
            String[] splitExams = rawExams.split(";");
            examList.addAll(Arrays.asList(splitExams));
            return examList;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                currentNode.setImagePath(mCurrentPhotoPath);
                currentNode.displayImage(this, filePath.getAbsolutePath(), imageName);
                //uploadAzureImage(filePath.getAbsolutePath(), imageName);
                adapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    public void
    onBackPressed() {
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

    @Override
    public void fabClickedAtEnd() {
        //currentNode = node;
        fabClick();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos, String type) {
        onListClicked(null, groupPos, childPos, type);
    }


}