package com.example.cleansafi1.account;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.byteshaft.requests.HttpRequest;
import com.example.cleansafi1.R;
import com.example.cleansafi1.autocountryflag.BaseActivity;
import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


public class RegisterActivity extends BaseActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mRegisterButton;
    private EditText mUsername;
    private EditText mEmailAddress;
    private EditText mPassword;
    private EditText mVerifyPassword;
    private String mUsernameString;
    private String mEmailAddressString;
    private String mVerifyPasswordString;
    public String mPhoneNumberString;
    private String mPasswordString;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 0;

    private HttpRequest request;
    private static RegisterActivity sInstance;
    private static final int MY_PERMISSIONS_REQUEST_PHONE_STATE = 1;

    public static RegisterActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.register);
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        sInstance = this;
        mUsername = findViewById(R.id.user_name);
        mEmailAddress =  findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mVerifyPassword = findViewById(R.id.verify_password);
        mRegisterButton = findViewById(R.id.register_button);
        mUsername.setTypeface(AppGlobals.typefaceNormal);
        mEmailAddress.setTypeface(AppGlobals.typefaceNormal);
        mPassword.setTypeface(AppGlobals.typefaceNormal);
        mVerifyPassword.setTypeface(AppGlobals.typefaceNormal);
        mRegisterButton.setTypeface(AppGlobals.typefaceNormal);
        mRegisterButton.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_PHONE_STATE);
        } else {
            initUI();
            initCodes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
                    alertDialog.setTitle("Permission request");
                    alertDialog.setMessage("To verify your phone number " + getString(R.string.app_name)
                            + " app can easily check your verification code if you allow Sms permission.");
                    alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ActivityCompat.requestPermissions(RegisterActivity.this,
                                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                                    MY_PERMISSIONS_REQUEST_READ_SMS);
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    if (validateEditText()) {
                        registerUser(
                                mUsernameString,
                                mPasswordString,
                                mEmailAddressString,
                                mPhoneNumberString.replaceAll("\\+", "")
                        );
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initUI();
                            initCodes();
                        }
                    }, 500);
                } else {
                    Toast.makeText(this, "Select your country manually", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    private boolean validateEditText() {
        boolean valid = true;
        mPasswordString = mPassword.getText().toString();
        mVerifyPasswordString = mVerifyPassword.getText().toString();
        mEmailAddressString = mEmailAddress.getText().toString();
        mPhoneNumberString = mPhoneEdit.getText().toString().replaceAll(" ", "").replaceAll("'+'", "");
        mUsernameString = mUsername.getText().toString();
        if (mPasswordString.trim().isEmpty() || mPasswordString.length() < 6) {
            mPassword.setError("enter at least 6 characters");
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
        if (mPhoneNumberString.trim().isEmpty() || mPhoneNumberString.length() < 3) {
            mPhoneEdit.setError("please enter your phone number");
            valid = false;
        } else {
            mPhoneEdit.setError(null);
        }


        if (mEmailAddressString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailAddressString).matches()) {
            mEmailAddress.setError("please provide a valid email");
            valid = false;
        } else {
            mEmailAddress.setError(null);
        }
        return valid;
    }

    private void registerUser(String username, String password, String email, String phoneNumber) {
        request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%suser/register", AppGlobals.BASE_URL));
        request.send(getRegisterData(username, password, email, phoneNumber));
        WebServiceHelpers.showProgressDialog(RegisterActivity.this, "Registering User ");
    }


    private String getRegisterData(String username, String password, String email, String phoneNumber) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("full_name", username);
            jsonObject.put("email", email);
            jsonObject.put("mobile_number", phoneNumber);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        if (readyState == HttpRequest.STATE_DONE) {
            WebServiceHelpers.dismissProgressDialog();
            Log.i("TAG", "Response " + request.getResponseText());
            switch (request.getStatus()) {
                case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                    AppGlobals.alertDialog(RegisterActivity.this, "Registration Failed!", "please check your internet connection");
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    AppGlobals.alertDialog(RegisterActivity.this, "Registration Failed!", "Email already in use");
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    System.out.println(request.getResponseText() + "working ");
                    Toast.makeText(getApplicationContext(), "Activation code has been sent to you! Please check your Email", Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject jsonObject = new JSONObject(request.getResponseText());
                        System.out.println(jsonObject + "working ");
                        String username = jsonObject.getString(AppGlobals.KEY_FULL_NAME);
                        String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                        String email = jsonObject.getString(AppGlobals.KEY_EMAIL);
                        String phoneNumber = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER);
                        //saving values
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FULL_NAME, username);
                        Log.i("user name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME));
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                        Log.i("user name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER));
                        AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                        AppGlobals.saveUserLogin(true);
                        LoginActivity.getInstance().finish();
                        finish();
                        Intent intent = new Intent(getApplicationContext(), CodeConfirmationActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyS, short error, Exception exception) {
        System.out.println(request.getStatus());
        switch (request.getStatus()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);
    }

    public void finish() {
    }
}
