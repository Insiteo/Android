package com.insiteo.sampleapp.initialization;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.init.listener.ISIInitListener;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.common.utils.threading.ISICancelable;

import java.util.Stack;

/**
 * Created by MMO on 08/07/2015.
 */
public class ISInitializationTaskFragment extends Fragment implements ISIInitListener {

    private static final String TAG = "ISInitTaskFragment";
    private ProgressDialog mProgress;

    public enum TaskState {
        INITIALIZING,
        STARTING,
        DOWNLOADING,
        INSTALLING,
        UNKNOWN
    }


  //  private Callback mListener;

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
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Downloading New Package");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setIndeterminate(true);
        mProgress.setProgress(0);
      //  View rootView = inflater.inflate(R.layout.fragment_map_location, container, false);

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initializeAPI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        ISLog.d(TAG, this + "initializeAPI: ");
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
        ISLog.d(TAG, this + "onInitDone() called with: " + "error = [" + error + "], suggestedSite = [" + suggestedSite + "], fromLocalCache = [" + fromLocalCache + "]");
        if(error == null) {
            // The suggested site will be started
            ISLog.i(TAG, "onInitDone: starting site " + suggestedSite);
            Insiteo.getInstance().start(suggestedSite, this);
        } else {
            ISLog.e(TAG, "onInitDone: " + error);
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        ISLog.d(TAG, "onStartDone() called with: " + "error = [" + error + "], packageToUpdate = [" + packageToUpdate + "]");
        if(error == null) {
            if(!packageToUpdate.isEmpty()) {
                // Package update are available. They will be downloaded.
                ISLog.d(TAG, "onStartDone: packages should be updated");
                Insiteo.getInstance().update(this, packageToUpdate);
                mProgress.show();
            } else {
                // No package require to be updated. The SDK is no ready to be used.
                ISLog.e(TAG, "onStartDone:  no pck to update starting map");
                initMap();
            }
        } else {
            ISLog.e(TAG, "onStartDone: " + error);
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    @Override
    public void onPackageUpdateProgress(ISEPackageType packageType, boolean download,
                                        long progress, long total) {
        ISLog.d(TAG, "onPackageUpdateProgress() called with: " + "packageType = [" + packageType + "], download = [" + download + "], progress = [" + progress + "], total = [" + total + "]");
        mUpdateProgressValue = download ? progress / 1024 : progress;
        mUpdateProgressTotal = download ? total / 1024 : total;
        mProgress.setProgress((int) mUpdateProgressValue);
        eCurrentState = download ? TaskState.DOWNLOADING : TaskState.INSTALLING;
    }

    @Override
    public void onDataUpdateDone(ISError error) {
        if(error == null) {
            // Packages have been updated. The SDK is no ready to be used.
            ISLog.e(TAG, "onDataUpdateDone: starting map");
            initMap();
            mProgress.hide();
        } else {
            ISLog.e(TAG, "onDataUpdateDone: " + error);
            mProgress.hide();
        }
        eCurrentState = TaskState.UNKNOWN;
    }

    public void initMap()
    {

    }
}
