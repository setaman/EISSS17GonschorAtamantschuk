package com.example.android.harvesthand;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddNewEntry extends AppCompatActivity {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ImageButton locationButton;
    private ProgressBar locationPb;
    private EditText locationEdit;
    private Geocoder geocoder;
    private ScrollView container;
    private Contracts contracts;
    private TextInputLayout locationInputLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_entry);


        contracts = new Contracts();

        geocoder = new Geocoder(this, Locale.getDefault());

        container = (ScrollView) findViewById(R.id.add_new_entry_container);

        locationPb = (ProgressBar) findViewById(R.id.add_new_entry_location_pb);

        locationInputLayout = (TextInputLayout) findViewById(R.id.add_inputlayout_entry_location);

        Spinner cropSpinner = (Spinner) findViewById(R.id.spinner_crop);
        Spinner soilSpinner = (Spinner) findViewById(R.id.spinner_soil);
        cropSpinner.setAdapter(setupSpinner(R.array.crop_spinner_array));
        soilSpinner.setAdapter(setupSpinner(R.array.soil_spinner_array));

        final EditText locationEdit = (EditText) findViewById(R.id.add_entry_location);
        locationEdit = (EditText) findViewById(R.id.add_entry_location);
        locationButton = (ImageButton) findViewById(R.id.add_location_button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {

                locationEdit.setText(location.getLatitude() + " | " + location.getLongitude());
                Toast.makeText(AddNewEntry.this, location.getLatitude() + " | " + location.getLongitude(), Toast.LENGTH_LONG).show();
                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                }


                contracts.showSnackbar(container, getString(R.string.msg_receive_gps_data), false, true);
                if (!Geocoder.isPresent()) {
                    contracts.showSnackbar(container, getString(R.string.msg_can_not_get_location), true, false);
                    stopRequestLocation();
                    return;
                }
                findGeocoder(location.getLatitude(), location.getLongitude());
                stopRequestLocation();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }

        };
        setButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setButton();
                    return;
                }
                break;
        }
    }

    private void setButton() {
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(AddNewEntry.this, "Reauest Location...", Toast.LENGTH_LONG).show();

                if (ActivityCompat.checkSelfPermission(AddNewEntry.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(AddNewEntry.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.INTERNET}

                                ,10);
                    }
                    return;
                }

                                , 10);
                    }
                    return;
                }
                locationPb.setVisibility(View.VISIBLE);
                locationInputLayout.setHint(getString(R.string.msg_request_location));
                locationManager.requestLocationUpdates("gps", 1000, 10, locationListener);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_save:
                Toast.makeText(this, "Entry saved", Toast.LENGTH_LONG).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public ArrayAdapter setupSpinner(int strings) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                strings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    public void sendRequest(String URL) {
        final View view = findViewById(R.id.main_content);
        final Contracts contracts = new Contracts();

        Log.i("URL: ", URL);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        try {
                            String currentUserId = response.getString("_id");
                            if (currentUserId != null) {
                                startActivity(new Intent(AddNewEntry.this, MainActivity.class));
                                Toast.makeText(AddNewEntry.this, getString(R.string.welcome_to_harvesthand), Toast.LENGTH_SHORT).show();
                            } else {
                                contracts.showSnackbar(view, getString(R.string.msg_please_login), true);
                            }
                            Log.i("User_id: ", currentUserId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            contracts.showSnackbar(view, getString(R.string.msg_error), true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 500:
                                    contracts.showSnackbar(view, getString(R.string.msg_internal_error), true);
                                    break;
                                case 404:
                                    contracts.showSnackbar(view, getString(R.string.msg_404_error), true);
                                    break;
                            }
                        } else {
                            contracts.showSnackbar(view, getString(R.string.connection_err), true);
                        }
                    }
                });
        Volley.newRequestQueue(this).add(request);
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        // ...

        List<String> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.get
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        try {
                            String currentUserId = response.getString("_id");
                            if (currentUserId != null) {
                                startActivity(new Intent(AddNewEntry.this, MainActivity.class));
                                Toast.makeText(AddNewEntry.this, getString(R.string.welcome_to_harvesthand), Toast.LENGTH_SHORT).show();
                            } else {
                                contracts.showSnackbar(view, getString(R.string.msg_please_login), true, false);
                            }
                            Log.i("User_id: ", currentUserId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            contracts.showSnackbar(view, getString(R.string.msg_error), true, false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 500:
                                    contracts.showSnackbar(view, getString(R.string.msg_internal_error), true, false);
                                    break;
                                case 404:
                                    contracts.showSnackbar(view, getString(R.string.msg_404_error), true, false);
                                    break;
                            }
                        } else {
                            contracts.showSnackbar(view, getString(R.string.connection_err), true, false);
                        }
                    }
                });
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRequestLocation();
    }

    private void findGeocoder(Double lat, Double lon) {
        final int maxResults = 1;
        List<Address> addresses;
        locationPb.setVisibility(View.GONE);
        locationInputLayout.setHint(getString(R.string.new_entry_hint_location));
        try {
            addresses = geocoder.getFromLocation(lat, lon, maxResults);
            if (addresses != null) {
                Address currentAddress = addresses.get(0);
                locationEdit.setText(currentAddress.getCountryName() + getString(R.string.item_speaktext_coma)
                        + currentAddress.getLocality());
            } else {
                contracts.showSnackbar(container, getString(R.string.msg_can_not_get_location), true, false);
            }

        } catch (IOException e) {
            e.printStackTrace();
            contracts.showSnackbar(container, getString(R.string.msg_can_not_get_location), true, false);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            contracts.showSnackbar(container, getString(R.string.msg_can_not_get_location), true, false);
        }

    }

    private void stopRequestLocation() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
