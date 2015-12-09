package com.embedonix.pslogger.registrationwizard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.embedonix.pslogger.R;


public class RegistrationWizardStepOneActivity extends Activity {

    private Button mButtonNext;
    private CheckBox mCheckBoxAgree;
    private Context mContext;
    private Button mButtonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_wizard_step_one);

        mContext = this;
        mButtonNext = (Button) findViewById(R.id.regWizOneButtonNext);
        mCheckBoxAgree = (CheckBox) findViewById(R.id.regWizOneCheckBoxAgree);

        mCheckBoxAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_enable);
                } else { //not checked
                    mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_disable);
                }
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckBoxAgree.isChecked()) {
                    Intent intent = new Intent(mContext,
                            RegistrationWizardStepTwoActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                } else { //not checked
                    Toast.makeText(mContext, "To continue, you have to check the agreement " +
                            "checkbox!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mButtonBack = (Button) findViewById(R.id.regWizOneButtonBack);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
