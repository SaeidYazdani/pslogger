package com.embedonix.pslogger.fragments;

/**
 * <p>Activities that contain
 * {@link CollapsibleFragmentContainer} should
 * implement this interface!
 *
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 25/07/2014.
 */
public interface CollapsibleFragmentSupport {
    /**
     * <p>Gets a reference to the container where
     * {@link CollapsibleFragment} instances are being
     * shown.
     * </p>
     * @return {@link CollapsibleFragmentContainer}
     */
    public CollapsibleFragmentContainer getContainer();
}
