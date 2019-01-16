package com.companiontracker;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.companiontracker.asynctasks.GetAddressAsyncTask;
import com.companiontracker.interfaces.GetAddressInterface;
import com.companiontracker.model.InvitePeopleModel;
import com.companiontracker.model.LocationModel;
import com.companiontracker.permissions.PermissionUtil;
import com.companiontracker.permissions.PermissionsActivity;
import com.companiontracker.utility.AsyncTaskTools;
import com.companiontracker.utility.MarkerAnimationHelper;
import com.companiontracker.utility.Networking;
import com.companiontracker.utility.Preferences;
import com.companiontracker.viewholder.DashboardViewHolder;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.companiontracker.R.id.map;

public class DashboardActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>, View.OnClickListener, GetAddressInterface {

    private Toolbar mainToolbar;
    private NavigationView navigationView;
    private GoogleMap mMap;

    private ProgressDialog progressDialog;
    private TextView mToolbarTitleTextView, mNavUserNameTextView, mNavUserEmailTextView,
            mUserNameTextView, mUserAddressTextView, mConnectionsTextView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private RelativeLayout mDragViewRelativeLayout;
    private LinearLayout mUserDetailsRelativeLayout;
    private ProgressBar mDashboardProgressBar, connectionsProgressBar;
    private ImageView mFriendPhotoImageView;
    private CircleImageView mProfileImage;
    private BottomSheetBehavior behavior;

    private FirebaseDatabase database;
    private FirebaseRecyclerAdapter<InvitePeopleModel, DashboardViewHolder> mAdapter;

    private InvitePeopleModel userModel;

    private Preferences mPreferences;
    private SupportMapFragment mapFragment;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private Bitmap marker_image;
    private Marker marker;
    private LatLng frndLatLng, currentLocation;

    private GetAddressAsyncTask addressAsyncTask;

    public static final int EDIT_USER_CALLBACK = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        configureToolbar();

        //build location Api's
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        mDashboardProgressBar = (ProgressBar) findViewById(R.id.dashboard_progressBar);
        connectionsProgressBar = (ProgressBar) findViewById(R.id.connections_progressBar);
        mConnectionsTextView = (TextView) findViewById(R.id.connections_textView);
        mDragViewRelativeLayout = (RelativeLayout) findViewById(R.id.dragView_relativeLayout);
        mFriendPhotoImageView = (ImageView) findViewById(R.id.friendPhoto_imageView);
        mUserDetailsRelativeLayout = (LinearLayout) findViewById(R.id.userDetails_linearLayout);
        mUserNameTextView = (TextView) findViewById(R.id.userName_textView);
        mUserAddressTextView = (TextView) findViewById(R.id.userAddress_textView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //DrawerLayout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //NavigationView
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        mNavUserNameTextView = (TextView) header.findViewById(R.id.user_name_textView);
        mNavUserEmailTextView = (TextView) header.findViewById(R.id.user_email_textView);
        mProfileImage = (CircleImageView) header.findViewById(R.id.profile_image);

        //MapView
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        //RecyclerView
        mRecycler = (RecyclerView) findViewById(R.id.connections_recyclerView);
        mRecycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        //Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        //Creating Preferences object
        mPreferences = new Preferences();

        showUserDetails();

        //Creating marker image
        marker_image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_location);

        mDragViewRelativeLayout.setOnClickListener(this);

