package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.PhysicalExam;
import edu.jhu.bme.cbid.healthassistantsclient.objects.TableExam;

public class TableExamActivity extends AppCompatActivity {

    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mSpo2;
    Long obsID;
    final String LOG_TAG = "TableExamActivity";
    private Long patientID;
    private ArrayList<String> physExams;

    private InsertTableExamDb mTask = null;


    // EditText bmi = (EditText) findViewById(R.id.table_bmi);
    // bmi.setFocusable(false);
    // TODO: intent passes along patient id, gender
    // TODO: autocalculation of bmi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_exam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeight = (EditText) findViewById(R.id.table_height);
        mWeight = (EditText) findViewById(R.id.table_weight);
        mPulse = (EditText) findViewById(R.id.table_pulse);
        mBpSys = (EditText) findViewById(R.id.table_bpsys);
        mBpDia = (EditText) findViewById(R.id.table_bpdia);
        mTemperature = (EditText) findViewById(R.id.table_temp);
        mSpo2 = (EditText) findViewById(R.id.table_spo2);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 0);
            physExams = intent.getStringArrayListExtra("exams");
            Log.v(LOG_TAG, patientID + "");
        }

        mSpo2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.table_spo2 || id == EditorInfo.IME_NULL) {
                    validateTable();
                    return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTable();
            }
        });
    }

    public void validateTable() {
        if (mTask != null) {
            return;
        }

        // Reset errors.


    // .getText().toString()
        boolean cancel = false;
        View focusView = null;

        // TODO: bmi, patient id should go here
        // Store values at the time of the login attempt.
        ArrayList<EditText> values = new ArrayList<EditText>();
        values.add(mHeight);
        values.add(mWeight);
        values.add(mPulse);
        values.add(mBpSys);
        values.add(mBpDia);
        values.add(mTemperature);
        values.add(mSpo2);

        // Check for a valid values.
        for(int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);

            if (TextUtils.isEmpty(et.getText().toString())) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                cancel = true;
                break;
            }
        }


        if (cancel) {
            // There was an error - focus the first form field with an error.
            focusView.requestFocus();
        } else {
            TableExam results = new TableExam();
            try {
                results.setHeight(Double.parseDouble(mHeight.getText().toString()));
                results.setWeight(Double.parseDouble(mWeight.getText().toString()));
                results.setPulse(Double.parseDouble(mPulse.getText().toString()));
                results.setBpsys(Double.parseDouble(mBpSys.getText().toString()));
                results.setBpdia(Double.parseDouble(mBpDia.getText().toString()));
                results.setTemperature(Double.parseDouble(mTemperature.getText().toString()));
                results.setSpo2(Double.parseDouble(mSpo2.getText().toString()));
            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.cl_table), "Error: non-decimal number entered.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            mTask = new InsertTableExamDb(results);
            mTask.execute();
        }


    }

    public class InsertTableExamDb extends AsyncTask<Void, Void, Long>
            implements DialogInterface.OnCancelListener {

        int id;
        double height, weight, pulse, bpsys, bpdia, temperature, spo2;
        TableExam exam;
        private ProgressDialog dialog;


        InsertTableExamDb(TableExam result) {
            id = result.getPatientId();
            height = result.getHeight();
            weight = result.getWeight();
            pulse = result.getPulse();
            bpsys = result.getBpsys();
            bpdia = result.getBpdia();
            temperature = result.getTemperature();
            spo2 = result.getSpo2();

            exam = result;
        }

        protected void onPreExecute()
        {
            dialog = ProgressDialog
                    .show(TableExamActivity.this, "", "Loading. Please wait...", true);
        }

        @Override
        protected Long doInBackground(Void... params) {
            LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(TableExamActivity.this);

            final int VISIT_ID = 100; // TODO: Connect the proper VISIT_ID
            final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

            final int CONCEPT_ID = 163189; // RHK EXAM BLURB

            Gson gson = new Gson();
            String toInsert = gson.toJson(exam);

            Log.d(LOG_TAG, toInsert);

            ContentValues complaintEntries = new ContentValues();

            complaintEntries.put("patient_id", patientID);
            complaintEntries.put("visit_id", VISIT_ID);
            complaintEntries.put("creator", CREATOR_ID);
            complaintEntries.put("value", toInsert);
            complaintEntries.put("concept_id", CONCEPT_ID);

            SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
            return localdb.insert("obs", null, complaintEntries);
        }

        protected void onPostExecute(Long result)
        {
            dialog.dismiss();
            obsID = result;

            Intent intent = new Intent(TableExamActivity.this, PhysicalExamActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putStringArrayListExtra("exams", physExams);
            startActivity(intent);
        }

        public void onCancel(DialogInterface dialog)
        {
            cancel(true);
            dialog.dismiss();
        }
    }

}
