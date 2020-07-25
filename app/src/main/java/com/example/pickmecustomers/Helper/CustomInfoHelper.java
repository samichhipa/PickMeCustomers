package com.example.pickmecustomers.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.pickmecustomers.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoHelper implements GoogleMap.InfoWindowAdapter {

    View view;

    public CustomInfoHelper(Context context) {

        view= LayoutInflater.from(context).inflate(R.layout.custom_rider_info,null);


    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txt_title,txt_snipest;

        txt_title=view.findViewById(R.id.txt_pickup);
        txt_snipest=view.findViewById(R.id.txt_pickup_snippest);

        txt_title.setText(marker.getTitle());
        txt_snipest.setText(marker.getSnippet());



        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
