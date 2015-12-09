package com.embedonix.pslogger.registrationwizard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.embedonix.pslogger.BaseActivity;
import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.user.UserRegistration;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.DialogAction;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.serverwork.ServerConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Saeid on 27-6-2014.
 */
public class RegistrationWizardStepTwoActivity extends BaseActivity
        implements
        TextView.OnEditorActionListener,
        View.OnKeyListener,
        View.OnFocusChangeListener {

    public final static String TAG = "RegistrationWizardStepTwoActivity";
    ArrayList<View> mViewsToBeValidated;
    private Context mContext;
    private boolean mIsNameOk;
    private boolean mIsUsernameOk;
    private boolean mIsPasswordsOk;
    private boolean mIsEmailOk;
    private Button mButtonBack;
    private Button mButtonNext;
    private EditText mInputFullName;
    private EditText mInputUserName;
    private EditText mInputPassword;
    private EditText mInputPasswordRepeat;
    private EditText mInputEmail;
    private EditText mCurrentEditText;

    private Button mButtonRegisterWithFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_wizard_step_two);
        mContext = this;

        setupView();
    }

    private void setupView() {
        mButtonBack = (Button) findViewById(R.id.regWizTwoButtonBack);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mButtonNext = (Button) findViewById(R.id.regWizTwoButtonNext);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "please fill the form first!",
//                        Toast.LENGTH_SHORT).show();

                goToNextEditText();
            }
        });

        mInputFullName = (EditText) findViewById(R.id.regWizTwoFullName);
        mInputFullName.requestFocus();
        mInputFullName.setSelection(0);
        mCurrentEditText = mInputFullName;
        mInputUserName = (EditText) findViewById(R.id.regWizTwoUserName);
        mInputPassword = (EditText) findViewById(R.id.regWizTwoPass);
        mInputPasswordRepeat = (EditText) findViewById(R.id.regWizTwoPassRepeat);
        mInputEmail = (EditText) findViewById(R.id.regWizTwoEmail);

        updateFromSavedValuesFromUserRegistrationFile();

        mViewsToBeValidated = new ArrayList<View>();
        mViewsToBeValidated.add(mInputFullName);
        mViewsToBeValidated.add(mInputUserName);
        mViewsToBeValidated.add(mInputPassword);
        mViewsToBeValidated.add(mInputPasswordRepeat);
        mViewsToBeValidated.add(mInputEmail);

        setupAutoValidations();
    }

    private void goToNextEditText() {

        mIsNameOk = true;
        mIsEmailOk = true;

        if(mCurrentEditText != null) {

            if(mCurrentEditText == mInputFullName){
                mInputUserName.requestFocus();
                mInputUserName.setSelection(0);
                return;
            }

            if(mCurrentEditText == mInputUserName) {
                new CheckUserNameExistTask().execute();
                mInputPassword.requestFocus();
                mInputPassword.setSelection(0);
                return;
            }

            if(mCurrentEditText == mInputPassword) {
                mInputPasswordRepeat.requestFocus();
                mInputPasswordRepeat.setSelection(0);
                return;
            }

            if(mCurrentEditText == mInputPasswordRepeat) {
                mInputEmail.requestFocus();
                mInputEmail.setSelection(0);
                onFormCompleted();
                return;
            }

            if(mCurrentEditText == mInputEmail) {
                mInputFullName.requestFocus();
                mInputFullName.setSelection(0);
                mCurrentEditText = mInputFullName;
                onFormCompleted();
                return;
            }
        }
    }

    private void updateFromSavedValuesFromUserRegistrationFile() {
        UserRegistration ur = Storage.readUserRegistration(getApplicationContext());

        if (ur != null) {
            if (ur.getUserInfo().getFullName() != null) {
                mInputFullName.setText(ur.getUserInfo().getFullName());
                mIsNameOk = true;
            }

            if (ur.getUserInfo().getUserName() != null) {
                mInputUserName.setText(ur.getUserInfo().getUserName());
                mIsUsernameOk = true;
                new CheckUserNameExistTask().execute();
            }

            if (ur.getUserInfo().getPassword() != null) {
                mInputPassword.setText(ur.getUserInfo().getPassword());
                mInputPasswordRepeat.setText(ur.getUserInfo().getPassword());
                mIsPasswordsOk = true;
            }

            if (ur.getUserInfo().getEmailAddress() != null) {
                mInputEmail.setText(ur.getUserInfo().getEmailAddress());
                mIsEmailOk = true;
            }

            onFormCompleted();
        }
    }

    private void setupAutoValidations() {

        //This is an special case because it requires the username to not have any spaces!
        mInputUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mCurrentEditText = mInputUserName;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = editable.toString().replaceAll(" ", "").trim();
                if (!editable.toString().equals(result)) {
                    mInputUserName.setText(result);
                    mInputUserName.setSelection(result.length());
                }
                onFormCompleted();
            }
        });

        mInputFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mCurrentEditText = mInputFullName;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mCurrentEditText = mInputPassword;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInputPasswordRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mCurrentEditText = mInputPasswordRepeat;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mCurrentEditText = mInputEmail;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mInputFullName.setOnEditorActionListener(this);
        mInputUserName.setOnEditorActionListener(this);
        mInputPassword.setOnEditorActionListener(this);
        mInputPasswordRepeat.setOnEditorActionListener(this);
        mInputEmail.setOnEditorActionListener(this);

        mInputFullName.setOnKeyListener(this);
        mInputUserName.setOnKeyListener(this);
        mInputPassword.setOnKeyListener(this);
        mInputPasswordRepeat.setOnKeyListener(this);
        mInputEmail.setOnKeyListener(this);

        mInputFullName.setOnFocusChangeListener(this);
        mInputUserName.setOnFocusChangeListener(this);
        mInputPassword.setOnFocusChangeListener(this);
        mInputPasswordRepeat.setOnFocusChangeListener(this);
        mInputEmail.setOnFocusChangeListener(this);

        onFormCompleted();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUserRegistration();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
