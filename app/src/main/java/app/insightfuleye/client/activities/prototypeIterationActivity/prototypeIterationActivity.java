package app.insightfuleye.client.activities.prototypeIterationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.utilities.SessionManager;

public class prototypeIterationActivity extends AppCompatActivity {
    private static final String TAG = prototypeIterationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;

    public static final int PROTOTYPE = 206;


    RadioButton m10D;
    RadioButton m12D;
    RadioButton mWithScope;
    RadioButton mWithoutScope;
    RadioButton mLEDStrip;
    RadioButton mLEDBulb;
    RadioButton mRBulb;
    RadioButton mLBulb;
    RadioButton mBothBulbs;
    RadioButton mRStrip;
    RadioButton mLStrip;
    RadioButton mBothStrip;
    private String mDiopter="";
    private String mScope="";
    private String mLightType="";
    private String mNumLights="";
    private String mPrototype="";
    private String mResistance="";
    private String mScopeLength="";
    LinearLayout numBulbs;
    LinearLayout numStrips;
    LinearLayout typeLights;

    RadioButton m33k;
    RadioButton m15k;
    RadioButton m1k;
    RadioButton m800;
    RadioButton m500;
    RadioButton m10mm;
    RadioButton m12mm;
    RadioButton m14mm;

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
        mRBulb=findViewById(R.id.prototype_RBulb);
        mLBulb=findViewById(R.id.prototype_LBulb);
        mBothBulbs=findViewById(R.id.prototype_BothBulbs);
        numBulbs=findViewById(R.id.numBulbs);
        numStrips=findViewById(R.id.numStrips);
        mRStrip=findViewById(R.id.prototype_RStrip);
        mLStrip=findViewById(R.id.prototype_LStrip);
        mBothStrip=findViewById(R.id.prototype_BothStrip);
        typeLights=findViewById(R.id.prototype_typeOfLights);
        m33k=findViewById(R.id.prototype_33k);
        m15k=findViewById(R.id.prototype_15k);
        m1k= findViewById(R.id.prototype_1k);
        m800= findViewById(R.id.prototype_800);
        m500=findViewById(R.id.prototype_500);
        m10mm=findViewById(R.id.prototype_10mm);
        m12mm=findViewById(R.id.prototype_12mm);
        m14mm=findViewById(R.id.prototype_14mm);

        typeLights.setVisibility(View.GONE);

        numBulbs.setVisibility(View.GONE);
        numStrips.setVisibility(View.GONE);

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
                numBulbs.setVisibility(View.VISIBLE);
                onRadioButtonClicked(v);
            }
        });

        mLEDStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numStrips.setVisibility(View.VISIBLE);
                onRadioButtonClicked(v);
            }
        });

        mRBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mLBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mBothBulbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        mRStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onRadioButtonClicked(v); }
        });

        mLStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mBothStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m33k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        m15k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m1k.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m800.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m10mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m12mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        m14mm.setOnClickListener(new View.OnClickListener() {
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

            case R.id.prototype_RBulb:

            case R.id.prototype_RStrip:
                if (checked)
                    mNumLights = "Right Bulb";
                Log.v(TAG, "Num Lights :" + mNumLights);
                break;
            case R.id.prototype_LBulb:
            case R.id.prototype_LStrip:
                if (checked)
                    mNumLights = "Left Bulbs";
                Log.v(TAG, "Num Lights: " + mNumLights);
                break;
            case R.id.prototype_BothBulbs:
            case R.id.prototype_BothStrip:
                if (checked)
                    mNumLights = "2 Bulbs";
                Log.v(TAG, "Num Lights: " + mNumLights);
                break;
            case R.id.prototype_33k:
                if (checked)
                    mResistance="3.3 Kohm";
                Log.v(TAG, "Resistance: " + mResistance);
                break;

            case R.id.prototype_15k:
                if(checked)
                    mResistance="1.5 Kohm";
                Log.v(TAG, "Resistance: " + mResistance);
                break;

            case R.id.prototype_1k:
                if (checked)
                    mResistance="1 Kohm";
                Log.v(TAG, "Resistance: " + mResistance);
                break;

            case R.id.prototype_800:
                if(checked)
                    mResistance= "800 ohm";
                Log.v("TAG", "Resistance: " + mResistance);
                break;

            case R.id.prototype_500:
                if(checked)
                    mResistance = "500 ohm";
                Log.v(TAG, "Resistance: " + mResistance);
                break;

            case R.id.prototype_10mm:
                if(checked)
                    mScopeLength="10mm";
                Log.v(TAG, "Scope Length: " + mScopeLength);
                break;

            case R.id.prototype_12mm:
                if(checked)
                    mScopeLength="12mm";
                Log.v(TAG,"Scope Length: " + mScopeLength);
                break;

            case R.id.prototype_14mm:
                if (checked)
                    mScopeLength="14mm";
                Log.v(TAG, "Scope Length: " + mScopeLength);
                break;
        }
    }

    private void onFabClicked(){
        if(typeLights.getVisibility()==View.VISIBLE)
            mPrototype=mDiopter + " " + mScope + ", " + mScopeLength + ", " + mLightType + ", " + mNumLights + ", " + mResistance;
        else
            mPrototype=mDiopter + " " + mScope + ", " + mScopeLength + ", " + mResistance;
        Log.d(TAG, "Prototype: " + mPrototype);

        Intent intent1= new Intent();
        intent1.putExtra("prototype", mPrototype);
        setResult(RESULT_OK, intent1);
        finish();
    }

}
