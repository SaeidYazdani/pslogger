package com.embedonix.pslogger.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.embedonix.pslogger.BaseActivity;
import com.embedonix.pslogger.R;

public class ShowAboutAppActivity extends BaseActivity {

    public static final String SHOW_ABOUT_DIALOG =
            "com.embedonix.pslogger.action.SHOW_ABOUT_DIALOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String versionName = "";
        try {
            versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "1.0";
            e.printStackTrace();
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.app_name).toLowerCase() + " version " + versionName);
        b.setMessage(getApplicationReference().getAboutAppString());
        b.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        b.setCancelable(false);
        Dialog dialog = b.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}



