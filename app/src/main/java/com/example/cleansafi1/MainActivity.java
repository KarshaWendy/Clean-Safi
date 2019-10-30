package com.example.cleansafi1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cleansafi1.account.CodeConfirmationActivity;
import com.example.cleansafi1.account.LoginActivity;
import com.example.cleansafi1.account.ResetPassword;
import com.example.cleansafi1.account.UpdateProfile;
import com.example.cleansafi1.laundry.LaundryCategoriesActivity;
import com.example.cleansafi1.utils.AppGlobals;
import com.example.cleansafi1.utils.BitmapWithCharacter;
import com.example.cleansafi1.utils.HeadingTextView;
import com.byteshaft.requests.HttpRequest;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HttpRequest.OnReadyStateChangeListener {

    public static MainActivity sInstance;
    private View header;
    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private TextView mName;
    private TextView mEmail;
    private NavigationView navigationView;
    private ProgressDialog progress;
    private HeadingTextView laundryText;
    private String mToken;
    private JSONArray array;
    private CustomAdapter listAdapter;
    private boolean onResumeCalled = false;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        setContentView(R.layout.activity_main);
        laundryText = (HeadingTextView) findViewById(R.id.laundry_text);
        AppGlobals.sActivity = MainActivity.this;
        mToken = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN);
        Log.i("TAG", "" + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Hello there");
                startActivity(new Intent(MainActivity.this, LaundryCategoriesActivity.class));
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.canScrollVertically(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
    }

    private void laundryRequestDetails() {
        if (!onResumeCalled) {
            progress = ProgressDialog.show(this, "Please wait..",
                    "Getting data", true);
        }
        HttpRequest mRequest = new HttpRequest(AppGlobals.getContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.open("GET", AppGlobals.LAUNDRY_REQUEST_URL);
        mRequest.setRequestHeader("Authorization", "Token " + mToken);
        mRequest.send();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onResumeCalled = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
        if (!AppGlobals.isUserActive() && !AppGlobals.isUserLoggedIn()) {
            laundryText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            laundryText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            laundryRequestDetails();
        }
        MenuItem login, logout, active, addLocation;
        Menu menu = navigationView.getMenu();
        if (!AppGlobals.isUserLoggedIn()) {
            login = menu.findItem(R.id.login);
            logout = menu.findItem(R.id.nav_logout);
            login.setVisible(true);
            logout.setVisible(false);
        } else {
            login = menu.findItem(R.id.login);
            active = menu.findItem(R.id.active);
            if (!AppGlobals.isUserActive()) {
                active.setVisible(true);
            } else {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    addLocation = menu.findItem(R.id.add_address);
                    addLocation.setVisible(true);
                }
                active.setVisible(false);
            }
            logout = menu.findItem(R.id.nav_logout);
            login.setVisible(false);
            logout.setVisible(true);
        }

        mName = (TextView) header.findViewById(R.id.nav_user_name);
        mEmail = (TextView) header.findViewById(R.id.nav_user_email);
        mName.setTypeface(AppGlobals.typefaceBold);
        mEmail.setTypeface(AppGlobals.typefaceNormal);
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME).equals("")
                && AppGlobals.isUserActive()) {
            String simpleName = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME);
            String firstUpperCaseName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
            mName.setText(firstUpperCaseName);
        } else {
            mName.setText("username");
        }
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL).equals("")) {
            mEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
        } else {
            mEmail.setText("abc@xyz.com");
        }
        CircularImageView circularImageView = (CircularImageView) header.findViewById(R.id.imageView);
        if (AppGlobals.isUserLoggedIn()) {
            final Resources res = getResources();
            int[] array = getResources().getIntArray(R.array.letter_tile_colors);
            final BitmapWithCharacter tileProvider = new BitmapWithCharacter();
            final Bitmap letterTile = tileProvider.getLetterTile(AppGlobals.
                            getStringFromSharedPreferences(AppGlobals.KEY_FULL_NAME),
                    String.valueOf(array[new Random().nextInt(array.length)]), 100, 100);
            circularImageView.setImageBitmap(letterTile);
        }

        if (!AppGlobals.isUserActive() && !AppGlobals.dialogCancel && AppGlobals.isUserLoggedIn()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("User not active");
            alertDialogBuilder.setMessage("please activate your account")
                    .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), CodeConfirmationActivity.class));
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    AppGlobals.dialogCancel = true;
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.add_address:
                startActivity(new Intent(getApplicationContext(), AddressesActivity.class));
                break;
            case R.id.request_laundry:
                startActivity(new Intent(getApplicationContext(), LaundryCategoriesActivity.class));
                break;
            case R.id.login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.active:
                startActivity(new Intent(getApplicationContext(), CodeConfirmationActivity.class));
                break;
            case R.id.nav_update_profile:
                startActivity(new Intent(getApplicationContext(), UpdateProfile.class));
                break;
            case R.id.nav_reset_password:
                startActivity(new Intent(getApplicationContext(), ResetPassword.class));
                break;
            case R.id.nav_logout:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Confirmation");
                alertDialogBuilder.setMessage("Do you really want to logout?").setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences sharedpreferences = AppGlobals.getPreferenceManager();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.clear();
                                editor.commit();
                                AppGlobals.logout = true;
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            default:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                if (!onResumeCalled && progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
                System.out.println("Ok kro :   " + request.getResponseText());
                try {
                    array = new JSONArray(request.getResponseText());
                    listAdapter = new CustomAdapter(array);
                    mRecyclerView.setAdapter(listAdapter);
                    if (array.length() < 1) {
                        laundryText.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    static class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
            RecyclerView.OnItemTouchListener {

        private JSONArray items;
        private CustomView viewHolder;
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;

        public interface OnItemClickListener {
            void onItem(String item);
        }

        public CustomAdapter(JSONArray categories, Context context,
                             OnItemClickListener listener) {
            this.items = categories;
            mListener = listener;
            mGestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }
                    });
        }

        public CustomAdapter(JSONArray categories) {
//            mAddresses = addresses;
//            mCreatedDate = createdDate;
//            mIsDone = isDone;
            items = categories;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.delegate_laundry_items, parent, false);
            viewHolder = new CustomView(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            try {
                JSONObject jsonObject = items.getJSONObject(position);
                Log.i("Tag", jsonObject.toString());
                JSONObject addressObject = jsonObject.getJSONObject("address");
                JSONArray itemsArray = jsonObject.getJSONArray("service_items");
                viewHolder.address.setText("Address: " + addressObject.getString("name"));
                viewHolder.address.setTypeface(AppGlobals.typefaceBold);
                boolean isDone = jsonObject.getBoolean("service_done");
                viewHolder.status.setTypeface(AppGlobals.typefaceNormal);
                if (isDone) {
                    viewHolder.status.setText("Status: Done");
                    viewHolder.status.setTextColor(Color.parseColor("#009900"));
                } else {
                    viewHolder.status.setText("Status: Pending");
                    viewHolder.status.setTextColor(Color.parseColor("#b20000"));
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemDetails = itemsArray.getJSONObject(i);
                    stringBuilder.append(itemDetails.getString("name"));
                    stringBuilder.append(" (");
                    stringBuilder.append(itemDetails.getString("quantity") +")");
                    if (i+1 < itemsArray.length()) {
                        stringBuilder.append(" , ");
                    }
                    viewHolder.itemName.setTypeface(AppGlobals.typefaceNormal);
                    viewHolder.itemName.setText(stringBuilder.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return items.length();
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
//                mListener.onItem(items.get(rv.getChildPosition(childView)));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        /* Member class that extends RecyclerView.ViewHolder allows us to access the elements inside
           xml it takes view in constructor
         */
        public class CustomView extends RecyclerView.ViewHolder {
            public TextView address;
            public TextView textItem;
            public TextView itemName;
            public TextView status;

            public CustomView(View itemView) {
                super(itemView);
                address = (TextView) itemView.findViewById(R.id.tv_address);
                textItem = (TextView) itemView.findViewById(R.id.tv_item);
                textItem.setTypeface(AppGlobals.typefaceBold);
                itemName = (TextView) itemView.findViewById(R.id.tv_item_name);
                status = (TextView) itemView.findViewById(R.id.tv_status);
            }
        }
    }

}
