package com.example.pickmecustomers.Fare;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class GoogleClient {

    private static Retrofit retrofit=null;

    public static Retrofit getGoogleClient(String baseUrl)
    {

        if (retrofit==null){

            retrofit=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();


        }
        return retrofit;

    }
}
