package com.embedonix.pslogger.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.helpres.AppConstants;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.serverwork.UploadNotUploadedFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 05/08/2014.
 */
public class UploadQueueFragment extends CollapsibleFragment {

    public static final String TAG = "UploadQueueFragment";

    private View mView;
    private TextView mTvQueueInformation;
    private ImageView mQueueUpload;
    private ProgressBar mProgressBar;
    private LinearLayout mContainerProgressBar;

    private boolean mIsUploading;
    private Timer mTimer;
    private ArrayList<File> mFiles;

    public UploadQueueFragment() {
        setName(TAG);
        super.TAG = TAG;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_upload_queue, container, false);
        setupView();
        setupWidgets();
        if(mShouldActivateOnCreate) {
            activate();
        }
        return mView;
    }

    @Override
    protected void setupView() {
        mSupport = (CollapsibleFragmentSupport) getActivity();

        mHeaderLayout = mView.findViewById(R.id.queueHeader);
        //mHeaderLayout.setOnTouchListener(mSupport.getContainer());
        mContainer = mView.findViewById(R.id.queueContainer);
        mCollapsedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        mExpandedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_add);

        mToggleVisibilityImageView =
                (ImageView) mView.findViewById(R.id.queueToggleVisibility);
        mToggleVisibilityImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (getVisibilityState()) {
                    case COLLAPSED:
                        Animation expandAnimation = AnimationUtils.loadAnimation(getActivity()
                                , android.R.anim.fade_in);
                        expand(expandAnimation, false);
                        break;
                    case EXPANDED:
                        Animation collapseAnimation = AnimationUtils.loadAnimation(getActivity()
                                , android.R.anim.fade_out);
                        collapse(collapseAnimation, false);
                        break;
                }
            }
        });
    }

    private void setupWidgets(){
        mTvQueueInformation = (TextView) mView.findViewById(R.id.queueInformation);
        mQueueUpload = (ImageView) mView.findViewById(R.id.queueUpload);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.queueProgressBar);
        mContainerProgressBar =
                (LinearLayout) mView.findViewById(R.id.queueProgressBarContainer);

        mTvQueueInformation.setText("Files to upload: 0");
    }

    @Override
    public void setShouldActivateOnCreate(boolean shouldActivateOnCreate) {

    }

    @Override
    public void activate() {
        super.activate();

        if(mTimer != null) {
            return;
        }

        mTimer = new Timer("QueueInformation");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkForRemainingFiles();
            }
        }, 1000, 1000 * 60 * 5); //after 2 seconds, each 5 minutes

    }

    @Override
    public void deactivate() {
        super.deactivate();

        if(mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        mTimer = null;
        mProgressBar.setProgress(0);
        mFiles = null;
        mIsUploading = false;
    }

    @Override
    protected void expand(Animation animation, boolean shouldActivate) {
        super.expand(animation, shouldActivate);
    }

    @Override
    protected void collapse(Animation animation, boolean shouldDeactivate) {
        super.collapse(animation, shouldDeactivate);
    }

    private void checkForRemainingFiles() {

        Context ctx = null;

        try{
            ctx = getActivity();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if(ctx == null) {
            return;
        }


        final Context context = ctx;

        new AsyncTask<Void, Void, List<File>>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvQueueInformation.setText("Checking for remaining files...");
                    }
                });
            }

            @Override
            protected List<File> doInBackground(Void... params) {


                List<File> files = null;

                try {
                    files = FileHelpers.getListOfNotUploadedFiles(context);
                } catch (Exception e) {
                    files = null;
                }

                if(files != null && files.size() > 0) {
                    mFiles = (ArrayList<File>) files;
                    return files;
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<File> files) {
                super.onPostExecute(files);

                final List<File> theFiles = files;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(theFiles != null) {
                                onListOfFilesReceived(theFiles);
                            } else {
                                mTvQueueInformation.setText("There are no files in queue now");
                            }
                        }
                    });
                }

            }
        }.execute();
    }

    private void onListOfFilesReceived(final List<File> files) {

        final UploadQueueFragment instance = this;

        mTvQueueInformation.setText("There are " + files.size() + " files waiting for upload");
        mQueueUpload.setBackgroundResource(R.drawable.ic_action_upload);
        mQueueUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsUploading) {
                    Toast.makeText(getActivity(), "please wait for the current files to be uploaded"
                            , Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!AppHelpers.getAppPreferences(getActivity()).isLoggedIn()) {
                    Toast.makeText(getActivity(), "you are not logged in." +
                            "\nplease login to be able to process upload queue"
                            , Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!AppHelpers.isInternetAvailable(getActivity())) {
                    Toast.makeText(getActivity(), "you are not connected to internet!" +
                            "\nplease try when you are connected.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //now initiate upload task

                mIsUploading = true;
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
                mProgressBar.setMax(files.size());
                mProgressBar.setProgress(0);
                mTvQueueInformation.setText("Preparing to upload " + files.size()  + " files");
                UserInfo ui = Storage.readUserRegistration(getActivity()).getUserInfo();

              new UploadNotUploadedFiles(getActivity(), ui, instance)
                      .execute(files.toArray(new File[files.size()]));
            }
        });
    }

    public void onUploadResultReceived(boolean result) {
        if(result) {
            Toast.makeText(getActivity(), "successfully uploaded " + mFiles.size() + " files."
                    , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "unfortunately an error " +
                    "occured during uploading queued files, please try again later"
                    , Toast.LENGTH_SHORT).show();
        }

        mQueueUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });
        mFiles = null;
        mQueueUpload.setBackgroundResource(android.R.drawable.ic_menu_upload);
        deactivate();
        activate();
        mIsUploading = false;
    }

    public void onUploadProgressReceived(Integer value) {
        mTvQueueInformation.setText(String.format("Uploaded %d of %d files "
                ,value + 1, mProgressBar.getMax()));
        mProgressBar.setProgress(value);
    }

    public boolean isUploading() {
        return mIsUploading;
    }
}