/*        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!AppHelpers.isValidEmail(mInputEmail.getText())) {
                Toast.makeText(mContext, "email address is not valid!",
                        Toast.LENGTH_SHORT).show();
                mInputEmail.setText("");
                mInputEmail.setHint("valid email address!");
                mIsEmailOk = true;
            } else {
                mIsEmailOk = true;
            }
            onFormCompleted();
            return false;
        }
        if (i == EditorInfo.IME_ACTION_NEXT) {

            validateInputsByView(textView);
            onFormCompleted();
            return false;
        }*/

        onFormCompleted();

        return false;
    }

    private void onFormCompleted() {
        if (isFormCompleted()) {
            mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_enable);
            mButtonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUserRegistration();
                    Intent intent = new Intent(mContext,
                            RegistrationWizardStepThreeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                }
            });
        } else {
            mButtonNext.setBackgroundResource(R.drawable.button_right_arrow_disable);
            mButtonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(mContext, "please fill the form first!",
//                            Toast.LENGTH_SHORT).show();
                    goToNextEditText();
                }
            });
        }
    }

    private boolean isFormCompleted() {
        if (/*mInputFullName.getText().toString().trim().equals("") ||*/
                mInputUserName.getText().toString().trim().equals("") ||
                        mInputPassword.getText().toString().trim().equals("") ||
                        mInputPasswordRepeat.getText().toString().trim().equals("")/* ||
                mInputEmail.getText().toString().trim().equals("")*/) {
            return false;
        }

        if (!mIsNameOk || !mIsUsernameOk || !mIsPasswordsOk || !mIsEmailOk) {
            return false;
        }

        return true;
    }

    private void updateUserRegistration() {
        UserRegistration ur = Storage.readUserRegistration(getApplicationContext());

        if (ur == null) {
            ur = new UserRegistration();
        }

        if (mIsNameOk) {

            if (mInputFullName != null && mInputFullName.getText().length() > 0) {
                ur.getUserInfo().setFullName(mInputFullName.getText().toString());
            } else {
                ur.getUserInfo().setFullName("no name".toString());
            }
        }
        if (mIsUsernameOk) {
            ur.getUserInfo().setUserName(mInputUserName.getText().toString());
        }
        if (mIsPasswordsOk) {
            ur.getUserInfo().setPassword(mInputPassword.getText().toString());
        }
        if (mIsEmailOk) {
            if (mInputEmail != null && mInputEmail.getText().length() > 0) {
                ur.getUserInfo().setEmailAddress(mInputEmail.getText().toString());
            } else {
                ur.getUserInfo().setEmailAddress(" no@email.giv");
            }
        }

        Storage.updateUserRegistration(getApplicationContext(), ur, true);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            validateInputsByView(view);
        }
        return false;
    }

    private void validateInputsByView(View view) {
        switch (view.getId()) {
            case R.id.regWizTwoFullName:

                if (mInputFullName.getText().length() == 0) {
                    mIsNameOk = true;
                    break;
                }
                if (mInputFullName.getText().length() < 3) {
                    String name = mInputFullName.getText().toString();
                    Toast.makeText(mContext, "full name should be at least 3 characters!",
                            Toast.LENGTH_SHORT).show();
                    mInputFullName.setText("");
                    mInputFullName.setHint(name + " is that a name?");
                    //mIsEmailOk = false; //full name is now optional
                    mIsNameOk = true;
                } else {
                    mIsNameOk = true;
                }
                break;
            case R.id.regWizTwoUserName:
                if (mInputUserName.getText().length() == 0) {
                    mIsUsernameOk = false;
                    break;
                }

                if (mInputUserName.length() < 3) {
                    mIsUsernameOk = false;
                    Toast.makeText(mContext, "username should be at least 3 characters!",
                            Toast.LENGTH_SHORT).show();
                    mInputUserName.setText("");
                    mInputUserName.setHint("username: at least 3 characters");
                    break;
                }

                //if more than 3 char
                new CheckUserNameExistTask().execute();

                break;
            case R.id.regWizTwoPass:

                if (mInputPassword.getText().length() == 0) {
                    mIsPasswordsOk = false;
                    break;
                }

                if (mInputPassword.getText().length() < 6) {
                    Toast.makeText(mContext, "password should be at least 6 characters!",
                            Toast.LENGTH_SHORT).show();
                    mInputPassword.setText("");
                    mInputPassword.setHint("password > 6 characters!");
                    mIsPasswordsOk = false;
                }
                break;
            case R.id.regWizTwoPassRepeat:

                if (mInputPasswordRepeat.getText().length() == 0) {
                    mIsPasswordsOk = false;
                    break;
                }

                if (!mInputPassword.getText().toString()
                        .equals(mInputPasswordRepeat.getText().toString())) {
                    Toast.makeText(mContext, "your passwords does not match!",
                            Toast.LENGTH_SHORT).show();
                    mInputPasswordRepeat.setText("");
                    mInputPasswordRepeat.setHint("must match above!");
                    mIsPasswordsOk = false;
                } else {
                    mIsPasswordsOk = true;
                }
                break;
            case R.id.regWizTwoEmail:

/*                if (mInputEmail.getText().length() == 0) {
                    mIsEmailOk = true;
                    break;
                }

                if (!AppHelpers.isValidEmail(mInputEmail.getText())) {
                    Toast.makeText(mContext, "email address is not valid!",
                            Toast.LENGTH_SHORT).show();
                    mInputEmail.setText("");
                    mInputEmail.setHint("valid email address!");
                    mIsEmailOk = true;
                } else {
                    mIsEmailOk = true;
                    //new CheckEmailExistTask().execute();
                }*/
                mIsEmailOk = true;
                break;
            default:
                break;
        }
        onFormCompleted();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            validateInputsByView(view);
        }
    }


    private class CheckUserNameExistTask extends AsyncTask<Void, Void, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(Void... voids) {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair(ServerConstants.ACTION,
                    ServerConstants.CHECK_USERNAME_EXISTS));
            pairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME,
                    mInputUserName.getText().toString()));

            HttpResponse response = null;
            HttpPost httppost = new HttpPost(getAppPreferences().getServerBaseUrl() +
                    ServerConstants.URL_CHECK_FOR_EXISTING_VALUES);

            try {
                httppost.setEntity(new UrlEncodedFormEntity(pairs));
                HttpClient client = new DefaultHttpClient();
                response = client.execute(httppost);
            } catch (IllegalStateException is) { //happens if HttpPost has given an invalid URL
                is.printStackTrace();
                return null;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            super.onPostExecute(response);

            if (response == null) {
                Log.e(TAG, "SERVER ERR response is NULL " +
                        "- Can not check if username exist in mHealth database");
                return;
            }

            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    if (result.equals(ServerConstants.RESULT_VALUE_EXIST)) {
                        String currentUsername = mInputUserName.getText().toString();
                        Toast.makeText(mContext, currentUsername + " Already Exist!",
                                Toast.LENGTH_SHORT).show();
                        mInputUserName.setText("");
                        mInputUserName.setHint("username already taken!");
                        mIsUsernameOk = false;
                    } else if (result.equals(ServerConstants.RESULT_VALUE_NOT_EXIST)) {
                        mIsUsernameOk = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    AppHelpers.showErrorDialog((RegistrationWizardStepTwoActivity) mContext,
                            "Server error.", "Unfortunately our registration server is not " +
                                    "functioning at the moment. please " +
                                    "try again later!", "Wait", "Exit", DialogAction.NO_ACTION,
                            DialogAction.FINISH_ACTIVITY
                    );
                }
            } else {
                Log.e(TAG, "SERVER ERR - Can not check if username exist in mHealth database");
            }
        }
    }

    private class CheckEmailExistTask extends AsyncTask<Void, Void, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(Void... voids) {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair(ServerConstants.ACTION,
                    ServerConstants.CHECK_EMAIL_EXIST));
            pairs.add(new BasicNameValuePair(ServerConstants.PF_EMAIL,
                    mInputEmail.getText().toString()));

            HttpResponse response = null;
            HttpPost httppost = new HttpPost(getAppPreferences().getServerBaseUrl()
                    + ServerConstants.URL_CHECK_FOR_EXISTING_VALUES);

            try {
                httppost.setEntity(new UrlEncodedFormEntity(pairs));
                HttpClient client = new DefaultHttpClient();
                response = client.execute(httppost);
            } catch (IllegalStateException is) { //happens if HttpPost has given an invalid URL
                is.printStackTrace();
                return null;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            super.onPostExecute(response);

            if (response == null) {
                Log.e(TAG, "response is NULL - Can not check if username exist in database");
                return;
            }

            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    if (result.equals(ServerConstants.RESULT_VALUE_EXIST)) {
                        String currentEmail = mInputEmail.getText().toString();
                        Toast.makeText(mContext, currentEmail + " Already Exist try another" +
                                        "or login if you have previously registered!",
                                Toast.LENGTH_SHORT).show();
                        mInputEmail.setText("");
                        mInputEmail.setHint("email exist in database");
                        mIsEmailOk = true;
                    } else if (result.equals(ServerConstants.RESULT_VALUE_NOT_EXIST)) {
                        mIsEmailOk = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    AppHelpers.showErrorDialog((RegistrationWizardStepTwoActivity) mContext,
                            "Server error.", "Unfortunately our registration server is not " +
                                    "functioning at the moment. please " +
                                    "try again later!", "Wait", "Exit", DialogAction.NO_ACTION,
                            DialogAction.FINISH_ACTIVITY
                    );
                }
            } else {
                Log.e(TAG, "SERVER ERR - Can not check if email exist in database");
            }
        }
    }

}
