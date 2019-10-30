package com.example.cleansafi1.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.requests.HttpRequest;
import com.example.cleansafi1.MainActivity;
import com.example.cleansafi1.R;
import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.WebServiceHelpers;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


public class UpdateProfile extends Activity implements HttpRequest.OnErrorListener,
        HttpRequest.OnReadyStateChangeListener {

    private Button mUpdateButton;
    private EditText mUsername;
    private EditText mEmailAddress;
    private EditText mPhoneNumber;
    private EditText mPassword;
    private EditText mVerifyPassword;

    private String mUsernameString;
    private String mEmailAddressString;
    private String mPhoneNumberString;
    private String mPasswordString;
    private String mVerifyPasswordString;
    private boolean isPasswordChanged = false;

    private HttpRequest request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.update_profile);
        mUsername = (EditText) findViewById(R.id.user_name);
        mEmailAddress = (EditText) findViewById(R.id.email);
        mPhoneNumber = (EditText) findViewById(R.id.phone);
        mPassword = (EditText) findViewById(R.id.password);
        mVerifyPassword = (EditText) findViewById(R.id.verify_password);
        mUpdateButton = (Button) findViewById(R.id.update_button);
        mUsername.setTypeface(AppGlobals.typefaceNormal);
        mEmailAddress.setTypeface(AppGlobals.typefaceNormal);
        mPhoneNumber.setTypeface(AppGlobals.typefaceNormal);
        mPassword.setTypeface(AppGlobals.typefaceNormal);
        mVerifyPassword.setTypeface(AppGlobals.typefaceNormal);
        mUpdateButton.setTypeface(AppGlobals.typefaceNormal);

        //getting user saved data from sharedPreference
        mUsername.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME));
        mEmailAddress.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        mPhoneNumber.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER));
        mEmailAddress.setEnabled(false);

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isPasswordChanged = true;

            }
        });

        mVerifyPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isPasswordChanged = true;
            }
        });
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsernameString = mUsername.getText().toString();
                mPhoneNumberString = mPhoneNumber.getText().toString();
                System.out.println(mPhoneNumberString + "working");
                if (isPasswordChanged) {
                    if (validateEditText()) {
                        updateUser(mUsernameString, mPasswordString, mPhoneNumberString);
                    }
                } else {

                    updateUser(mUsernameString, mPasswordString, mPhoneNumberString);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPasswordChanged = false;
    }

    private boolean validateEditText() {

        boolean valid = true;
        mPasswordString = mPassword.getText().toString();
        mVerifyPasswordString = mVerifyPassword.getText().toString();


        if (mPasswordString.trim().isEmpty() || mPasswordString.length() < 3) {
            mPassword.setError("enter at least 3 characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (mVerifyPasswordString.trim().isEmpty() || mVerifyPasswordString.length() < 3 ||
                !mVerifyPasswordString.equals(mPasswordString)) {
            mVerifyPassword.setError("password does not match");
            valid = false;
        } else {
            mVerifyPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                WebServiceHelpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(this, "Registration Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_OK:
                        System.out.println(request.getResponseText() + "working ");
                        Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            String username = jsonObject.getString(AppGlobals.KEY_FULL_NAME);
                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String email = jsonObject.getString(AppGlobals.KEY_EMAIL);
                            String phoneNumber = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER);

                            //saving values
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FULL_NAME, username);
                            Log.i("user name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME));
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }

    }

    private void updateUser(String username, String password, String phoneNumber) {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("PUT", "http://178.62.87.25/api/user/me");
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(updateUserData(username, password, phoneNumber));
        WebServiceHelpers.showProgressDialog(this, "Updating User Profile");
    }

    private String updateUserData(String username, String password, String phoneNumber) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("full_name", username);
            jsonObject.put("mobile_number", phoneNumber);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject.toString());
        return jsonObject.toString();

    }
}
