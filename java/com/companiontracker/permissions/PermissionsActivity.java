package com.companiontracker.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.companiontracker.LocationService;


public class PermissionsActivity {
        Activity activity;

    public PermissionsActivity(Activity activity) {
                this.activity = activity;

    }

    public static final String TAG = "MainActivity";

    /**
     * Id to identify a camera permission request.
     */
    public static final int REQUEST_CAMERA = 3;

    /**
     * Id to identify a contacts permission request.
     */
    public static final int REQUEST_CONTACTS = 1;

    /**
     * Permissions required to read and write contacts.
     */
    public static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};

    /**
     * Permissions required to read and write contacts.
     */
    private static String[] permissionsCameraGallery = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};


    public boolean checkSDCardReadPermission(Context context) {
        return !(ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    public void displayCameraAndGalleryPermissionAlert(Activity activity) {
        // No explanation needed, we can request the permission.
        ActivityCompat.requestPermissions(activity, permissionsCameraGallery, REQUEST_CAMERA);
    }



    public void showCamera() {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                                    Manifest.permission.READ_EXTERNAL_STORAGE : null,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CAMERA);

        } else {


        }


    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showContacts() {
        // Contact permissions have not been granted yet. Request them directly.
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
    }

    public void showStorage() {
        // Contact permissions have not been granted yet. Request them directly.
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            ActivityCompat.requestPermissions(activity, new String[]{
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                                    Manifest.permission.READ_EXTERNAL_STORAGE : null,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    3);

        }
    }

    //location permission

    public void locationPermission() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION);

        }
    }

}
