package com.embedonix.pslogger.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class should be able to contain as many of
 * {@link com.embedonix.fragmentcontainertest.fragments.CollapsibleFragment}
 * and let the user to position the fragments by holding their header which is of type
 * {@link android.widget.LinearLayout} on a vertical basis in this container.
 * </p>
 * <p>
 * the position of fragments should be saved and restored when application stops and starts. Either
 * using this class itself or the {@link android.app.Activity} that hosts this class.
 * </p>
 * TODO <P>make this class implement OnTouchListener and allow users to reposition fragments
 * by holding the header of a fragment and move inside this container. Animation is a MUST!</P>
 */
public class CollapsibleFragmentContainer extends LinearLayout implements View.OnTouchListener {

    private Context mContext;
    private List<CollapsibleFragment> mFragments;

    private float mXDiff, mYDiff, mX, mY;
    private LayoutParams mThisParams, mFragmentParams;

    public CollapsibleFragmentContainer(Context context) {
        super(context);
        mContext = context;
        this.setOrientation(VERTICAL);
    }

    public CollapsibleFragmentContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public CollapsibleFragmentContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    /**
     * Adds a fragment in the container
     *
     * @param fragment
     */
    public void addFragment(FragmentManager manager, CollapsibleFragment fragment) {
        if (mFragments == null || mFragments.size() == 0) {
            mFragments = new ArrayList<CollapsibleFragment>();
        }

        fragment.setContainerId(this.getId());
        String name = fragment.getName() + "_" + mFragments.size();
        fragment.setName(name);
        mFragments.add(fragment);
        manager.beginTransaction().add(getId(), fragment, name).commit();
    }

    public void addFragment(FragmentManager manager, CollapsibleFragment[] fragments) {
        if (mFragments == null || mFragments.size() == 0) {
            mFragments = new ArrayList<CollapsibleFragment>();
        }

        FragmentTransaction ft = manager.beginTransaction();
        int currentListSize = mFragments.size();

        for (int i = 0; i < fragments.length; i++) {
            CollapsibleFragment colFrag = fragments[i];
            String name = colFrag.getName() + "_" + (currentListSize + i);
            colFrag.setName(name);
            colFrag.setContainerId(this.getId());
            mFragments.add(colFrag);
            ft.add(getId(), colFrag, name);
        }

        ft.commit();
    }

    /**
     * Removes a fragment by tag in list
     *
     * @param tag tag of the fragment in the list
     * @return true if fragment was removed, false if not
     */
    public boolean removeFragmentByTag(FragmentManager fm, String tag) {
        if (mFragments == null | mFragments.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < mFragments.size(); i++) {
                CollapsibleFragment collapsibleFragment = mFragments.get(i);
                if (collapsibleFragment.getName().equals(tag + "_" + i)) {
                    mFragments.remove(i);
                    updatePositions();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * redraw the view for changes to become effective
     */
    private void updatePositions() {
        //throw new UnsupportedOperationException("this method is not yet implemented");
    }

    public void activateAll() {
        if (mFragments != null && mFragments.size() > 0) {
            for (int i = 0; i < mFragments.size(); i++) {
                mFragments.get(i).activate();
            }
        }
    }

    public void deactivateAll() {
        for (int i = 0; i < mFragments.size(); i++) {
            mFragments.get(i).deactivate();
        }
    }

    public int getNumberOfFragments() {
        if (mFragments == null) {
            return -1;
        } else {
            return mFragments.size();
        }
    }

    public boolean removeLastFragment(FragmentManager fm) {
        if (mFragments == null || mFragments.size() == 0) {
            return false;
        } else {
            int position = mFragments.size() - 1;
            return removeFragmentByPosition(fm, position);
        }
    }

    /**
     * Removes a fragment by position in list
     *
     * @param position position of the fragment in the list
     * @return true if fragment was removed, false if not
     */
    public boolean removeFragmentByPosition(FragmentManager fm, int position) {
        if (mFragments == null || mFragments.size() == 0 || mFragments.get(position) == null) {
            return false;
        } else {
            CollapsibleFragment lastFrag = mFragments.get(position);
            lastFrag.deactivate();
            fm.beginTransaction().remove(lastFrag).commit();
            mFragments.remove(position);

            updatePositions();
            return true;
        }
    }

    public boolean removeAllFragments(FragmentManager fm) {
        if (mFragments == null || mFragments.size() == 0) {
            return false;
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            for (int i = 0; i < mFragments.size(); i++) {
                CollapsibleFragment fragment = mFragments.get(i);
                fragment.deactivate();
                ft.remove(fragment);
            }
            ft.commit();
            mFragments = new ArrayList<CollapsibleFragment>();
            return true;
        }
    }

    public boolean collapseAll() {
        if (mFragments == null || mFragments.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < mFragments.size(); i++) {
                mFragments.get(i).collapse(AnimationUtils.loadAnimation(mContext,
                        android.R.anim.fade_out), false);
            }
            return true;
        }
    }

    public boolean expandAll() {
        if (mFragments == null || mFragments.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < mFragments.size(); i++) {
                mFragments.get(i).expand(AnimationUtils.loadAnimation(mContext,
                        android.R.anim.fade_in), false);
            }
            return true;
        }
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {

        /**
         * TODO when user touches and holds a fragment header, the fragment view should pop out
         * and allow user to reposition the fragment in the container (this)
         */



        CollapsibleFragment fragment = null; //this will be the fragment which is's header is touched

        //This loops through fragments and toasts the name of fragment that it's header was clicked
        for (int i = 0; i < mFragments.size(); i++) {
            CollapsibleFragment frag = mFragments.get(i);
            if (frag.getHeader() == v) {
                fragment = frag;
                break;
            }
        }

        if(fragment == null) { //touch was not in a header of any fragments, return false
            return false;
        }


        mThisParams = (LayoutParams) this.getLayoutParams();




        return true;
    }
}

