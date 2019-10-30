package com.example.cleansafi1;

import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.WebServiceHelpers;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;


public class PickLDropLaundryActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, CompoundButton.OnCheckedChangeListener, HttpRequest.OnErrorListener, HttpRequest.OnReadyStateChangeListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private Marker currLocationMarker;
    private Double longitude;
    private Double latitude;
    private int counter = 0;
    private String city = "";
    private String address = "";
    private String zipCode = "";
    private String houseNumber = "";
    private EditText addressTitle;
    private EditText cityEditText;
    private EditText streetEditText;
    private EditText zipCodeEditText;
    private EditText houseNumberEditText;
    private Animation slideDown;
    private Animation slideUp;
    private RelativeLayout relativeLayout;
    private TextView switchTextView;
    private Switch deliverySwitch;
    private boolean switchOn = true;
    private EditText deliveryHouseNumber;
    private EditText deliveryCityEditText;
    private EditText deliveryStreetEditText;
    private EditText deliveryZipCodeEditText;
    private LatLng pickUpLatLong;
    private LatLng dropLatLong;
    private MenuItem menuItem;
    private boolean updateMode = false;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_laundry);
        setTitle("Choose Address");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.colorPrimaryDark)));
        addressTitle =  findViewById(R.id.address_title);
        cityEditText =  findViewById(R.id.city_address);
        houseNumberEditText =  findViewById(R.id.house_number);
        streetEditText =  findViewById(R.id.street);
        zipCodeEditText =  findViewById(R.id.zip_code);
        relativeLayout =  findViewById(R.id.layout_delivery);
        deliverySwitch =  findViewById(R.id.same_delivery_address);
        deliveryCityEditText =  findViewById(R.id.delivery_city_address);
        deliveryStreetEditText =  findViewById(R.id.delivery_street);
        deliveryZipCodeEditText =  findViewById(R.id.delivery_zip_code);
        switchTextView =  findViewById(R.id.switch_text);
        deliveryHouseNumber =  findViewById(R.id.delivery_house_number);
        addressTitle.setTypeface(AppGlobals.typefaceBold);
        cityEditText.setTypeface(AppGlobals.typefaceNormal);
        houseNumberEditText.setTypeface(AppGlobals.typefaceNormal);
        streetEditText.setTypeface(AppGlobals.typefaceNormal);
        zipCodeEditText.setTypeface(AppGlobals.typefaceNormal);
        deliverySwitch.setTypeface(AppGlobals.typefaceNormal);
        deliveryCityEditText.setTypeface(AppGlobals.typefaceNormal);
        deliveryStreetEditText.setTypeface(AppGlobals.typefaceNormal);
        deliveryZipCodeEditText.setTypeface(AppGlobals.typefaceNormal);
        switchTextView.setTypeface(AppGlobals.typefaceNormal);
        deliveryHouseNumber.setTypeface(AppGlobals.typefaceNormal);
        if (getIntent().getExtras() != null) {
            id = Integer.parseInt(getIntent().getStringExtra("id"));
            addressTitle.setText(getIntent().getStringExtra("title"));
            cityEditText.setText(getIntent().getStringExtra("city"));
            streetEditText.setText(getIntent().getStringExtra("street"));
            houseNumberEditText.setText(getIntent().getStringExtra("house"));
            zipCodeEditText.setText(getIntent().getStringExtra("zip"));
            String loc = getIntent().getStringExtra("pick_location");
            String[] pickDrop = loc.split("\\|");
            String removeLatlng = pickDrop[0].replaceAll("lat/lng: ", "").replace("(", "").replace(")", "");
            String[] latLng = removeLatlng.split(",");
            final double latitude = Double.parseDouble(latLng[0]);
            final double longitude = Double.parseDouble(latLng[1]);
            pickUpLatLong = new LatLng(latitude, longitude);
            boolean sameDropLocation = getIntent().getBooleanExtra("boolean", false);
            switchOn = sameDropLocation;
            deliverySwitch.setChecked(sameDropLocation);
            if (!sameDropLocation) {
                deliveryCityEditText.setText(getIntent().getStringExtra("drop_city"));
                deliveryStreetEditText.setText(getIntent().getStringExtra("drop_street"));
                deliveryHouseNumber.setText(getIntent().getStringExtra("drop_house"));
                deliveryZipCodeEditText.setText(getIntent().getStringExtra("drop_zip"));
                String replaceLatLng = pickDrop[1].replaceAll("lat/lng: ", "").replace("(", "").replace(")", "");
                ;
                String[] dropLatLng = replaceLatLng.split(",");
                final double dropLatitude = Double.parseDouble(dropLatLng[0]);
                final double dropLongitude = Double.parseDouble(dropLatLng[1]);
                dropLatLong = new LatLng(dropLatitude, dropLongitude);
                Log.i("TAG", "drop lat lng "+ dropLatLong);
                relativeLayout.setVisibility(View.VISIBLE);
                relativeLayout.setAnimation(slideDown);
            }
            updateMode = true;
        }
        deliverySwitch.setOnCheckedChangeListener(this);
        slideDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        slideUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean validateFields() {
        if (deliverySwitch.isEnabled()) {
            if (addressTitle.getText().toString().trim().isEmpty() ||
                    cityEditText.getText().toString().trim().isEmpty() ||
                    streetEditText.getText().toString().trim().isEmpty() ||
                    zipCodeEditText.getText().toString().trim().isEmpty() ||
                    houseNumberEditText.getText().toString().trim().isEmpty()
                    ) {
                Log.i("TAG", "first");
                Toast.makeText(this, "All fields are Required", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (addressTitle.getText().toString().trim().isEmpty() ||
                    cityEditText.getText().toString().trim().isEmpty() ||
                    streetEditText.getText().toString().trim().isEmpty() ||
                    zipCodeEditText.getText().toString().trim().isEmpty() ||
                    deliveryCityEditText.getText().toString().trim().isEmpty() ||
                    deliveryStreetEditText.getText().toString().trim().isEmpty() ||
                    deliveryZipCodeEditText.getText().toString().trim().isEmpty() ||
                    houseNumberEditText.getText().toString().trim().isEmpty() ||
                    deliveryHouseNumber.getText().toString().trim().isEmpty()) {
                Log.i("TAG", "second");
                Toast.makeText(this, "All fields are Required", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (dropLatLong == null && !switchOn) {
            Toast.makeText(this, "please select drop location on map", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pickUpLatLong == null) {
            Toast.makeText(this, "please select pickup location on map", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_address_menu, menu);
        menuItem = menu.findItem(R.id.save_address);
        if (getIntent().getExtras() != null) {
            menuItem.setTitle("Update");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_address) {
            if (validateFields()) {
                JSONObject jsonObject = new JSONObject();
                String location = "";
                if (switchOn) {
                    location = pickUpLatLong.toString();
                } else {
                    location = pickUpLatLong + "|" + dropLatLong;
                }
                Log.i("TAG", "Location to be added " + location);
                try {
                    jsonObject.put("location", location);
                    jsonObject.put("name", addressTitle.getText().toString());
                    jsonObject.put("pickup_city", cityEditText.getText().toString());
                    jsonObject.put("pickup_house_number", houseNumberEditText.getText().toString());
                    jsonObject.put("pickup_street", streetEditText.getText().toString());
                    jsonObject.put("pickup_zip", zipCodeEditText.getText().toString());
                    if (!switchOn) {
                        jsonObject.put("drop_city", deliveryCityEditText.getText().toString());
                        jsonObject.put("drop_house_number", deliveryHouseNumber.getText().toString());
                        jsonObject.put("drop_street", deliveryStreetEditText.getText().toString());
                        jsonObject.put("drop_zip", deliveryZipCodeEditText.getText().toString());
                        jsonObject.put("drop_on_pickup_location", "false");
                    }
                    Log.i("TAG", "Data " + jsonObject.toString());
                    addLocation(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addLocation(JSONObject jsonObject) {
        HttpRequest request = new HttpRequest(AppGlobals.getContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        String url;
        String method;
        if (updateMode) {
            method = "PUT";
            url = String.format("%suser/addresses/%s", AppGlobals.BASE_URL, id);
        } else {
            method = "POST";
            url = String.format("%suser/addresses", AppGlobals.BASE_URL);
        }
        request.open(method, url);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(jsonObject.toString());
        WebServiceHelpers.showProgressDialog(this, "Processing");
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        WebServiceHelpers.dismissProgressDialog();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                WebServiceHelpers.dismissProgressDialog();
                Log.i("TAG", "address " + request.getStatus());
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_CREATED:
                        finish();
                        Toast.makeText(this, "Address added!", Toast.LENGTH_SHORT).show();
                        AddressesActivity.getLocationData();
                        break;
                    case HttpURLConnection.HTTP_OK:
                        if (updateMode)
                        finish();
                        Toast.makeText(this, "Address update!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();

                }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) {
            switchTextView.setText(getResources().getString(R.string.delivery_address_change));
            switchOn = false;
            relativeLayout.setVisibility(View.VISIBLE);
            relativeLayout.setAnimation(slideDown);
            deliveryCityEditText.setText(cityEditText.getText().toString());
            mMap.clear();
            Toast.makeText(this, "please select drop location on map", Toast.LENGTH_SHORT).show();
        } else {
            switchTextView.setText(getResources().getString(R.string.delivery_address_same_as_pick_address));
            switchOn = true;
            mMap.addMarker(new MarkerOptions()
                    .position(pickUpLatLong).title("pickup").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
            relativeLayout.setVisibility(View.GONE);
            relativeLayout.setAnimation(slideUp);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean sameDropLocation = getIntent().getBooleanExtra("boolean", false);
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                String title;
                if (switchOn) {
                    title = "pickup";
                } else {
                    title = "drop";
                }
                mMap.addMarker(new MarkerOptions()
                        .position(latLng).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                );
                longitude = latLng.longitude;
                latitude = latLng.latitude;
                if (switchOn) {
                    pickUpLatLong = new LatLng(latitude, longitude);
                } else {
                    dropLatLong = new LatLng(latitude, longitude);
                    Log.i("TAG", "dropLatlong" + dropLatLong);
                }
                getCompleteAddressString(latitude, longitude);
            }
        });
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (getIntent().getExtras() != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(pickUpLatLong).title("pickup")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            );
            if (!sameDropLocation) {
                mMap.addMarker(new MarkerOptions()
                        .position(dropLatLong).title("drop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                );
            }
        }
    }


    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                address = addresses.get(0).getAddressLine(0);
                if (address != null && !address.trim().isEmpty()) {
                    if (switchOn) {
                        cityEditText.setText(address);
                    } else {
                        deliveryCityEditText.setText(address);
                    }
                }
                city = addresses.get(0).getLocality();
                if (city != null && !city.trim().isEmpty()) {
                    if (switchOn) {
                        cityEditText.setText(address + " " + city);
                    } else {
                        deliveryCityEditText.setText(address + " " + city);
                    }
                }
                zipCode = addresses.get(0).getPostalCode();
                if (zipCode != null && !zipCode.trim().isEmpty()) {
                    if (switchOn)
                        zipCodeEditText.setText(zipCode);
                    else deliveryZipCodeEditText.setText(zipCode);
                }
                Log.i("TAG", "address " + address + city + zipCode);
            } else {
                Log.w("Current location", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current location ", "Cannot get Address!");
        }
        return strAdd;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currLocationMarker = mMap.addMarker(markerOptions);
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getIntent().getExtras() == null) {
            if (currLocationMarker != null) {
                currLocationMarker.remove();
            }
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currLocationMarker = mMap.addMarker(markerOptions);

            if (counter < 1) {
                longitude = latLng.longitude;
                latitude = latLng.latitude;
                getCompleteAddressString(latitude, longitude);
            }
            counter++;
            //zoom to current position:
            if (switchOn) {
                pickUpLatLong = new LatLng(latitude, longitude);
            } else {
                dropLatLong = new LatLng(latitude, longitude);
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(16).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pickUpLatLong).zoom(10).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }
}
