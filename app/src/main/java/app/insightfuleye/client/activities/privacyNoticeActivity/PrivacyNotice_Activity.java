package app.insightfuleye.client.activities.privacyNoticeActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.identificationActivity.IdentificationActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.utilities.FileUtils;
import app.insightfuleye.client.utilities.SessionManager;

public class PrivacyNotice_Activity extends AppCompatActivity implements View.OnClickListener {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    Button accept, reject;
    MaterialCheckBox checkBox_cho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_2);
        setTitle(getString(R.string.privacy_notice_title));

        /*
         * Toolbar which displays back arrow on action bar
         * Add the below lines for every activity*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        sessionManager = new SessionManager(this);
        privacy_textview = findViewById(R.id.privacy_text);
        privacy_textview.setAutoLinkMask(Linkify.ALL);
        accept = findViewById(R.id.button_accept);
        reject = findViewById(R.id.button_reject);
        checkBox_cho = findViewById(R.id.checkbox_CHO);


        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    obj = new JSONObject(Objects.requireNonNullElse(
                            FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                            String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
                }

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

//            SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
//            if(sharedPreferences.getAll().values().contains("cb"))
            Locale current = getResources().getConfiguration().locale;
//            if (current.toString().equals("cb")) {
//                String privacy_string = obj.getString("privacyNoticeText_Cebuano");
//                if (privacy_string.isEmpty()) {
//                    privacy_string = obj.getString("privacyNoticeText");
//                    privacy_textview.setText(privacy_string);
//                } else {
//                    privacy_textview.setText(privacy_string);
//                }
//
//            } else if (current.toString().equals("or")) {
//                String privacy_string = obj.getString("privacyNoticeText_Odiya");
//                if (privacy_string.isEmpty()) {
//                    privacy_string = obj.getString("privacyNoticeText");
//                    privacy_textview.setText(privacy_string);
//                } else {
//                    privacy_textview.setText(privacy_string);
//                }
//
//            } else
            if (current.toString().equals("hi")) {
                String privacy_string = obj.getString("privacyNoticeText_Hindi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else if(current.toString().equals("ta")){
                String privacy_string = obj.getString("privacyNoticeText_Tamil");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }
            }
            else {
                String privacy_string = obj.getString("privacyNoticeText");
                privacy_textview.setText(privacy_string);
            }

            accept.setOnClickListener(this);
            reject.setOnClickListener(this);


//            txt_next.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    int selected_radio = radiogrp.getCheckedRadioButtonId();
//                    radiobtn = findViewById(selected_radio);
//
//
//                    if (checkBox_cho.isChecked() & (radio_acc.isChecked() || radio_rej.isChecked()))
//                    {
//                        if(radio_acc.isChecked())
//                        {
//                            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
//                            intent.putExtra("privacy",radiobtn.getText()); //privacy value send to identificationActivity
//                            Log.d("Privacy", "selected radio: "+radiobtn.getText().toString());
//                            startActivity(intent);
//                        }
//                        else
//                        {
//                            Toast.makeText(PrivacyNotice_Activity.this, getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }
//                    else if((radio_acc.isChecked() || radio_rej.isChecked()) & !checkBox_cho.isChecked())
//                    {
//
//                        radiogrp.clearCheck();
//                        Toast.makeText(PrivacyNotice_Activity.this, "Please read out the Privacy Consent first.", Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        Toast.makeText(getApplicationContext(),getString(R.string.privacy_toast), Toast.LENGTH_SHORT).show();
//                    }
//
//
//                }
//            });


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (checkBox_cho.isChecked() && v.getId() == R.id.button_accept) {
            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
            intent.putExtra("privacy", accept.getText().toString()); //privacy value send to identificationActivity
            Log.d("Privacy", "selected radio: " + accept.getText().toString());
            startActivity(intent);
        } else if (checkBox_cho.isChecked() && v.getId() == R.id.button_reject) {
            Toast.makeText(PrivacyNotice_Activity.this,
                    getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(PrivacyNotice_Activity.this,
                    R.string.please_read_out_consent, Toast.LENGTH_SHORT).show();
        }

//        if(v.getId() == R.id.button_accept)
//            Toast.makeText(this, "Accept", Toast.LENGTH_SHORT).show();
//        else if(v.getId() == R.id.button_reject)
//            Toast.makeText(this, "Reject", Toast.LENGTH_SHORT).show();
    }
}
