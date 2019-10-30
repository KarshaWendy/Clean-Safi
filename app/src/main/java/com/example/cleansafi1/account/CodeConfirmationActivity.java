package com.example.cleansafi1.account;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.requests.HttpRequest;
import com.example.cleansafi1.MainActivity;
import com.example.cleansafi1.R;
import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.WebServiceHelpers;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;


public class CodeConfirmationActivity extends Activity implements
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mResendButton;
    private EditText mobileNumber;
    private String email;
    private EditText editTextOne;
    private EditText editTextTwo;
    private EditText editTextThree;
    private EditText editTextFour;
    private EditText editTextFive;
    private HttpRequest request;
    private SmsListener smsListener;
    private TextView timeTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.confirmation_code_activity);
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        mobileNumber =  findViewById(R.id.et_confirmation_code_mobile_number);
        editTextOne =  findViewById(R.id.et_confirmation_code_one);
        editTextTwo =  findViewById(R.id.et_confirmation_code_two);
        editTextThree =  findViewById(R.id.et_confirmation_code_three);
        editTextFour =  findViewById(R.id.et_confirmation_code_four);
        editTextFive =  findViewById(R.id.et_confirmation_code_five);
        mResendButton =  findViewById(R.id.btn_confirmation_code_resend);
        timeTextView = findViewById(R.id.text_view_time);
        progressBar =  findViewById(R.id.progressBar);
        mobileNumber.setTypeface(AppGlobals.typefaceNormal);
        editTextOne.setTypeface(AppGlobals.typefaceNormal);
        editTextTwo.setTypeface(AppGlobals.typefaceNormal);
        editTextThree.setTypeface(AppGlobals.typefaceNormal);
        editTextFour.setTypeface(AppGlobals.typefaceNormal);
        editTextFive.setTypeface(AppGlobals.typefaceNormal);
        mResendButton.setTypeface(AppGlobals.typefaceNormal);
        mResendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResendButton.isEnabled()) {
                    runTimer();
                    resendOtp(email);
                } else {
                    Toast.makeText(CodeConfirmationActivity.this, "please wait",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        String number = getIntent().getStringExtra("mobile_number");
        System.out.println(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER));
        mobileNumber.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER));
        email = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL);
        smsListener = new SmsListener();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(smsListener, filter);
        runTimer();
        progressBar.setVisibility(View.GONE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void runTimer() {
        mResendButton.setEnabled(false);
        new CountDownTimer(120000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
                String time = String.format("0%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                if (Integer.valueOf(time.split(":")[1]) < 10) {
                    String[] ifLess = time.split(":");
                    time = ifLess[0] + ":" + "0" + ifLess[1];
                }
                timeTextView.setText(time);
            }

            @Override
            public void onFinish() {
                timeTextView.setText("Time ended");
                mResendButton.setEnabled(true);
            }
        }.start();
    }

    private void resendOtp(String email) {
        request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        WebServiceHelpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                break;
                            case HttpURLConnection.HTTP_OK:
                                if (progressBar.getVisibility() == View.VISIBLE) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                Toast.makeText(getApplicationContext(), "Sms sent", Toast.LENGTH_SHORT).show();

                        }
                }

            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        request.open("POST", String.format("%suser/request-activation-key", AppGlobals.BASE_URL));
        request.send(getOtpData(email));
        WebServiceHelpers.showProgressDialog(CodeConfirmationActivity.this, "Resending Sms");
    }

    private String getOtpData(String email) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsListener != null) {
            unregisterReceiver(smsListener);
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                WebServiceHelpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Toast.makeText(getApplicationContext(), "Please enter correct account activation key", Toast.LENGTH_LONG).show();
                        break;
                    case HttpURLConnection.HTTP_OK:
                        if (progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.GONE);
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            String username = jsonObject.getString(AppGlobals.KEY_FULL_NAME);
                            String userId = jsonObject.getString(AppGlobals.KEY_USER_ID);
                            String email = jsonObject.getString(AppGlobals.KEY_EMAIL);
                            String phoneNumber = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER);
                            String token = jsonObject.getString(AppGlobals.KEY_TOKEN);

                            Log.i("TAG", "Token "+ token);
                            //saving values
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FULL_NAME, username);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_TOKEN, token);
                            AppGlobals.saveUserActive(true);
                            RegisterActivity.getInstance().finish();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }

    }


    private void activateUser(String email, String emailOtp) {
        request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%suser/activate", AppGlobals.BASE_URL));
        request.send(getUserActivationData(email, emailOtp));
        WebServiceHelpers.showProgressDialog(CodeConfirmationActivity.this, "Activating User");
    }


    private String getUserActivationData(String email, String emailOtp) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("sms_otp", emailOtp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        System.out.println(request.getStatus());
        switch (request.getStatus()) {

        }
    }

    public class SmsListener extends BroadcastReceiver {

        private SharedPreferences preferences;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
                SmsMessage[] msgs = null;
                String msg_from;
                if (bundle != null) {
                    //---retrieve the SMS message received---
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            String msgBody = msgs[i].getMessageBody();
                            Log.i("TAG", msgBody + " From " + msg_from);
                            if (msgBody.length() == 5) {
                                progressBar.setVisibility(View.VISIBLE);
                                editTextOne.setText(String.valueOf(msgBody.charAt(0)));
                                editTextTwo.setText(String.valueOf(msgBody.charAt(1)));
                                editTextThree.setText(String.valueOf(msgBody.charAt(2)));
                                editTextFour.setText(String.valueOf(msgBody.charAt(3)));
                                editTextFive.setText(String.valueOf(msgBody.charAt(4)));
                                activateUser(email, msgBody);
                                getWindow().setSoftInputMode(
                                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                );
                            }
                        }
                    } catch (Exception e) {
//                            Log.d("Exception caught",e.getMessage());
                    }
                }
            }
        }
    }
}
