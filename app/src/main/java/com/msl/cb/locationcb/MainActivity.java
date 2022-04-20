package com.msl.cb.locationcb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.sql.Array;
import java.text.MessageFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_ALL_PERMISSIONS_CODE = 1000;
    private TextView locationTextView;
    private final ArrayList<String> permissionsRequired = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;

    /* check the permissions required but not yet granted */
    @NonNull
    private ArrayList<String> checkPermissionsNotGranted(
            @NonNull ArrayList<String> permissionsRequired
    ) {
        ArrayList<String> notGranted = new ArrayList<>();
        for (String perm : permissionsRequired)
            if (!(checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED))
                notGranted.add(perm);
        return notGranted;
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS)
            Toast.makeText(MainActivity.this,
                    "Google Play Services Available",
                    Toast.LENGTH_LONG).show();
        else apiAvailability.getErrorDialog
                (this, resultCode, resultCode,
                        dialogInterface -> {
                            Toast.makeText(MainActivity.this,
                                    "Google Play Services NOT Available",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }).show();

    }

    private void getLastLocation() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient
                        (MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission
                            (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
                locationTextView.setText(
                        MessageFormat.format("Last Location Lat: {0}, Lon:{1}",
                                location.getLatitude(), location.getLongitude()));
            else
                locationTextView.setText("Check if GPS is switched off");

        }).addOnFailureListener(e -> {
            locationTextView.setText("Error trying to get last location");
            e.printStackTrace();
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* permissions required to run the location
           service
        * */
        permissionsRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = checkPermissionsNotGranted(permissionsRequired);
        int size = permissionsToRequest.size();
        if (size > 0)
            requestPermissions( permissionsToRequest.toArray(
                    new String[size]
                    ),
                    REQUEST_ALL_PERMISSIONS_CODE);

        locationTextView = findViewById(R.id.location_textview);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALL_PERMISSIONS_CODE) {
            ArrayList<String> permissionsRejected = new ArrayList<>();
            for (String perm: permissionsToRequest)
                if (!(checkSelfPermission(perm)==PackageManager.PERMISSION_GRANTED))
                    permissionsRejected.add(perm);

            int size = permissionsRejected.size();
            if (size > 0)
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0)))
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("These permissions are required to get location")
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                requestPermissions(permissionsRejected.toArray(
                                        new String[size]
                                        ),
                                        REQUEST_ALL_PERMISSIONS_CODE);
                            })
                            .setNegativeButton("Cancel",null)
                            .create()
                            .show();

        }


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkGooglePlayServices();
        /* once permissions are granted the onPostResume will
           be called again - we can try to start location
           updates again
        */
        Log.d("Loc","start location update here");
        getLastLocation();

    }
}