package com.example.pickmecustomers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pickmecustomers.Model.Common;
import com.example.pickmecustomers.Model.Drivers;
import com.example.pickmecustomers.Notification.Data;
import com.example.pickmecustomers.Notification.DriverTokens;
import com.example.pickmecustomers.Notification.IFCMService;
import com.example.pickmecustomers.Notification.MyResponse;
import com.example.pickmecustomers.Notification.Sender;
import com.example.pickmecustomers.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pickmecustomers.Model.Common.lastLocation;

public class CallDriverActivity extends AppCompatActivity {

    TextView txt_driver_name, txt_driver_phone, txt_driver_rating;
    Button callDriver, callDriverDialup;

    String driver_id;

    double lat, lng;

    IFCMService ifcmService;

    ImageView cancelImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        init();

        getDriverInfo();


        cancelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
        callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                sendRequestToDriver(driver_id);
            }
        });

        callDriverDialup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(CallDriverActivity.this
                        , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "+"+txt_driver_phone.getText().toString().trim(), null));
                startActivity(intent);
                startActivity(intent);

            }
            });



        }

        private void getDriverInfo () {

            FirebaseDatabase.getInstance().getReference().child("Drivers").child(driver_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            Drivers drivers = snapshot.getValue(Drivers.class);

                            txt_driver_name.setText(drivers.getName());
                            txt_driver_phone.setText(drivers.getDriver_phone());
                            txt_driver_rating.setText("4.5");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        }

        private void sendRequestToDriver ( final String driver_id){


            DatabaseReference driver_ref = FirebaseDatabase.getInstance().getReference().child("DriverTokens");
            driver_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot snap : snapshot.getChildren()) {


                        DriverTokens driverTokens = snap.getValue(DriverTokens.class);

                        String id = driverTokens.getDriver_id();

                        if (driver_id.equals(id)) {
                            String driver_token = driverTokens.getToken_id();

                            Toast.makeText(CallDriverActivity.this, driver_token, Toast.LENGTH_SHORT).show();

                            //make notification Payload//

                            String json_lat_lng = new Gson().toJson(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

                            Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(), json_lat_lng);
                            Sender sender = new Sender(driver_token, data);

                            ifcmService.sendMessage(sender).enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    Toast.makeText(CallDriverActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Toast.makeText(CallDriverActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();


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

        private void init () {

            ifcmService = Common.getFCMService();

            txt_driver_name = findViewById(R.id.driver_name);
            txt_driver_phone = findViewById(R.id.driver_phone);
            txt_driver_rating = findViewById(R.id.driver_rating);
            callDriver = findViewById(R.id.call_driver);
            callDriverDialup = findViewById(R.id.call_driver_dialup);
            cancelImg=findViewById(R.id.cancelBtn);

            if (getIntent() != null) {

                driver_id = getIntent().getStringExtra("driver_id");
                lat = getIntent().getDoubleExtra("lat", -1.0);
                lng = getIntent().getDoubleExtra("lng", -1.0);

                lastLocation=new Location("");
                lastLocation.setLatitude(lat);
                lastLocation.setLongitude(lng);
            }

        }
    }
