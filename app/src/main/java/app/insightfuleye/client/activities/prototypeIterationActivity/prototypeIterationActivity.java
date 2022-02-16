package app.insightfuleye.client.activities.prototypeIterationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.uploadImageActivity.uploadImageActivity;
import app.insightfuleye.client.activities.uploadImageActivity.uploadImageInfoActivity;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.utilities.SessionManager;

public class prototypeIterationActivity extends AppCompatActivity {
    private static final String TAG = prototypeIterationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;

    RadioButton m10D;
    RadioButton m12D;
    RadioButton mWithScope;
    RadioButton mWithoutScope;
    RadioButton mLEDStrip;
    RadioButton mLEDBulb;
    RadioButton m1Bulb;
    RadioButton m2Bulb;
    private String mDiopter="";
    private String mScope="";
    private String mLightType="";
    private String mNumLights="";
    private String mPrototype="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prototype_iteration);
        setTitle("Select Prototype Iteration");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        m10D=findViewById(R.id.prototype_10D);
        m12D=findViewById(R.id.prototype_12D);
        mWithScope=findViewById(R.id.prototype_withScope);
        mWithoutScope=findViewById(R.id.prototype_withoutScope);
        mLEDStrip=findViewById(R.id.prototype_ledStrip);
        mLEDBulb=findViewById(R.id.prototype_ledBulb);
        m1Bulb=findViewById(R.id.prototype_1Bulb);
        m2Bulb=findViewById(R.id.prototype_2Bulbs);
        FloatingActionButton fab = findViewById(R.id.fab);

        m10D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m12D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mWithScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mWithoutScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mLEDBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mLEDStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m1Bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m2Bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClicked();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.prototype_10D:
                if (checked)
                    mDiopter = "10D";
                Log.v(TAG, "Diopter: " + mDiopter);
                break;
            case R.id.prototype_12D:
                if (checked)
                    mDiopter = "12D";
                Log.v(TAG, "Diopter: " + mDiopter);
                break;

            case R.id.prototype_withScope:
                if (checked)
                    mScope = "With Scope";
                Log.v(TAG, "Scope: " + mScope);
                break;
            case R.id.prototype_withoutScope:
                if (checked)
                    mScope = "Without Scope";
                Log.v(TAG, "Scope: " + mScope);
                break;

            case R.id.prototype_ledBulb:
                if (checked)
                    mLightType = "LED Bulb";
                Log.v(TAG, "Light Type :" + mLightType);
                break;
            case R.id.prototype_ledStrip:
                if (checked)
                    mLightType = "LED Strip";
                Log.v(TAG, "Light Type :" + mLightType);
                break;

            case R.id.prototype_1Bulb:
                if (checked)
                    mNumLights = "1 Bulb";
                Log.v(TAG, "Num Lights :" + mNumLights);
                break;
            case R.id.prototype_2Bulbs:
                if (checked)
                    mNumLights = "2 Bulbs";
                Log.v(TAG, "Num Lights: " + mNumLights);
                break;
        }
    }

    private void onFabClicked(){
        if (mLightType=="LED Bulb"){
            mPrototype=mDiopter + " " + mScope + ", " + mLightType + ", " + mNumLights;
        }
        else{
            mPrototype=mDiopter + " " + mScope + ", " + mLightType;
        }
        Log.d(TAG, "Prototype: " + mPrototype);

        Intent intent1= new Intent(prototypeIterationActivity.this, uploadImageInfoActivity.class);
        intent1.putExtra("prototype", mPrototype);
        startActivity(intent1);
    }

}