        View bottomSheet = findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:

                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mDragViewRelativeLayout.setVisibility(View.GONE);
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mDragViewRelativeLayout.setVisibility(View.VISIBLE);
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("BottomSheetCallback", "slideOffset: " + slideOffset);
            }
        });

        //pop up LocationSettings
        checkLocationSettings();

        //Get user connections and show in recycler view
        getConnections();

        //locate current location and update to fireBase in background
        startService(new Intent(DashboardActivity.this, LocationService.class));

    }

    private void showUserDetails() {
        //Assigning username and email to NavigationView text
        mNavUserNameTextView.setText(mPreferences.getFirstName(this));
        mNavUserEmailTextView.setText(mPreferences.getEmail(this));
        if (!mPreferences.getImageUrl(this).equals("")) {
            Picasso.with(this).load(mPreferences.getImageUrl(this)).fit().centerCrop().into(mProfileImage);

        }
    }

    private void getAddressFromLatLng(LatLng data) {

        if (Networking.isNetworkAvailable(this)) {
            addressAsyncTask = new GetAddressAsyncTask(this);
            addressAsyncTask.delegate = this;
            AsyncTaskTools.execute(addressAsyncTask, data);
        } else {
            showDialog_singleButton(getResources().getString(R.string.no_connection));
        }
    }

    //Receives intent from LocationService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO receives current location updates from loactionService.class
            // Get extra data included in the Intent
            //   Toast.makeText(DashboardActivity.this, "BROADCAST", Toast.LENGTH_SHORT).show();
            Log.e("receiver", "Bro success");
            //Latitude //Longitude

            Double Latitude = intent.getDoubleExtra("Latitude", 0);
            Double Longitude = intent.getDoubleExtra("Longitude", 0);
            currentLocation = new LatLng(Latitude, Longitude);

        }
    };

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    //Get friend location latlng from firebase and update Ui
    private void locationUpdate(String key, final String name) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                if (dataSnapshot.exists()) {
                    LocationModel userData = dataSnapshot.getValue(LocationModel.class);

                    Log.d("", "LocationUpdate: " + userData.lat + "," + userData.lng);

                    frndLatLng = new LatLng(userData.lat, userData.lng);

                    addMarkersToMap(name, frndLatLng);
                    getAddressFromLatLng(frndLatLng);


                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("location");
        myRef.child(key).addValueEventListener(postListener);
    }


    private void addMarkersToMap(String name, LatLng latlng) {

        //clear map and add new marker to map

        mMap.clear();

        marker = mMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title(name)
                .icon(BitmapDescriptorFactory.fromBitmap(marker_image))
                .flat(false));

        LatLngBounds bounds = new LatLngBounds.Builder().include(latlng).build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
        new MarkerAnimationHelper(mMap).animateMarker(marker, latlng, latlng);

    }

    private void configureToolbar() {
        mainToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mainToolbar != null) {
            mToolbarTitleTextView = (TextView) mainToolbar.findViewById(R.id.toolbar_title_textView);
            mToolbarTitleTextView.setText(getString(R.string.app_name));
            setSupportActionBar(mainToolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

    }

    private void updateBottomUi(boolean value) {
        mFriendPhotoImageView.setVisibility(value ? View.GONE : View.VISIBLE);
        mUserDetailsRelativeLayout.setVisibility(value ? View.GONE : View.VISIBLE);
        mDashboardProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void getConnections() {

        //show progress bar
        updateBottomUi(true);

        connectionsProgressBar.setVisibility(View.VISIBLE);
        mConnectionsTextView.setVisibility(View.VISIBLE);

        final String myKey = mPreferences.getUserKey(DashboardActivity.this);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("my_connections");
        Query postsQuery = myRef.child(myKey);
        mAdapter = new FirebaseRecyclerAdapter<InvitePeopleModel, DashboardViewHolder>(InvitePeopleModel.class,
                R.layout.item_dashboard,
                DashboardViewHolder.class, postsQuery) {

            @Override
            protected void populateViewHolder(DashboardViewHolder viewHolder, final InvitePeopleModel model,
                                              int position) {

                final DatabaseReference postRef = getRef(position);
                final String friend_key = postRef.getKey();

                if (model != null) {

                    viewHolder.bindToNotification(DashboardActivity.this, model,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //RecyclerView item click
                                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    updateUi(model, friend_key);

                                }
                            });
                } else {
                    Toast.makeText(DashboardActivity.this, "No Connections", Toast.LENGTH_SHORT).show();
                }

            }
        };

        mRecycler.setAdapter(mAdapter);

        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    mConnectionsTextView.setText("No companions. Please invite a companion!");
                    mConnectionsTextView.setVisibility(View.VISIBLE);
                    connectionsProgressBar.setVisibility(View.GONE);

                    mFriendPhotoImageView.setVisibility(View.GONE);
                    mUserDetailsRelativeLayout.setVisibility(View.GONE);
                    mDashboardProgressBar.setVisibility(View.GONE);
                } else {
                    connectionsProgressBar.setVisibility(View.GONE);
                    mConnectionsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateUi(InvitePeopleModel model, String userTrackingKey) {

        //clear map and add new marker to map
        mMap.clear();

        mUserNameTextView.setText(model.user_name);
        mUserAddressTextView.setText("");
        locationUpdate(userTrackingKey, model.user_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       // int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    boolean close = true;
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_invite) {
            Intent i = new Intent(this, InviteActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_notifications) {

            Intent i = new Intent(this, NotificationActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_my_connections) {
            Intent i = new Intent(this, MyConnectionsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent editUserIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(editUserIntent,EDIT_USER_CALLBACK);
        } else if (id == R.id.nav_logout) {

            if (Networking.isNetworkAvailable(this)) {
                new Preferences().saveUserKey(this, "");
                new Preferences().deleteUserDetails(this);
                Intent loginIntent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(loginIntent);
                finish();
                close = true;
            }

            else {
                close = false;
                showSnackBar(getResources().getString(R.string.no_connection));

            }

        } else if (id == R.id.nav_share) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Here is the share content body";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        } else if (id == R.id.nav_send) {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
            }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        //Ask user for location permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            new PermissionsActivity(DashboardActivity.this).locationPermission();
        }

        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i("", "All location settings are satisfied.");
                // permissionsActivity.showCamera();
                //startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("", "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(DashboardActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i("", "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i("", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //get current location refer example locationSetting
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
       /* if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }*/

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        IntentFilter filter = new IntentFilter(LocationService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.dragView_relativeLayout:
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                break;

            case R.id.fab:

                if (frndLatLng != null){
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(frndLatLng, 50);
                    mMap.animateCamera(cameraUpdate);
                }

                // mMap.clear();
             /*   mMap.addPolyline((new PolylineOptions())
                        .add(currentLocation, frndLatLng));*/
                break;

            default:
                break;
        }

    }

    @Override
    public void onGetAddressPostExecute(String address) {
        if (!TextUtils.isEmpty(address)) {
            mUserAddressTextView.setText(address);
            //show progress bar

        }
        updateBottomUi(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION) {

            if (PermissionUtil.verifyPermissions(grantResults)) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                //All required permissions have been granted, display contacts fragment.
                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== EDIT_USER_CALLBACK ){
            showUserDetails();

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

}
