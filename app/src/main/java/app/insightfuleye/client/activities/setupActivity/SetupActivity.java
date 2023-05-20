package app.insightfuleye.client.activities.setupActivity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.LocationDAO;
import app.insightfuleye.client.models.Data;
import app.insightfuleye.client.models.DownloadMindMapRes;
import app.insightfuleye.client.models.Results;
import app.insightfuleye.client.models.dto.LocationDTO;
import app.insightfuleye.client.models.loginModel.PostSignIn;
import app.insightfuleye.client.models.loginModel.Signin;
import app.insightfuleye.client.networkApiCalls.Api;
import app.insightfuleye.client.networkApiCalls.ApiClient;
import app.insightfuleye.client.networkApiCalls.ApiInterface;
import app.insightfuleye.client.utilities.Base64Utils;
import app.insightfuleye.client.utilities.DialogUtils;
import app.insightfuleye.client.utilities.DownloadMindMaps;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.NetworkConnection;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringEncryption;
import app.insightfuleye.client.utilities.UrlModifiers;
import app.insightfuleye.client.utilities.exception.DAOException;
import app.insightfuleye.client.widget.materialprogressbar.CustomProgressDialog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SetupActivity extends AppCompatActivity {

    private static final String TAG = SetupActivity.class.getSimpleName();
    private boolean isLocationFetched;
    private static final int PERMISSION_ALL = 1;
    private long createdRecordsCount = 0;
    ProgressDialog mProgressDialog;

    //    protected AccountManager manager;
    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    AlertDialog.Builder dialog;
    String key = "aravind20212"; //JS
    String licenseUrl = "testing.intelehealth.org"; //JS
    SessionManager sessionManager = null;
    public File base_dir;
    public String[] FILES;
    //        private TestSetup mAuthTask = null;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private TextView mAndroidIdTextView;
    private String hospitalLocation;
    private String[] hospitalLocations;
    private Spinner spinner;
    private String mUrlString;
    //private RadioButton r1;
    //private RadioButton r2;
    final Handler mHandler = new Handler();
    boolean click_box = false;

    Context context;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;
    CustomProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar();
        sessionManager = new SessionManager(this);
        // Persistent login information
//        manager = AccountManager.get(SetupActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        context = SetupActivity.this;
        customProgressDialog = new CustomProgressDialog(context);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        // populateAutoComplete(); TODO: create our own autocomplete code
        sessionManager.setMindMapServerUrl(licenseUrl);//JS
        getMindmapDownloadURL("https://" + licenseUrl + ":3004/", key);//JS

        mLoginButton = findViewById(R.id.setup_submit_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkConnection.isOnline(v.getContext())) {
                    attemptLogin();
                } else {
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //r1 = findViewById(R.id.demoMindmap);
        //r2 = findViewById(R.id.downloadMindmap);

        mPasswordView = findViewById(R.id.password);


        mAndroidIdTextView = findViewById(R.id.textView_Aid);
        String deviceID = "Device Id: " + IntelehealthApplication.getAndroidId();
        mAndroidIdTextView.setText(deviceID);

        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showOkDialog(this, getString(R.string.generic_warning), getString(R.string.setup_internet), getString(R.string.generic_ok));
        hospitalLocations = getResources().getStringArray(R.array.hospital_locations);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hospitalLocations);
        spinner = findViewById(R.id.spinner_location);
        spinner.setAdapter(locationAdapter);
        EditText editLocation = findViewById(R.id.ac_hospital_location);
        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
                System.out.println("spinnerclicked");
            }
        });

        editLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    spinner.performClick();
                    System.out.println("spinnerclicked");
                }

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editLocation.setText(parent.getItemAtPosition(position).toString());

                hospitalLocation = hospitalLocations[spinner.getSelectedItemPosition()];
                if (hospitalLocation != null & !hospitalLocation.isEmpty()) {
                    //String urlString = mUrlField.getText().toString();
                    mUrlString = getResources().getStringArray(R.array.base_urls)[spinner.getSelectedItemPosition() - 1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showProgressbar();
    }

    /**
     * Check username and password validations.
     * Get user selected location.
     */
    private void attemptLogin() {

//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;

        }
        if (spinner.getSelectedItemPosition() <= 0) {
            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_selected), Toast.LENGTH_LONG);
            cancel=true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            TestSetup(mUrlString, email, password);
            Log.d(TAG, "attempting setup");
        }
    }

    private void showProgressbar() {


// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(SetupActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }



   /* public void onRadioClick(View v) {

        boolean checked = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.demoMindmap:
                if (checked) {
                    r2.setChecked(false);
                }
                break;

            case R.id.downloadMindmap:
                if (NetworkConnection.isOnline(this)) {
                    if (checked) {
                        r1.setChecked(false);
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                        // AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                        LayoutInflater li = LayoutInflater.from(this);
                        View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                        licenseUrl= mUrlField.getText().toString();
                        sessionManager.setMindMapServerUrl(licenseUrl);
                        getMindmapDownloadURL("http://" + licenseUrl + ":3004/");
                    }
                } else {
                    ((RadioButton) v).setChecked(false);
                    Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }*/

    /**
     * Attempts login to the OpenMRS server.
     * If successful cretes a new {@link Account}
     * If unsuccessful details are saved in SharedPreferences.
     */
    public void TestSetup(String CLEAN_URL, String USERNAME, String PASSWORD) {
        sessionManager.setServerUrl(CLEAN_URL);
        sessionManager.setBaseUrl(CLEAN_URL);

        ProgressDialog progress;
        Logger.logD(TAG, "username and password" + USERNAME + PASSWORD);
        progress = new ProgressDialog(SetupActivity.this, R.style.AlertDialogStyle);
        ;//SetupActivity.this);
        progress.setTitle(getString(R.string.please_wait_progress));
        progress.setMessage(getString(R.string.logging_in));
        progress.show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ApiClient.changeApiBaseUrl(CLEAN_URL);
        Api api = ApiClient.createService(Api.class);
        Log.d("baseUrl", ApiClient.getApiBaseUrl());
        PostSignIn postSignIn = new PostSignIn(USERNAME, PASSWORD);


        Observable<Signin> loginModelObservable = api.signIn(postSignIn);
        loginModelObservable.subscribe(new Observer<Signin>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(@NonNull Signin signin) {
                Log.d("signin", String.valueOf(signin.getSuccess()));
                Boolean authencated = signin.getSuccess();
                if (authencated) {
                    sessionManager.setSetupComplete(true);
                    sessionManager.setChwname(signin.getData().getUser().getUsername());
                    sessionManager.setCreatorID(signin.getData().getUser().getPersonId());
                    //sessionManager.setProviderID(signin.getData().getUser().getPersonId());
                    sessionManager.setAuthToken(signin.getData().getToken());
                    sessionManager.setRefreshToken(signin.getData().getRefreshToken());
                    SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                    //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                    sqLiteDatabase.beginTransaction();
                    //read_db.beginTransaction();
                    ContentValues values = new ContentValues();

                    String random_salt = getSalt_DATA();

                    //String random_salt = stringEncryption.getRandomSaltString();
                    Log.d("salt", "salt: " + random_salt);
                    //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                    //salt_getter_setter.setSalt(random`_salt);


                    String hash_password = null;
                    try {
                        //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                        hash_password = StringEncryption.convertToSHA256(random_salt + PASSWORD);
                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                    try {
                        values.put("username", USERNAME);
                        values.put("password", hash_password);
                        values.put("creator_uuid_cred", sessionManager.getCreatorID());
                        values.put("chwname", signin.getData().getUser().getUsername());
                        values.put("provider_uuid_cred", sessionManager.getProviderID());
                        createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        sqLiteDatabase.setTransactionSuccessful();

                        Logger.logD("values", "values" + values);
                        Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                    } catch (SQLException e) {
                        Log.d("SQL", "SQL user credentials: " + e);
                    } finally {
                        sqLiteDatabase.endTransaction();
                    }

                    synchronized(this){
                        getLocationFromServer();
                    }

                    Log.i(TAG, "onPostExecute: Parse init");
                    Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                    intent.putExtra("setup", true);
                    startActivity(intent);
                    finish();
                    progress.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                progress.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showerrorDialog(SetupActivity.this, "Error Login", getString(R.string.error_incorrect_password), "ok");
                mEmailView.requestFocus();
                mPasswordView.requestFocus();
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });


    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("salt.env")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                salt = mLine;
                Log.d("SA", "SA " + salt);
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return salt;
    }

    private void getMindmapDownloadURL(String url, String key) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context, mProgressDialog);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();

                            } else {
//                                Toast.makeText(SetupActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                                Toast.makeText(SetupActivity.this, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Log.e("MindMapURL", " " + e);
                            Toast.makeText(SetupActivity.this, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {
        //Check is there any existing mindmaps are present, if yes then delete.

        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");
    }
    private void getLocationFromServer() {
        System.out.println("getLocation");
        if(mUrlString!=null){
            ApiClient.changeApiBaseUrl(mUrlString);
        }
        Api apiService = ApiClient.createService(Api.class);
        try {
            Observable<Results> resultsObservable = apiService.getLocations();
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Results>() {
                        @Override
                        public void onNext(Results locationResults) {
                            if (locationResults.getData() != null) {
                                //LocationDTO locationDTO= locationResults.getData().getRows().get(0);

                                List<LocationDTO> items = locationResults.getData().getRows();
                                System.out.println("# of locations server " + items.size());
                                Log.d("locations", items.toString());

                                LocationDAO locationDAO = new LocationDAO();
                                try {
                                    locationDAO.insertLocations(items);
                                } catch (DAOException e) {
                                    e.printStackTrace();
                                }
                                isLocationFetched = true;
                            } else {
                                isLocationFetched = false;
                                Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();
                            }
                        }


                        @Override
                        public void onError(Throwable e) {
                            isLocationFetched = false;
                            Toast.makeText(SetupActivity.this, getString(R.string.error_location_not_fetched), Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }
}