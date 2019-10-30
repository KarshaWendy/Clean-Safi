package com.example.cleansafi1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleansafi1.account.LoginActivity;
import com.example.cleansafi1.laundry.LaundryCategoriesActivity;
import com.example.cleansafi1.laundry.OrderItem;
import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.WebServiceHelpers;
import com.byteshaft.requests.HttpRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.cleansafi1.laundry.LaundryCategoriesActivity.order;


public class CheckOutActivity extends AppCompatActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button sendButton;
    private ListView listView;
    private TextView nothingInCart;
    private ArrayList<Integer> keysArrayList;
    private Adapter adapter;
    public static int sAddressId = -1;
    public static HashMap<Integer, Integer> sTotalPrice;
    private static CheckOutActivity sInstance;

    public static CheckOutActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        sInstance = this;
        setTitle("Checkout");
        sTotalPrice = new HashMap<>();
        AddressesActivity.getLocationData();
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        listView = (ListView) findViewById(R.id.order_list);
        nothingInCart = (TextView) findViewById(R.id.nothing_in_cart);
        sendButton = (Button) findViewById(R.id.send);
        sendButton.setTypeface(AppGlobals.typefaceNormal);
        sendButton.setOnClickListener(this);
        keysArrayList = new ArrayList<>();
        for (Map.Entry<Integer, OrderItem> map : order.entrySet()) {
            keysArrayList.add(map.getKey());
        }
        if (keysArrayList.size() < 1) {
            nothingInCart.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }else {
            nothingInCart.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
        adapter = new Adapter(getApplicationContext(), R.layout.delegate_order_list, keysArrayList);
        listView.setAdapter(adapter);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                if (AppGlobals.isUserLoggedIn() && AppGlobals.isUserActive()) {
                    startActivity(new Intent(getApplicationContext(), CheckoutStageTwo.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    Toast.makeText(this, "please login to proceed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void sendData(String pickUpTime, String dropTime, String laundryType) {
        if (sAddressId == -1) {
            Toast.makeText(this, "please select your address", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONArray jsonArray = new JSONArray();
        for (Integer key : keysArrayList) {
            OrderItem orderItem = order.get(key);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("item", orderItem.getId());
                jsonObject.put("quantity", orderItem.getQuantity());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        orderRequest(jsonArray, pickUpTime,  dropTime, laundryType);
    }


    private class Adapter extends ArrayAdapter<Integer> {

        private ArrayList<Integer> orderData;
        private ViewHolder viewHolder;

        public Adapter(Context context, int resource, ArrayList<Integer> orderData) {
            super(context, resource);
            this.orderData = orderData;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_order_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.order_name);
                viewHolder.quantity = (TextView) convertView.findViewById(R.id.order_quantity);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.order_image);
                viewHolder.price = (TextView) convertView.findViewById(R.id.order_price);
                viewHolder.plusButton = (ImageButton) convertView.findViewById(R.id.plus);
                viewHolder.minusButton = (ImageButton) convertView.findViewById(R.id.minus);
                viewHolder.name.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.quantity.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.price.setTypeface(AppGlobals.typefaceNormal);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            OrderItem orderItem = order.get(orderData.get(position));
            Log.i("TAG", "" + String.valueOf(orderItem == null));
            String titleLowerCase = orderItem.getName();
            String firstUpper = titleLowerCase.substring(0, 1).toUpperCase() + titleLowerCase.substring(1);
            viewHolder.name.setText(firstUpper);
            viewHolder.quantity.setText("Qty: " + orderItem.getQuantity());
            int price = 0;
            viewHolder.minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String qty = viewHolder.quantity.getText().toString().replace("Qty: ", "");
                    OrderItem orderItem = order.get(orderData.get(position));
                    if (Integer.valueOf(qty) == 1) {
                        orderData.remove(position);
                        order.remove(orderItem.getId());
                        Toast.makeText(CheckOutActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                        adapter.remove(position);
                        adapter.notifyDataSetChanged();
                        if (orderData.size() < 1) {
                            nothingInCart.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        }
                        return;
                    }
                    orderItem.setQuantity(String.valueOf(Integer.valueOf(qty)-1));
                    order.put(orderItem.getId(), orderItem);
                    sTotalPrice = new HashMap<>();
                    notifyDataSetChanged();
                    Log.i("TAG", "minusButton click");
                }
            });
            viewHolder.plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String qty = viewHolder.quantity.getText().toString().replace("Qty: ", "");
                    if (Integer.valueOf(qty) == 10) {
                        Toast.makeText(CheckOutActivity.this, "limit reached", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OrderItem orderItem = order.get(orderData.get(position));
                    orderItem.setQuantity(String.valueOf(Integer.valueOf(qty)+1));
                    order.put(orderItem.getId(), orderItem);
                    sTotalPrice = new HashMap<>();
                    notifyDataSetChanged();
                    Log.i("TAG", "plusButton click");
                }
            });
            if (Integer.valueOf(orderItem.getQuantity()) > 1) {
                Log.i("TAG", "qty  condition matched");
                for (int i = 1; i <= Integer.valueOf(orderItem.getQuantity()); i++) {
                    price = price + Integer.valueOf(orderItem.getPrice());
                }
                viewHolder.price.setText("Total:" + price + " SAR");
                Log.i("TAG", "sTotalPrice" + sTotalPrice + " adding "+ price);
                if (!sTotalPrice.containsKey(orderItem.getId())) {
                    sTotalPrice.put(orderItem.getId(), price);
                }
            } else {
                viewHolder.price.setText("Total:" + orderItem.getPrice());
                if (!sTotalPrice.containsKey(orderItem.getId())) {
                    sTotalPrice.put(orderItem.getId(), Integer.valueOf(orderItem.getPrice()));
                }
                Log.i("TAG", " else sTotalPrice" + sTotalPrice + " adding "+ Integer.valueOf(orderItem.getPrice()));
            }
            Picasso.with(AppGlobals.getContext())
                    .load(orderItem.getImageUrl())
                    .resize(150, 150)
                    .centerCrop()
                    .into(viewHolder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                        }
                    });

            return convertView;
        }

        @Override
        public int getCount() {
            return orderData.size();
        }
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
                        AppGlobals.alertDialog(CheckOutActivity.this, "Request Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        System.out.println(request.getResponseURL());
                        Toast.makeText(getApplicationContext(), "Your request has been received", Toast.LENGTH_SHORT).show();
                        LaundryCategoriesActivity.getInstance().finish();
                        finish();
                        CheckoutStageTwo.getInstance().finish();
                }
        }
    }

    private void orderRequest(JSONArray itemsQuantity,String pickupTime,  String dropTime, String laundryType) {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%slaundry/request", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        Log.i("TAG", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(orderRequestData(itemsQuantity, pickupTime,  dropTime, laundryType));
        WebServiceHelpers.showProgressDialog(CheckOutActivity.this, "Sending your laundry request..");
    }

    private String orderRequestData(JSONArray itemsQuantity,String pickUpTime,  String dropTime,
                                    String laundryType) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", sAddressId);
            jsonObject.put("pickup_time", pickUpTime);
            jsonObject.put("drop_time", dropTime);
            jsonObject.put("laundry_type", laundryType);
            jsonObject.put("service_items", itemsQuantity);
            jsonObject.put("approved_for_processing", "False");
            jsonObject.put("service_done", "False");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("TAG", "DATA "+ jsonObject.toString());
        return jsonObject.toString();
    }

    private class ViewHolder {
        public TextView name;
        public TextView quantity;
        public TextView price;
        public ImageView imageView;
        public ImageButton plusButton;
        public ImageButton minusButton;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);

    }
}
