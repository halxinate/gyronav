package com.kukarin.app.gyronav;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by alexk on 2/9/2016.
 */
public class Busy {
    static Busy instance = null;
    Context mContext = null;
    ProgressDialog mDialog = null;

    private Busy() {
    }

    public static Busy getInstance() {
        if (instance == null)
            instance = new Busy();
        return instance;
    }

    public void setContext(Context c) {
        mContext = c;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Show spinner on UI
     */
    public void On() {
        if(mDialog==null) {
            mDialog = new ProgressDialog(mContext);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage("Processing. Please wait...");
            mDialog.setIndeterminate(true);
            mDialog.setCanceledOnTouchOutside(false);
        }
        mDialog.show();
    }

    /**
     * Hide Spinner on UI
     */
    public void Off() {
        mDialog.dismiss();
    }
}
