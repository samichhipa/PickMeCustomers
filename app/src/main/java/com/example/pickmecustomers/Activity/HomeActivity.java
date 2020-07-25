package com.example.pickmecustomers.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.example.pickmecustomers.Fragments.BottomSheetPickUpFragment;
import com.example.pickmecustomers.Helper.CustomInfoHelper;
import com.example.pickmecustomers.Model.Common;
import com.example.pickmecustomers.Model.Customers;
import com.example.pickmecustomers.Model.Drivers;
import com.example.pickmecustomers.Notification.CustomerTokens;
import com.example.pickmecustomers.Notification.Data;
import com.example.pickmecustomers.Notification.DriverTokens;
import com.example.pickmecustomers.Notification.IFCMService;
import com.example.pickmecustomers.Notification.MyResponse;
import com.example.pickmecustomers.Notification.Sender;
import com.example.pickmecustomers.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pickmecustomers.Model.Common.driver_id;
import static com.example.pickmecustomers.Model.Common.isDriverFound;
import static com.example.pickmecustomers.Model.Common.lastLocation;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, ValueEventListener {


    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    SupportMapFragment mapFragment;

    private static int MY_PERMISSION_REQ_CODE = 1000;
    private static int PLAY_SERVICES_RES_REQUEST = 10001;

    LocationRequest locationRequest;

    TextView txt_username, txt_phone;


    Marker currentMarker, destination_marker;


    AutocompleteFilter typeFilter;

    BottomSheetDialogFragment bottomSheetDialogFragment;
    Button pickup_Btn;

    //cars
    ImageView economy_car, business_car;
    boolean isEconomySelected = true, isBusinessSelected = false;


    int radius = 1; //1 km
    int distance = 3; //3 km
    private static final int LIMIT = 3;

    IFCMService ifcmService;


    GeoFire geoFire;
    DatabaseReference customer_ref;
    private GoogleMap mMap;


    //Presence System//
    DatabaseReference driverAvailable;

    String source_txt, destination_txt;


    AutocompleteSupportFragment source_autocompleteFragment;
    AutocompleteSupportFragment destination_autocompleteFragment;

    MapRipple mapRipple;


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            driver_id = "";
            isDriverFound = false;

            pickup_Btn.setText("PickUp Request");
            pickup_Btn.setEnabled(true);

            if (mapRipple.isAnimationRunning()) {

                mapRipple.stopRippleMapAnimation();
            }

        }
    };


    BroadcastReceiver dropoff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            driver_id = "";
            isDriverFound = false;

            pickup_Btn.setText("PickUp Request");
            pickup_Btn.setEnabled(true);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("CANCEL_PICKUP"));
        LocalBroadcastManager.getInstance(this).registerReceiver(dropoff, new IntentFilter("DROPOFF"));

        ifcmService = Common.getFCMService();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(HomeActivity.this);
        navigationView.getMenu().getItem(0).setChecked(true);


        View headerView = navigationView.getHeaderView(0);
        txt_username = headerView.findViewById(R.id.header_customer_name);
        txt_phone = headerView.findViewById(R.id.header_customer_phone);

        getCurrentUser();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);


        customer_ref = FirebaseDatabase.getInstance().getReference().child("CustomersLocation");
        geoFire = new GeoFire(customer_ref);

        pickup_Btn = findViewById(R.id.pickup_req_Btn);
