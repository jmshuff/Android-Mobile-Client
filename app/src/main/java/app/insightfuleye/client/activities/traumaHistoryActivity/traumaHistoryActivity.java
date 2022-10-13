package app.insightfuleye.client.activities.traumaHistoryActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.utilities.SessionManager;

public class traumaHistoryActivity extends AppCompatActivity {
    SessionManager sessionManager=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trauma_history);
        setTitle("Trauma History");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
