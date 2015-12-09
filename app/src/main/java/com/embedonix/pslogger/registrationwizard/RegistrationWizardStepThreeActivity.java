package com.embedonix.pslogger.registrationwizard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.embedonix.pslogger.MainActivity;
import com.embedonix.pslogger.R;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.data.user.UserRegistration;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Saeid on 30/06/2014.
 */
public class RegistrationWizardStepThreeActivity extends Activity {

    public static final String TAG = "RegistrationWizardStepFourActivity";
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Context mContext;

    private Button mButtonBack;
    private Button mButtonNext;
    private Button mButtonPickPhoto;
    private Button mButtonResetPhoto;
    private Button mButtonNoPhoto;
    private ImageView mImageViewProfilePhoto;
    private boolean mIsAnyButtonClicked;

    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_wizard_step_four);
        mContext = this;

        setupView();
    }

    private void setupView() {

        mButtonBack = (Button) findViewById(R.id.regWizFourButtonBack);
        mButtonBack.setBackgroundResource(R.drawable.button_left_arrow_enable);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mButtonNext = (Button) findViewById(R.id.regWizFourButtonNext);
        disableFinalization();

        mButtonNoPhoto = (Button) findViewById(R.id.regWizFourNoProfilePhoto);
        mButtonNoPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPhoto();
                enableFinalization();
                mIsAnyButtonClicked = true;
            }
        });

        mButtonResetPhoto = (Button) findViewById(R.id.regWizFourResetProfilePhoto);
        mButtonResetPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPhoto();
                mIsAnyButtonClicked = true;
            }
        });

        mButtonPickPhoto = (Button) findViewById(R.id.regWizFourButtonPickPhoto);
        mButtonPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crop.pickImage((RegistrationWizardStepThreeActivity) mContext);
                mIsAnyButtonClicked = true;
            }
        });

        mImageViewProfilePhoto = (ImageView) findViewById(R.id.regWizFourImageView);

        resetPhoto();
        getPreviousPhoto();
    }

    private void getPreviousPhoto() {
        UserRegistration ur = Storage.readUserRegistration(getApplicationContext());
        if (ur != null) {
            if (ur.getUserInfo() != null) {
                mBitmap = ur.getUserInfo().getProfilePicture();
                mImageViewProfilePhoto.setImageBitmap(mBitmap);
                enableFinalization();
            } else {
                disableFinalization();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUserRegistration();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }


    private void enableFinalization() {
        mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_enable);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mIsAnyButtonClicked) {
                    Toast.makeText(mContext, "please select a photo or " +
                            "click on 'don't use photo' button'", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserRegistration();
                doRegistration();
            }
        });
    }

    private void disableFinalization() {
        mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_disable);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "please decide about your profile!", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void updateUserRegistration() {

        if (mBitmap == null) {
            resetPhoto();
            mBitmap = ((BitmapDrawable) mImageViewProfilePhoto.getDrawable()).getBitmap();
        }

        UserRegistration ur = Storage.readUserRegistration(getApplicationContext());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        mBitmap.recycle();
        String bitmapContent = "";
        try {
            bitmapContent = FileHelpers.compressAndBase64(byteArrayOutputStream.toByteArray());
            Log.v(TAG, "Bitmap length is: " + bitmapContent.length());
            byteArrayOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error converting bitmap to gzip and base64");
            e.printStackTrace();
        }


        if (bitmapContent != null & bitmapContent.length() > 10) {
            ur.getUserInfo().setProfilePhotoBase64(bitmapContent);
        } else {
            ur.getUserInfo().setProfilePhotoBase64(null);
        }

        Storage.updateUserRegistration(getApplicationContext(), ur, true);
    }


    private void resetPhoto() {
        mImageViewProfilePhoto.setImageResource(R.drawable.default_profile_photo);
        if(mBitmap != null) {
            mBitmap.recycle();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        new Crop(source).output(outputUri).withMaxSize(100, 100)
                .withAspect(1, 1).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {

            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }

            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver()
                        , Crop.getOutput(result)); //load image from URI

                File tempImageFile = new File(mContext.getFilesDir().getAbsolutePath()
                        , "temp_1311_14_hahahah_lol_WTF_shit_1414.bin");

                FileOutputStream out = new FileOutputStream (tempImageFile);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                mBitmap.recycle();
                mBitmap = null;

                mBitmap = BitmapFactory.decodeFile(tempImageFile.getPath());
                boolean deleted = tempImageFile.delete();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error loading photo. please try with another photo"
                        , Toast.LENGTH_SHORT).show();
                return;
            }

            if (mBitmap != null) {
                mImageViewProfilePhoto.setImageBitmap(mBitmap);
                enableFinalization();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
            disableFinalization();
        }
    }


    public void onCompleteSendingDataToServer(String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle("registration is complete!");
        b.setMessage(message);
        b.setPositiveButton("start the app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UserRegistration ur = Storage.readUserRegistration(getApplicationContext());
                ur.setIsRegistrationDone(true);
                Storage.updateUserRegistration(getApplicationContext(), ur, true);
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        b.create().show();
    }

    private void doRegistration() {
        if(!AppHelpers.isInternetAvailable(this)){
            AppHelpers.showNoInternetConnectionDialog(this);
            return;
        }

        FinalizeRegistration fr = new FinalizeRegistration(this
                , Storage.readUserRegistration(getApplicationContext()));
    }

    public void onErrorSendingRegistrationRequest(String s) {
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle("server error");
        b.setMessage("there was a problem while trying to complete your registration:\n" + s);
        b.setPositiveButton("try again later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.dismiss();
            }
        });
        b.create().show();
    }
}
