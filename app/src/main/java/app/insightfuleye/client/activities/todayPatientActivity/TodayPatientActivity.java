package app.insightfuleye.client.activities.todayPatientActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.Lists;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.InteleHealthDatabaseHelper;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ProviderDAO;
import app.insightfuleye.client.database.dao.VisitsDAO;
import app.insightfuleye.client.models.TodayPatientModel;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.dto.VisitDTO;
import app.insightfuleye.client.models.volunteerTotalModel;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.exception.DAOException;

public class TodayPatientActivity extends AppCompatActivity {
    private static final String TAG = TodayPatientActivity.class.getSimpleName();
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    RecyclerView mTodayPatientList;
   MaterialAlertDialogBuilder dialogBuilder;
   TextView totalPatients;
   CardView totalPatientCard;
   private RecyclerView volunteerRecyclerView;

    private ArrayList<String> listPatientUUID = new ArrayList<String>();
    private ArrayList<volunteerTotalModel> volunteerList=new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_patient);
        setTitle(getString(R.string.title_activity_today_patient));
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_today_patient);

        Toolbar toolbar = findViewById(R.id.toolbar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);
//        toolbar.setOverflowIcon(drawable);


        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mTodayPatientList = findViewById(R.id.today_patient_recycler_view);
        volunteerRecyclerView=findViewById(R.id.volunteer_total_recycler_view);
        volunteerRecyclerView.setVisibility(View.GONE);
        sessionManager = new SessionManager(this);
        totalPatients=findViewById(R.id.totalPatientsNum_textView);
        totalPatientCard=findViewById(R.id.totalPatients);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        if (sessionManager.isPullSyncFinished()) {
            doQuery();
        }

        getVisits();
        setVolunteerAdapter();

        totalPatientCard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(volunteerRecyclerView.getVisibility()==View.VISIBLE){
                    volunteerRecyclerView.setVisibility(View.GONE);
                }
                else{
                    volunteerRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getVisits() {

        ArrayList<String> encounterVisitUUID = new ArrayList<String>();
        HashSet<String> hsPatientUUID = new HashSet<String>();

        //Get all Visits
        VisitsDAO visitsDAO = new VisitsDAO();
        List<VisitDTO> visitsDTOList = visitsDAO.getAllVisits();

        //Get all Encounters
        EncounterDAO encounterDAO = new EncounterDAO();
        List<EncounterDTO> encounterDTOList = encounterDAO.getAllEncounters();

        //Get Visit Complete Encounters only, visit complete encounter id - bd1fbfaa-f5fb-4ebd-b75c-564506fc309e
        if (encounterDTOList.size() > 0) {
            for (int i = 0; i < encounterDTOList.size(); i++) {
                if (encounterDTOList.get(i).getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                    encounterVisitUUID.add(encounterDTOList.get(i).getVisituuid());
                }
            }
        }


        //Get patientUUID from visitList
        for (int i = 0; i < encounterVisitUUID.size(); i++) {

            for (int j = 0; j < visitsDTOList.size(); j++) {

                if (encounterVisitUUID.get(i).equalsIgnoreCase(visitsDTOList.get(j).getUuid())) {
                    listPatientUUID.add(visitsDTOList.get(j).getPatientuuid());
                }
            }
        }

        if (listPatientUUID.size() > 0) {

            hsPatientUUID.addAll(listPatientUUID);
            listPatientUUID.clear();
            listPatientUUID.addAll(hsPatientUUID);

        }
    }


    private void doQuery() {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, c.provider_uuid,  b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter c " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.startdate LIKE '" + currentDate + "T%'   " +
                "AND c.visituuid=a.uuid " +
                "GROUP BY a.uuid ORDER BY a.patientuuid ASC";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        todayPatientList.add(new TodayPatientModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                cursor.getString(cursor.getColumnIndexOrThrow("provider_uuid")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (!todayPatientList.isEmpty()) {
            for (TodayPatientModel todayPatientModel : todayPatientList)
                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name() + todayPatientModel.getProvider_uuid());

            TodayPatientAdapter mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
            mTodayPatientList.setLayoutManager(linearLayoutManager);
           /* mTodayPatientList.addItemDecoration(new
                    DividerItemDecoration(TodayPatientActivity.this,
                    DividerItemDecoration.VERTICAL));*/
            mTodayPatientList.setAdapter(mTodayPatientAdapter);
        }

        int totalVisits=todayPatientList.size();
        Log.d("Number of visits: ", String.valueOf(totalVisits));
        totalPatients.setText(String.valueOf(totalVisits));


        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        Log.d("creator names", Arrays.toString(creator_uuid));


        List<String> uniqueVolunteers = new ArrayList<>();
        List<Integer> volunteerCounts = new ArrayList<>();
        for (TodayPatientModel todayPatientModel : todayPatientList){
            if (!uniqueVolunteers.contains(todayPatientModel.getProvider_uuid())) {
                uniqueVolunteers.add(todayPatientModel.getProvider_uuid());
                volunteerCounts.add(1);
            }
            else{
                int i= findIndex(uniqueVolunteers, String.valueOf(todayPatientModel.getProvider_uuid()));
                volunteerCounts.set(i, volunteerCounts.get(i) + 1);
            }
        }
        Log.i(TAG, String.valueOf(uniqueVolunteers));
        Log.i(TAG, String.valueOf(volunteerCounts));
        int k=0;
        for (String volunteer : uniqueVolunteers){

            int i= 0;
            while (!creator_uuid[i].equals(volunteer)){
                i++;
            }
            uniqueVolunteers.set(k, creator_names[i]);
            k++;
        }
        Log.i(TAG, String.valueOf(uniqueVolunteers));
        Log.i(TAG, String.valueOf(volunteerCounts));

        //add to model
        int j=0;
        while (j < uniqueVolunteers.size()){
            volunteerList.add(new volunteerTotalModel(uniqueVolunteers.get(j), volunteerCounts.get(j)));
            j++;
        }
        Log.d("length", String.valueOf(volunteerList.size()));
    }

    private void setVolunteerAdapter(){
        recyclerVolunteerAdapter adapter= new recyclerVolunteerAdapter(volunteerList);
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(getApplicationContext());
        volunteerRecyclerView.setLayoutManager(layoutManager);
        volunteerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        volunteerRecyclerView.setAdapter(adapter);

    }
    public static int findIndex(List<String> arr, String t)
    {
        // if array is Null
        if (arr == null) {
            return 0;
        }

        // find length of array
        int len = arr.size();
        int i = 0;

        // traverse in the array
        while (i < len) {
            Log.d("match", arr.get(i) + " " + t);
            // if the i-th element is t
            // then return the index
            if (arr.get(i).equals(t)) {
                Log.d("matched", "yes");
                return i;
            }
            else {
                i = i + 1;
            }
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.summary_endAllVisit:
                endAllVisit();

            case R.id.action_filter:
                //alert box.
                displaySingleSelectionDialog();    //function call


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void displaySingleSelectionDialog() {
        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
//        boolean[] checkedItems = {false, false, false, false};
        // ngo_numbers = getResources().getStringArray(R.array.ngo_numbers);
        dialogBuilder = new MaterialAlertDialogBuilder(TodayPatientActivity.this);
        dialogBuilder.setTitle(R.string.filter_by_creator);

        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;
        dialogBuilder.setMultiChoiceItems(creator_names, null, new DialogInterface.OnMultiChoiceClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    selectedItems.add(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    selectedItems.remove(finalCreator_uuid[which]);
                    Logger.logD(TAG, finalCreator_names[which] + finalCreator_uuid[which]);
                }
            }
        });

        dialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
                doQueryWithProviders(selectedItems);
            }
        });

        dialogBuilder.setNegativeButton(R.string.generic_cancel, null);
