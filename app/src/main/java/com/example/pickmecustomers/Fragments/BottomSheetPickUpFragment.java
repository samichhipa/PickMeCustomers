package com.example.pickmecustomers.Fragments;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pickmecustomers.Fare.IGoogleAPI;
import com.example.pickmecustomers.Model.Common;
import com.example.pickmecustomers.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetPickUpFragment extends BottomSheetDialogFragment {


    String location, destination;
    IGoogleAPI iGoogleAPI;
    boolean isMapTapped=false;


    android.app.AlertDialog alertDialog;


    TextView txt_location, txt_destination, fare_calculate,distance_txt;



    public static BottomSheetDialogFragment getInstance(String loc, String dest,boolean isMapTap) {

        BottomSheetDialogFragment fragment = new BottomSheetPickUpFragment();
        Bundle bundle = new Bundle();
        bundle.putString("location", loc);
        bundle.putString("destination", dest);
        bundle.putBoolean("mapTapped",isMapTap);
        fragment.setArguments(bundle);
        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        location = getArguments().getString("location");
        destination = getArguments().getString("destination");
        isMapTapped=getArguments().getBoolean("mapTapped");


    }

    private void getFarePrice(String location, String destination) {

        alertDialog=new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).setTheme(R.style.Custom).build();
        alertDialog.setMessage("Loading...");
        alertDialog.show();


        String requestUrl = null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + location + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_map_api);

            Log.d("REQ", requestUrl);

            iGoogleAPI.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {



                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);


                        //get distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String txt_distance = distance.getString("text");


                        //use regex will remove all text not is digit
                        Double distance_val = Double.parseDouble(txt_distance.replaceAll("[^0-9\\\\.]+", ""));


                        //get time

                        JSONObject timeObj = legsObject.getJSONObject("duration");
                        String time_txt = timeObj.getString("text");
                        Toast.makeText(getContext(), txt_distance, Toast.LENGTH_SHORT).show();
                        Integer time_val = Integer.parseInt(time_txt.replaceAll("\\D+", ""));

                        String final_calculate = String.format("%s + %s = $%.2f", txt_distance, time_txt,
                                Common.getPrice(distance_val, time_val));

                        fare_calculate.setText(String.format("%.2f",Common.getPrice(distance_val, time_val)));

                        if (isMapTapped){

                            String start_address,end_address;
                            start_address=legsObject.getString("start_address");
                            end_address=legsObject.getString("end_address");


                            txt_location.setText(start_address);
                            txt_destination.setText(end_address);
                            distance_txt.setText(txt_distance);



                        }

                        alertDialog.dismiss();




                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    alertDialog.dismiss();

                    Log.d("ERROR", t.getMessage());
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.pickup_bottom_sheet, container, false);

        txt_location = root.findViewById(R.id.txt_sheet_location);
        txt_destination = root.findViewById(R.id.txt_sheet_destination);
        fare_calculate = root.findViewById(R.id.calculate_fare);
        distance_txt=root.findViewById(R.id.txt_distance);
        iGoogleAPI = Common.getGoogleService();

        getFarePrice(location, destination);

        if (!isMapTapped) {

            txt_location.setText(location);

            txt_destination.setText(destination);

        }

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }
}
