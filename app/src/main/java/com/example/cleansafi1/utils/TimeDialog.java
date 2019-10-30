package com.example.cleansafi1.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.cleansafi1.CheckoutStageTwo;
import com.example.cleansafi1.R;

import static com.example.cleansafi1.CheckoutStageTwo.sDropSelected;
import static com.example.cleansafi1.CheckoutStageTwo.sPickUpSelected;



public class TimeDialog extends Dialog implements View.OnClickListener {

    private RadioButton radioTwo;
    private RadioButton radioFour;
    private RadioButton radioSix;
    private Context mContext;
    private int  dialogType = 0;

    public TimeDialog(Context context, int themeResId, int dialogType) {
        super(context, themeResId);
        mContext = context;
        this.dialogType = dialogType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_dialog_layout);
        radioTwo = (RadioButton) findViewById(R.id.radio_two);
        radioFour = (RadioButton) findViewById(R.id.radio_four);
        radioSix = (RadioButton) findViewById(R.id.radio_six);
        radioTwo.setOnClickListener(this);
        radioFour.setOnClickListener(this);
        radioSix.setOnClickListener(this);
        switch (dialogType) {
            case 0:
                switch (sPickUpSelected) {
                    case 1:
                        radioTwo.setChecked(true);

                        break;
                    case 2:
                        radioFour.setChecked(true);
                        break;
                    case 3:
                        radioSix.setChecked(true);
                        break;
                }
                break;
            case 1:
                switch (sDropSelected) {
                    case 1:
                        radioTwo.setChecked(true);

                        break;
                    case 2:
                        radioFour.setChecked(true);
                        break;
                    case 3:
                        radioSix.setChecked(true);
                        break;
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radio_two:
                Log.i("TAG", "2");
                radioFour.setChecked(false);
                radioSix.setChecked(false);
                if (dialogType == 0) {
                    CheckoutStageTwo.getInstance().pickUpTimeSelected = true;
                    sPickUpSelected = 1;
                    CheckoutStageTwo.getInstance().pickUpTimeString = "2pm-4pm";
                } else {
                    CheckoutStageTwo.getInstance().dropTimeSelected = true;
                    CheckoutStageTwo.getInstance().dropTimeString = "2pm-4pm";
                    sDropSelected = 1;
                }

                break;
            case R.id.radio_four:
                radioTwo.setChecked(false);
                radioSix.setChecked(false);
                Log.i("TAG", "4");
                sPickUpSelected = 2;
                if (dialogType == 0) {
                    CheckoutStageTwo.getInstance().pickUpTimeSelected = true;
                    CheckoutStageTwo.getInstance().pickUpTimeString = "4pm-6pm";
                    sPickUpSelected = 1;
                } else {
                    CheckoutStageTwo.getInstance().dropTimeSelected = true;
                    CheckoutStageTwo.getInstance().dropTimeString = "4pm-6pm";
                    sDropSelected = 1;
                }
                break;
            case R.id.radio_six:
                radioTwo.setChecked(false);
                radioFour.setChecked(false);
                Log.i("TAG", "6");
                sPickUpSelected = 3;
                if (dialogType == 0) {
                    CheckoutStageTwo.getInstance().pickUpTimeSelected = true;
                    CheckoutStageTwo.getInstance().pickUpTimeString = "6pm-8pm";
                    sPickUpSelected = 1;
                } else {
                    CheckoutStageTwo.getInstance().dropTimeSelected = true;
                    CheckoutStageTwo.getInstance().dropTimeString = "6pm-8pm";
                    sDropSelected = 1;
                }
                break;

        }
        if (dialogType == 0) {
            CheckoutStageTwo.getInstance().pickUpTimeText.setText(CheckoutStageTwo.getInstance()
                    .pickUpDateString +" "+ CheckoutStageTwo.getInstance().pickUpTimeString);
        } else {
            CheckoutStageTwo.getInstance().dropTime.setText(CheckoutStageTwo.getInstance()
                    .dropDateString+" "+ CheckoutStageTwo.getInstance().dropTimeString);
        }
        Toast.makeText(mContext, "time selected", Toast.LENGTH_SHORT).show();
        CheckoutStageTwo.getInstance().pickUpTimeText.setBackgroundColor(
                mContext.getResources().getColor(R.color.card_selected_color));
        dismiss();
    }
}