//        bottomSheetDialogFragment = BottomSheetPickUpFragment.getInstance("This is New Bottom Sheet");


        economy_car = findViewById(R.id.economy_car);
        business_car = findViewById(R.id.business_car);

        if (isEconomySelected) {

            economy_car.setImageResource(R.drawable.selected_economy);

            // mMap.clear();
            //loadAllAvailableDrivers(new LatLng(Common.lastLocation.getLatitude(),Common.lastLocation.getLongitude()));

            Toast.makeText(HomeActivity.this, "EconomyClass", Toast.LENGTH_LONG).show();

        }

        economy_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!isEconomySelected) {
                    economy_car.setImageResource(R.drawable.selected_economy);
                    business_car.setImageResource(R.drawable.not_selected_business);
                    isEconomySelected = true;
                    Toast.makeText(HomeActivity.this, "EconomyClass", Toast.LENGTH_LONG).show();

                }
                mMap.clear();

                if (driverAvailable != null) {
                    driverAvailable.removeEventListener(HomeActivity.this);
                }

                driverAvailable = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(isEconomySelected ? "Economical" : "Business");
                driverAvailable.addValueEventListener(HomeActivity.this);


                loadAllAvailableDrivers(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));


            }
        });
        business_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (isEconomySelected) {
                    economy_car.setImageResource(R.drawable.not_selected_economy);
                    business_car.setImageResource(R.drawable.selected_business);
                    Toast.makeText(HomeActivity.this, "BusinessClass", Toast.LENGTH_LONG).show();
                    isEconomySelected = false;
                }

                mMap.clear();
                if (driverAvailable != null) {
                    driverAvailable.removeEventListener(HomeActivity.this);
                }

                driverAvailable = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(isEconomySelected ? "Economical" : "Business");
                driverAvailable.addValueEventListener(HomeActivity.this);

                loadAllAvailableDrivers(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));


            }
        });

        pickup_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isDriverFound) {

                    PickUpRequest(FirebaseAuth.getInstance().getCurrentUser().getUid());

                } else {

                    pickup_Btn.setEnabled(false);
                    sendRequestToDriver(driver_id);


                }

            }
        });

        //  getPermission();
        Places.initialize(getApplicationContext(), "AIzaSyDjX6eyW9a3zG4Lk8hxpgl3wuNakeYkeYo");


        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();


        source_autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.source_fragment);

        destination_autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_fragment);

        source_autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));

        destination_autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));


        source_autocompleteFragment.setOnPlaceSelectedListener(new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {

                source_txt = place.getName();
                source_txt = source_txt.replace(" ", "");

                mMap.clear();

                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin))
                        .title("PickUp Here"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));


            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        destination_autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                destination_txt = place.getName();
                destination_txt = destination_txt.replace(" ", "");

                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));


                bottomSheetDialogFragment = BottomSheetPickUpFragment.getInstance(source_txt, destination_txt, false);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());


            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
        setUpLocation();

    }

    private void getCurrentUser() {

        FirebaseDatabase.getInstance().getReference().child("Customers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Customers customers = snapshot.getValue(Customers.class);

                        txt_username.setText(customers.getName());
                        txt_phone.setText(customers.getCustomer_phone());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void sendRequestToDriver(final String driver_id) {


        DatabaseReference driver_ref = FirebaseDatabase.getInstance().getReference().child("DriverTokens");
        driver_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {


                    DriverTokens driverTokens = snap.getValue(DriverTokens.class);

                    String id = driverTokens.getDriver_id();

                    if (driver_id.equals(id)) {
                        String driver_token = driverTokens.getToken_id();

                        Toast.makeText(HomeActivity.this, driver_token, Toast.LENGTH_SHORT).show();

                        //make notification Payload//

                        String json_lat_lng = new Gson().toJson(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

                        Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(), json_lat_lng);
                        Sender sender = new Sender(driver_token, data);

                        ifcmService.sendMessage(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                Toast.makeText(HomeActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                                Toast.makeText(HomeActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();


                            }
                        });


                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void PickUpRequest(String uid) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PickUpRequest");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation(uid, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

        if (currentMarker != null) {
            currentMarker.remove();
        }
        mMap.addMarker(new MarkerOptions().title("PickUp Here")
                .snippet("").position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin)));

        currentMarker.showInfoWindow();

        mapRipple = new MapRipple(mMap, new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), this);
        mapRipple.withNumberOfRipples(2);
        mapRipple.withDistance(700);
        mapRipple.withDurationBetweenTwoRipples(1000);
        mapRipple.withRippleDuration(1000);
        mapRipple.withTransparency(0.7f);
        mapRipple.withFillColor(getResources().getColor(R.color.quantum_bluegrey800));

        mapRipple.startRippleMapAnimation();

        pickup_Btn.setText("Getting Your Driver");


        findDriver();
    }

    private void findDriver() {


        DatabaseReference ref;
        if (isEconomySelected) {
            ref = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child("Economical");
        } else {

            ref = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child("Business");

        }
        GeoFire g = new GeoFire(ref);
        final GeoQuery geoQuery = g.queryAtLocation(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude())
                , radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!isDriverFound) {

                    isDriverFound = true;
                    driver_id = key;
                    pickup_Btn.setText("Call Driver");
                    Toast.makeText(HomeActivity.this, "" + driver_id, Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onKeyExited(String key) {


            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if driver not found inrease radius //

                if (!isDriverFound && radius < LIMIT) {
                    radius++;
                    findDriver();

                } else {

                    if (!isDriverFound) {
                        pickup_Btn.setEnabled(true);

                        Toast.makeText(HomeActivity.this, "There is no available Driver Near You...", Toast.LENGTH_SHORT).show();
                        pickup_Btn.setText("PickUp Request");
                        geoQuery.removeAllListeners();

                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {


        } else if (id == R.id.nav_slideshow) {


            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {


                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }


        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void setUpLocation() {


        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.CALL_PHONE
            }, 1000);


        } else {


            BuildLocationCallback();
            buildLocationRequest();
            displayLocation();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    BuildLocationCallback();
                    buildLocationRequest();
                    displayLocation();



                }

                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

        } catch (Exception e) {
            e.printStackTrace();


        }


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);


        mMap.setInfoWindowAdapter(new CustomInfoHelper(this));

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (destination_marker != null)
                    destination_marker.remove();
                destination_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin))
                        .position(latLng)
                        .title("Destination Location"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                bottomSheetDialogFragment = BottomSheetPickUpFragment
                        .getInstance(String.format("%f,%f", lastLocation.getLatitude(), lastLocation.getLongitude())
                                , String.format("%f,%f", latLng.latitude, latLng.longitude), true);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());


            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {


                if (!marker.getTitle().equals("You") && !marker.getTitle().equals("Destination Location")) {


                    Intent intent = new Intent(HomeActivity.this, CallDriverActivity.class);
                    intent.putExtra("driver_id", marker.getSnippet().replace("\\D+", ""));
                    intent.putExtra("lat", lastLocation.getLatitude());
                    intent.putExtra("lng", lastLocation.getLongitude());
                    startActivity(intent);


                }
                if (marker.getTitle().equals("Destination Location")) {

                    Toast.makeText(HomeActivity.this, "Destination Location", Toast.LENGTH_LONG).show();
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        BuildLocationCallback();
        buildLocationRequest();
        displayLocation();
    }


    private void setTokenId() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("CustomerTokens");

        String tokenID = FirebaseInstanceId.getInstance().getToken();

        String driverid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerTokens token = new CustomerTokens(tokenID, driverid);
        reference.child(driverid).setValue(token);
        Common.CURRENT_TOKEN = tokenID;

    }

    @Override
    protected void onStart() {
        super.onStart();

        setTokenId();
    }

    private void loadAllAvailableDrivers(final LatLng location) {

        //load all the driver with in 3 km range

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(location)
                .title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin)));


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));


        //load all available Driver in distance 3km
        DatabaseReference ref;
        if (isEconomySelected) {
            ref = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child("Economical");
        } else {

            ref = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child("Business");

        }
        GeoFire g = new GeoFire(ref);

        GeoQuery geoQuery = g.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                FirebaseDatabase.getInstance().getReference().child("Drivers")
                        .child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Drivers driver = snapshot.getValue(Drivers.class);


                        if (isEconomySelected) {

                            if (driver.getCar_type().equals("Economical")) {

                                //add driver to map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(driver.getName())
                                        .snippet(driver.getId())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }

                        } else {


                            if (driver.getCar_type().equals("Business")) {

                                //add driver to map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(driver.getName())
                                        .snippet(driver.getId())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (distance <= LIMIT) { //distance find for 3 km

                    distance++;
                    loadAllAvailableDrivers(location);


                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }



        private void getPermission() {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE}, 1000);
            }





    }


    private void BuildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    lastLocation = location;
                }
                displayLocation();
            }
        };

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;

                if (lastLocation != null) {

                    LatLng centerpoint = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                    //distance in meters//
                    // heading 0 refer to northside, 90 is for east, 180 is for south and 270 is for west

                    LatLng northside = SphericalUtil.computeOffset(centerpoint, 100000, 0);
                    LatLng southside = SphericalUtil.computeOffset(centerpoint, 100000, 180);


                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northside)
                            .include(southside)
                            .build();


                    if (currentMarker != null) {
                        currentMarker.remove();
                    }


                    final double lat = lastLocation.getLatitude();
                    final double lng = lastLocation.getLongitude();


                    if (currentMarker != null) {
                        currentMarker.remove();

                    }
                    currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("My Location")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin)));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13.0f));

                    driverAvailable = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(isEconomySelected ? "Economical" : "Business");
                    driverAvailable.addValueEventListener(HomeActivity.this);
                    //rotateMarker(currentMarker, -360, mMap);


                    //  geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));


                    // loadAllAvailableDrivers();


                }
            }
        });

        // stopLocation();


    }

    private void stopLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    private void buildLocationRequest() {


        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        loadAllAvailableDrivers(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(dropoff);

        super.onDestroy();
    }
}
