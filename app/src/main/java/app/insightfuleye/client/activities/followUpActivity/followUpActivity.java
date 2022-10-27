package app.insightfuleye.client.activities.followUpActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import app.insightfuleye.client.BuildConfig;
import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.patientDetailActivity.PatientDetailActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.database.InteleHealthDatabaseHelper;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.VisitsDAO;
import app.insightfuleye.client.models.FollowUpPatientModel;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.dto.VisitDTO;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.UuidDictionary;
import app.insightfuleye.client.utilities.exception.DAOException;

public class followUpActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = followUpActivity.class.getSimpleName();
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    SessionManager sessionManager = null;
    RecyclerView mFollowUpPatientList;
    MaterialAlertDialogBuilder dialogBuilder;
    TextView totalPatients;
    CardView totalPatientCard;
    private MapView mMapView;
    private ArrayList<String> listPatientUUID = new ArrayList<String>();
    List<FollowUpPatientModel> followUpPatientList;
    private HashMap<Marker, String> mHashMap = new HashMap<Marker, String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_up);
        setTitle(getString(R.string.title_activity_follow_up));

        Toolbar toolbar = findViewById(R.id.toolbar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_sort_white_24dp);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mFollowUpPatientList = findViewById(R.id.followUp_patient_recycler_view);
        sessionManager = new SessionManager(this);
        totalPatients=findViewById(R.id.totalPatientsNum_textView_followUp);
        totalPatientCard=findViewById(R.id.totalPatients_followUp);
        mMapView = findViewById(R.id.follow_up_map);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String encounters= getTableAsString(db, "tbl_encounter");
        Log.i("encounter", encounters);
        if (sessionManager.isPullSyncFinished()) {
            followUpPatientList= doQuery();
        }

        getVisits();
        initGoogleMap(savedInstanceState);
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

        if (encounterDTOList.size() > 0) {
            for (int i = 0; i < encounterDTOList.size(); i++) {
                if (encounterDTOList.get(i).getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.ENCOUNTER_FOLLOW_UP)) {
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
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

    private List<FollowUpPatientModel> doQuery() {
        List<FollowUpPatientModel> followUpPatientList = new ArrayList<>();
/*
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
*/
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate,  b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.provider_uuid, c.encounter_type_uuid, d.value, d.person_attribute_type_uuid " +
                "FROM tbl_visit a, tbl_patient b, tbl_encounter c, tbl_patient_attribute d " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.patientuuid = d.patientuuid " +
                "AND c.encounter_type_uuid= '"+ UuidDictionary.ENCOUNTER_FOLLOW_UP + "' "+
                "AND d.person_attribute_type_uuid='f4af0ef3-579c-448a-8157-750283409122' "+
                //"AND a.startdate LIKE '" + currentDate + "T%' " +
                "AND c.visituuid=a.uuid " +
                "GROUP BY a.uuid ORDER BY a.patientuuid ASC";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        followUpPatientList.add(new FollowUpPatientModel(
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
                                cursor.getString(cursor.getColumnIndexOrThrow("provider_uuid")),
                                cursor.getString(cursor.getColumnIndexOrThrow("value")),
                                downloadDiagnoses(cursor.getString(cursor.getColumnIndexOrThrow("uuid")), "right"),
                                downloadDiagnoses(cursor.getString(cursor.getColumnIndexOrThrow("uuid")), "left")
                                )
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

        if (!followUpPatientList.isEmpty()) {
            for (FollowUpPatientModel followUpPatientModel : followUpPatientList)
                Log.i(TAG, followUpPatientModel.getFirst_name() + " " + followUpPatientModel.getLast_name() + followUpPatientModel.getProvider_uuid());

            followUpPatientAdapter mFollowUpPatientAdapter = new followUpPatientAdapter(followUpPatientList, followUpActivity.this, listPatientUUID);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(followUpActivity.this);
            mFollowUpPatientList.setLayoutManager(linearLayoutManager);
           /* mfollowUpPatientList.addItemDecoration(new
                    DividerItemDecoration(TodayPatientActivity.this,
                    DividerItemDecoration.VERTICAL));*/
            mFollowUpPatientList.setAdapter(mFollowUpPatientAdapter);
        }

        int totalVisits=followUpPatientList.size();
        Log.d("Number of visits: ", String.valueOf(totalVisits));
        totalPatients.setText(String.valueOf(totalVisits));
        return followUpPatientList;
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(BuildConfig.MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
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

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ArrayList<Marker> markers= new ArrayList<>();
        for(FollowUpPatientModel pt : followUpPatientList){
            Log.d("location", pt.getLocation());
            if(pt.getLocation()!=null && !pt.getLocation().equals("0.0, 0.0")){
                float lat= Float.parseFloat(pt.getLocation().split(", ")[0]);
                float lon= Float.parseFloat(pt.getLocation().split(", ")[1]);
                Marker marker= map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                        .title(pt.getFirst_name() + " " + pt.getLast_name())
                        .snippet(pt.getRightEyeDiagnosis() + " " + pt.getLeftEyeDiagnosis()));

                mHashMap.put(marker, pt.getPatientuuid());
                markers.add(marker);
            }
        }

        map.setMyLocationEnabled(true); //show your location on map
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng myLatLng = new LatLng(myLocation.getLatitude(),
                myLocation.getLongitude()); //get your position
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition()); //average positions of all markers on map
        }
        builder.include(myLatLng); //average position including your position
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding); //set camera to average position
        map.animateCamera(cu);
        map.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String patientStatus = "returning";
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patientUuid", mHashMap.get(marker));
        intent.putExtra("status", patientStatus);
        intent.putExtra("tag", "");
        String hasPrescription="false";
        for (int i = 0; i < listPatientUUID.size(); i++) {
            if (mHashMap.get(marker).equalsIgnoreCase(listPatientUUID.get(i))){
                hasPrescription="true";
            }
        }
        intent.putExtra("hasPrescription", hasPrescription);
        this.startActivity(intent);
    }

    private String downloadDiagnoses(String visitUuid, String type){
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
        String diagnosis="";
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        if (encounterCursor != null) {
            encounterCursor.close();
        }

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1'";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                if(dbConceptID.equals(UuidDictionary.DIAGNOSIS_RIGHT_EYE) && type.equals("right")){
                    diagnosis=dbValue;
                }
                if(dbConceptID.equals(UuidDictionary.DIAGNOSIS_LEFT_EYE) && type.equals("left")){
                    diagnosis=dbValue;
                }
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
        return diagnosis;

    }

}
