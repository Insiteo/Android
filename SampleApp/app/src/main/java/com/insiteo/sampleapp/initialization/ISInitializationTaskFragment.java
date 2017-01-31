package com.insiteo.sampleapp.initialization;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.CommonConstants;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISEServerType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.init.listener.ISIInitListener;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.common.utils.threading.ISICancelable;
import com.insiteo.lbs.map.render.ISERenderMode;

import java.util.Stack;

/**
 * Created by MMO on 08/07/2015.
 */
public class ISInitializationTaskFragment extends Fragment implements ISIInitListener {

    private static final String TAG = "ISInitTaskFragment";

    public enum TaskState {
        INITIALIZING,
        STARTING,
        DOWNLOADING,
        INSTALLING,
        UNKNOWN
    }


    private Callback mListener;

    private TaskState eCurrentState = TaskState.UNKNOWN;

    private long mUpdateProgressValue;
    private long mUpdateProgressTotal;


    public static ISInitializationTaskFragment newInstance() {
        ISInitializationTaskFragment fragment = new ISInitializationTaskFragment();
        return fragment;
    }

    public ISInitializationTaskFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parentFragment = getParentFragment();

        if(parentFragment != null) {
            try {
                mListener = (Callback) parentFragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(parentFragment.toString()
                        + " must implement InsiteoTaskCallbacks");
            }
        } else {
            try {
                mListener = (Callback) getActivity();
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement InsiteoTaskCallbacks");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /***********************************************************************************************
     Fragment callback interface to implement
     **********************************************************************************************/

    public interface Callback {
        void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache);
        void onStartDone(ISError error, Stack<ISPackage> packageToUpdate);
        void onPackageUpdateProgress(ISEPackageType packageType, boolean download,
                                     long progress, long total);
        void onDataUpdateDone(ISError error);
    }

    /***********************************************************************************************
     Initialization and Start
     **********************************************************************************************/

    /**
     * Cancels the current task
     */
    public void cancel() {
        ISICancelable currentTask = Insiteo.getCurrentTask();
        if(currentTask != null) {
            currentTask.cancel();
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    public void initializeAPI() {
        Insiteo.getInstance().initialize(getActivity(), this);
        eCurrentState = TaskState.INITIALIZING;
    }

    protected ISUserSite selectSite() {
        SparseArray<ISUserSite> sites = Insiteo.getCurrentUser().getSites();
        if(sites != null && sites.size() > 0) {
            return sites.valueAt(0);
        }
        return null;
    }

    public void startSite(ISUserSite site) {
        Insiteo.getInstance().startAndUpdate(site, this);
        eCurrentState = TaskState.STARTING;
    }

    public TaskState getCurrentState() {
        return eCurrentState;
    }

    public long getUpdateProgressValue() {
        return mUpdateProgressValue;
    }

    public long getUpdateProgressTotal() {
        return mUpdateProgressTotal;
    }


    /***********************************************************************************************
     ISIInitListener callbacks
     **********************************************************************************************/

    @Override
    public Location selectClosestToLocation() {
        return null;
    }

    @Override
    public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {
        ISLog.d(TAG, "onInitDone() called with: " + "error = [" + error + "], suggestedSite = [" + suggestedSite + "], fromLocalCache = [" + fromLocalCache + "]");
        if (mListener != null) {
            mListener.onInitDone(error, suggestedSite, fromLocalCache);
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        ISLog.d(TAG, "onStartDone() called with: " + "error = [" + error + "], packageToUpdate = [" + packageToUpdate + "]");
        if(mListener != null) {
            mListener.onStartDone(error, packageToUpdate);
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    @Override
    public void onPackageUpdateProgress(ISEPackageType packageType, boolean download,
                                        long progress, long total) {
        ISLog.d(TAG, "onPackageUpdateProgress() called with: " + "packageType = [" + packageType + "], download = [" + download + "], progress = [" + progress + "], total = [" + total + "]");
        mUpdateProgressValue = download ? progress / 1024 : progress;
        mUpdateProgressTotal = download ? total / 1024 : total;

        if(mListener != null) {
            mListener.onPackageUpdateProgress(packageType, download, mUpdateProgressValue,
                    mUpdateProgressTotal);
        }
        eCurrentState = download ? TaskState.DOWNLOADING : TaskState.INSTALLING;
    }

    @Override
    public void onDataUpdateDone(ISError error) {
        if(mListener != null) {
            mListener.onDataUpdateDone(error);
        }
        eCurrentState = TaskState.UNKNOWN;
    }

}
