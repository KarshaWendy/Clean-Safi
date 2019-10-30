package com.example.cleansafi1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.CustomExpandableListAdapter;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class AddressesActivity extends AppCompatActivity implements View.OnClickListener{

    private Button addAddress;
    public static CustomExpandableListAdapter listAdapter;
    public static ExpandableListView expListView;
    private static String mToken;
    public static HashMap<Integer, JSONObject> hashMap;
    public static int sSelectedPosition = -1;
    private static AddressesActivity sInstance;
    private static boolean foreground = false;
    public static JSONArray jsonArray;

    public static AddressesActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);
        sInstance = this;
        setTitle("Addresses");
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToken = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN);
        addAddress = findViewById(R.id.add_location);
        addAddress.setTypeface(AppGlobals.typefaceNormal);
        addAddress.setOnClickListener(this);
        expListView = findViewById(R.id.address_list);
//        expListView
//                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                if (order.size() > 0) {
//                    if (ExpandableListView.getPackedPositionType(l) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
//                        int groupPosition = ExpandableListView.getPackedPositionGroup(l);
//                        int childPosition = ExpandableListView.getPackedPositionChild(l);
//                        Log.i("TAG", "group position" + groupPosition);
//                        try {
//                            JSONObject jsonObject = hashMap.get(groupPosition);
//                            Log.i("TAG", "id " + jsonObject.getString("id"));
//                            sAddressId = jsonObject.getInt("id");
//                            sSelectedPosition = groupPosition;
//                            listAdapter.notifyDataSetChanged();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        Toast.makeText(AddressesActivity.this, "selected", Toast.LENGTH_SHORT).show();
//                        new android.os.Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                finish();
//                            }
//                        }, 1000);
//                        // You now have everything that you would as if this was an OnChildClickListener()
//                        // Add your logic here.
//
//                        // Return true as we are handling the event.
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        System.out.println(mToken);
        Log.i("LOG", "before hashmap condition");
        if (hashMap != null && hashMap.size() > 0) {
            Log.i("LOG", "before foreground");
            if (foreground) {
                listAdapter = new CustomExpandableListAdapter(this, hashMap);
                expListView.setAdapter(listAdapter);
            }
        } else {
            getLocationData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
    }

    public static void getLocationData() {
        mToken = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN);
        HttpRequest mRequest = new HttpRequest(AppGlobals.getContext());
        mRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        System.out.println(request.getResponseText());
                        hashMap = new HashMap<>();
                        try {
                            jsonArray = new JSONArray(request.getResponseText());
                            Log.i("TAG", "array size" + jsonArray);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                hashMap.put(i, jsonObject);
                            }
                            Log.i("TAG", "foreground " + foreground);
                            if (foreground) {
                                listAdapter = new CustomExpandableListAdapter(AppGlobals.getContext(),
                                        hashMap);
                                listAdapter.notifyDataSetChanged();
                                expListView.setAdapter(listAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }
        });
        mRequest.open("GET", AppGlobals.LOCATIONS_URL);
        mRequest.setRequestHeader("Authorization", "Token " + mToken);
        mRequest.send();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_location:
                startActivity(new Intent(this, PickLDropLaundryActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);

    }
}