//        dialogBuilder.show();

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
    }

    private void doQueryWithProviders(List<String> providersuuids) {
        List<TodayPatientModel> todayPatientList = new ArrayList<>();
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        String query = "SELECT  distinct a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter c " +
                "WHERE a.patientuuid = b.uuid " +
                "AND c.visituuid=a.uuid and c.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                "AND a.startdate LIKE '" + currentDate + "T%'" +
                "ORDER BY a.patientuuid ASC ";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        todayPatientList.add(new TodayPatientModel(
                                cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                                cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")))),
                                cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                cursor.getString(cursor.getColumnIndexOrThrow("provider_uuid")))
                        );
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (!todayPatientList.isEmpty()) {
            for (TodayPatientModel todayPatientModel : todayPatientList)
                Log.i(TAG, todayPatientModel.getFirst_name() + " " + todayPatientModel.getLast_name());

            TodayPatientAdapter mTodayPatientAdapter = new TodayPatientAdapter(todayPatientList, TodayPatientActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TodayPatientActivity.this);
            mTodayPatientList.setLayoutManager(linearLayoutManager);
            mTodayPatientList.addItemDecoration(new
                    DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            mTodayPatientList.setAdapter(mTodayPatientAdapter);
        }

    }


    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean result = endVisit(
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    );
                    if (!result) failedUploads++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(getString(R.string.unable_to_end) + failedUploads +
                    getString(R.string.upload_before_end_visit_active));
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);
        }

    }

    private boolean endVisit(String patientID, String patientName, String visitUUID) {

        return visitUUID != null;

    }

    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return phone;
    }
}




