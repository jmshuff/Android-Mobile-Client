package app.insightfuleye.client.activities;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import app.insightfuleye.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.insightfuleye.client.activities.questionNodeActivity.QuestionNodeActivity;
import app.insightfuleye.client.utilities.SessionManager;

public class MenuNodeActivity extends AppCompatActivity {

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

    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    Context context;

/*    public class intentInfo {
        Context context;
        String patientUuid;
        String visitUuid;
        String state;
        String patientName;
        String intentTag;
        String encounterVitals;
        String encounterAdultIntials;
        String EncounterAdultInitial_LatestVisit;


        public intentInfo(Context context, String patientUuid, String visitUuid, String state, String patientName, String intentTag, String encounterVitals, String encounterAdultIntials, String encounterAdultInitial_LatestVisit) {
            this.context = context;
            this.patientUuid = patientUuid;
            this.visitUuid = visitUuid;
            this.state = state;
            this.patientName = patientName;
            this.intentTag = intentTag;
            this.encounterVitals = encounterVitals;
            this.encounterAdultIntials = encounterAdultIntials;
            EncounterAdultInitial_LatestVisit = encounterAdultInitial_LatestVisit;
        }

        public void startComplaint(){
            Intent intent = new Intent(getApplicationContext(), QuestionNodeActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putExtra("scrollPos", 0);
            startActivity(intent);
        }
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_node_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_complaints:
                Intent intent = new Intent(getApplicationContext(), QuestionNodeActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("tag", intentTag);
                intent.putExtra("scrollPos", 0);
                startActivity(intent);

            case R.id.menu_patHist:
                Intent intent1 = new Intent(getApplicationContext(), PastMedicalHistoryActivity.class);
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent1.putExtra("state", state);
                intent1.putExtra("name", patientName);
                intent1.putExtra("tag", intentTag);
                intent1.putExtra("scrollPos", 0);
                startActivity(intent1);

            case R.id.menu_va:
                Intent intent2 = new Intent(getApplicationContext(), PhysicalExamActivity.class);
                intent2.putExtra("patientUuid", patientUuid);
                intent2.putExtra("visitUuid", visitUuid);
                intent2.putExtra("encounterUuidVitals", encounterVitals);
                intent2.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent2.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent2.putExtra("state", state);
                intent2.putExtra("name", patientName);
                intent2.putExtra("tag", intentTag);
                intent2.putExtra("scrollPos", 1);
                startActivity(intent2);
        }


        return true;
    }
}

