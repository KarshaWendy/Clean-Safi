package com.example.cleansafi1.utils;

import android.app.Activity;
import android.app.ProgressDialog;

public class WebServiceHelpers {

    private static ProgressDialog progressDialog;

    public WebServiceHelpers() {
    }

    public static void showProgressDialog(Activity activity, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }
}
