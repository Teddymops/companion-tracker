package com.companiontracker.asynctasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.companiontracker.interfaces.GetAddressInterface;

import java.util.List;
import java.util.Locale;


public class GetAddressAsyncTask extends AsyncTask<LatLng, Integer, String> {

    public GetAddressInterface delegate;


    private Context context;

    public GetAddressAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        delegate.onGetAddressPostExecute(result);
    }

    @Override
    protected String doInBackground(LatLng... latLngs) {
        return getResponse(latLngs[0]);
    }


    private String getResponse(LatLng location) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {

            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();


            return address + "," + city + "," + state + "," + country ;

        } catch (Exception e) {
            return null;
        }
    }
}
