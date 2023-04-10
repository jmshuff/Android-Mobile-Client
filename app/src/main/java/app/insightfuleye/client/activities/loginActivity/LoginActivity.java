package app.insightfuleye.client.activities.loginActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.File;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.setupActivity.SetupActivity;
import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.models.loginModel.PostSignIn;
import app.insightfuleye.client.models.loginModel.Signin;
import app.insightfuleye.client.models.loginProviderModel.LoginProviderModel;
import app.insightfuleye.client.networkApiCalls.Api;
import app.insightfuleye.client.networkApiCalls.ApiClient;
import app.insightfuleye.client.utilities.Base64Utils;
import app.insightfuleye.client.utilities.DialogUtils;
import app.insightfuleye.client.utilities.Logger;
import app.insightfuleye.client.utilities.OfflineLogin;
import app.insightfuleye.client.utilities.SessionManager;
import app.insightfuleye.client.utilities.StringEncryption;
import app.insightfuleye.client.utilities.UrlModifiers;
import app.insightfuleye.client.widget.materialprogressbar.CustomProgressDialog;

import app.insightfuleye.client.activities.homeActivity.HomeActivity;
import app.insightfuleye.client.utilities.NetworkConnection;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    TextView txt_cant_login;
    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "username:password", "admin:nimda"
    };
    private final String TAG = LoginActivity.class.getSimpleName();
//    protected AccountManager manager;
//    Account Manager is commented....
//    ProgressDialog progress;
    Context context;
    CustomProgressDialog cpd;
    SessionManager sessionManager = null;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;
    private OfflineLogin offlineLogin = null;

    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    // UI references.
    private EditText mUsernameView;
//    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private ImageView icLogo;

    private long createdRecordsCount = 0;
    String provider_url_uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionManager = new SessionManager(this);

        context = LoginActivity.this;
        sessionManager = new SessionManager(context);
        cpd = new CustomProgressDialog(context);

        setTitle(R.string.title_activity_login);

        offlineLogin = OfflineLogin.getOfflineLogin();
        txt_cant_login = findViewById(R.id.cant_login_id);
        txt_cant_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cant_log();
            }
        });
      /*  manager = AccountManager.get(LoginActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/

      /*  Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("login", true);
//            startJobDispatcherService(LoginActivity.this);
            startActivity(intent);
            finish();
        }*/

        //Enforces Offline Login Check only if network not present
        if (!NetworkConnection.isOnline(this)) {
            if (OfflineLogin.getOfflineLogin().getOfflineLoginStatus()) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("login", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

        icLogo = findViewById(R.id.iv_logo);
//        setLogo();

        // Set up the login form.
        mUsernameView = findViewById(R.id.et_email);
        // populateAutoComplete(); TODO: create our own autocomplete code
        mPasswordView = findViewById(R.id.et_password);
//      mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.logD(TAG, "button pressed");
                attemptLogin();
            }
        });

    }

    private void setLogo() {

        File f = new File("/data/data/" + context.getPackageName() + "/files/logo/ic_logo.png");
        if (f.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile("/data/data/" + context.getPackageName() + "/files/logo/ic_logo.png");
            icLogo.setImageBitmap(bitmap);
        } else {
            Log.e("SetLogo","No Logo Found in Mindmap Folder");
        }
    }

    /**
     * Returns void.
     * This method checks if valid username and password are given as input.
     *
     * @return void
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.enter_username));
            mUsernameView.requestFocus();
            return;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.enter_password));
            mPasswordView.requestFocus();
            return;
        }

        if (password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }

        if (NetworkConnection.isOnline(this)) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            UserLoginTask(email, password);
        } else {
            //offlineLogin.login(email, password);
            offlineLogin.offline_login(email, password);

        }

    }

    /**
     * @param password Password
     * @return boolean
     */
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void cant_log() {
        final SpannableString span_string = new SpannableString(getApplicationContext().getText(R.string.email_link));
        Linkify.addLinks(span_string, Linkify.EMAIL_ADDRESSES);

      MaterialAlertDialogBuilder builder =   new MaterialAlertDialogBuilder(this)
                .setMessage(getApplicationContext().getText(R.string.contact_whatsapp))
                .setNegativeButton(R.string.contact, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
//                        Intent intent = new Intent(Intent.ACTION_SENDTO); //to get only the list of e-mail clients
//                        intent.setType("text/plain");
//                        intent.setData(Uri.parse("mailto:support@intelehealth.io"));
//                        // intent.putExtra(Intent.EXTRA_EMAIL, "support@intelehealth.io");
//                        // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                        //  intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
//
//                        startActivity(Intent.createChooser(intent, "Send Email"));
//                        //add email function here !

                        String phoneNumberWithCountryCode = "+917005308163";
                        String message =
                                getString(R.string.hello_my_name_is) + sessionManager.getChwname() +
                                        /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);

                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(
                                        String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                                phoneNumberWithCountryCode, message))));

                    }

                })
                .setPositiveButton(R.string.close_button, null);

      AlertDialog alertDialog = builder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);

        //prajwal_changes
    }

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public void UserLoginTask(String USERNAME, String PASSWORD) {

        ProgressDialog progress;
        Logger.logD(TAG, "username and password" + USERNAME + PASSWORD);
        progress = new ProgressDialog(LoginActivity.this, R.style.AlertDialogStyle);
        ;//SetupActivity.this);
        progress.setTitle(getString(R.string.please_wait_progress));
        progress.setMessage(getString(R.string.logging_in));
        progress.show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Api api = ApiClient.createService(Api.class);
        PostSignIn postSignIn=new PostSignIn(USERNAME, PASSWORD);
        Observable<Signin> loginModelObservable = api.signIn(postSignIn);
        loginModelObservable.subscribe(new Observer<Signin>() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onNext(@NonNull Signin signin) {
                Boolean authencated = signin.getSuccess();
                if(authencated){
                    sessionManager.setChwname(signin.getData().getUser().getUsername());
                    sessionManager.setCreatorID(signin.getData().getUser().getId());
                    sessionManager.setProviderID(signin.getData().getUser().getPersonId());
                    SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                    Log.d("authToken", signin.getData().getToken());
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

                    Log.i(TAG, "onPostExecute: Parse init");
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("setup", true);
                    startActivity(intent);
                    finish();
                    progress.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                progress.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showerrorDialog(LoginActivity.this, "Error Login", getString(R.string.error_incorrect_password), "ok");
                mUsernameView.requestFocus();
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
}
