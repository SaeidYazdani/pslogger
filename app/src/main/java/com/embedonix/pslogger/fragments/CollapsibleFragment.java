package com.embedonix.pslogger.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * <p>
 * This class is a base for fragments that should have activation, deactivation, collapsing and
 * expanding capabilities.</p>
 * <p>{@link #activate()} and {@link #deactivate()} will cause the supposed functionality
 * to start and stop respectively.
 * </p>
 * <p/>
 * <p>{@link #collapse(android.view.animation.Animation, boolean)} and
 * {@link #expand(android.view.animation.Animation, boolean)} will cause the layout that contains
 * the views which have to do with the functionality,change its visibility
 * to <b>GONE</b> and <b>VISIBLE</b> accordingly.
 * </p>
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 24/07/2014.
 */
public abstract class CollapsibleFragment extends Fragment {

    protected String TAG = "CollapsibleFragment";

    protected boolean mShouldActivateOnCreate;

    protected View mView;
    protected CollapsibleFragmentSupport mSupport;
    protected int mContainerId;
    protected ImageView mToggleVisibilityImageView;
    protected View mHeaderLayout;
    protected View mContainer;
    protected Drawable mCollapsedStateDrawable;
    protected Drawable mExpandedStateDrawable;
    protected boolean mIsAttachedToActivity;
    private VisibilityState mVisibilityState;
    private OperationState mOperationState;

    public CollapsibleFragment() {
        mVisibilityState = VisibilityState.EXPANDED;
        mOperationState = OperationState.INACTIVE;
    }

    protected abstract void setupView();

    /**
     * <p>
     * If there is a need to activate a fragment right after its declaration and
     * before attaching to UI, then this method should be called with a true as its argument.     *
     * </p>
     * <p>The activation will happen right before <b>onCreateView()</b> returns the view</p>
     *
     * @param shouldActivateOnCreate if <b>true</b> the fragment gets activated in
     *                               <b>onCreateView</b>, if <b>false</b>, it will not be activated
     *                               as long as {@link #activate()} gets called.
     */
    public abstract void setShouldActivateOnCreate(boolean shouldActivateOnCreate);

    public String getName() {
        return TAG;
    }

    public void setName(String tagName) {
        TAG = tagName;
    }

    /**
     * collapse the container
     *
     * @param animation        Animation to perform
     * @param shouldDeactivate if true, {@link #activate()} will be also called
     */
    protected void collapse(Animation animation, boolean shouldDeactivate) {

        mContainer.setAnimation(animation);
        mContainer.animate();
        mContainer.setVisibility(View.GONE);
        mToggleVisibilityImageView.setImageDrawable(mExpandedStateDrawable);

        mVisibilityState = VisibilityState.COLLAPSED;

        if (shouldDeactivate) {
            deactivate();
            mVisibilityState = VisibilityState.COLLAPSED;

        }
        Log.i(TAG, "Collapsed");
    }

    /**
     * deactivate the functionality in the fragment
     */
    protected void deactivate() {
        mOperationState = OperationState.INACTIVE;
        Log.i(TAG, "deactivated");
    }

    /**
     * collapse the container     *
     *
     * @param animation      Animation to perform
     * @param shouldActivate if true, {@link #deactivate()} will be also called
     */
    protected void expand(Animation animation, boolean shouldActivate) {

        mContainer.setAnimation(animation);
        mContainer.animate();
        mContainer.setVisibility(View.VISIBLE);
        mToggleVisibilityImageView.setImageDrawable(mCollapsedStateDrawable);

        mVisibilityState = VisibilityState.EXPANDED;

        if (shouldActivate) {
            activate();
            mVisibilityState = VisibilityState.EXPANDED;
            mOperationState = OperationState.ACTIVE;
        }
        Log.i(TAG, "Expanded");
    }

    /**
     * activate the functionality in the fragment
     */
    protected void activate() {
        mOperationState = OperationState.ACTIVE;
        Log.i(TAG, "activated");
    }

    public View getHeader() {
        return mHeaderLayout;
    }

    /**
     * Get the visibility state of the fragment's container
     *
     * @return state of fragment
     * {@link com.embedonix.pslogger.fragments.CollapsibleFragment.VisibilityState}
     */
    protected VisibilityState getVisibilityState() {
        return mVisibilityState;
    }

    /**
     * Get the operation state of the fragment's functionality
     *
     * @return true if Activated, false if Inactivated
     */
    public OperationState getOperationState() {
        return mOperationState;
    }

    public int getContainerId() {
        return mContainerId;
    }

    public void setContainerId(int id) {
        mContainerId = id;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIsAttachedToActivity = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttachedToActivity = false;
    }

    public boolean isAttached() {
        return mIsAttachedToActivity;
    }

    /**
     * Visibility of the fragment content
     */
    public enum VisibilityState {
        /**
         * Visibility of the container is View.GONE
         */
        COLLAPSED,
        /**
         * Visibility of the container is View.VISIBLE
         */
        EXPANDED,
    }

    /**
     * Operation state, whether the fragment functionality is activated or deactivated
     */
    public enum OperationState {
        /**
         * Activated
         */
        ACTIVE,
        /**
         * Inactivated
         */
        INACTIVE
    }
}