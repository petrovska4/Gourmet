package com.example.booking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;

public class AddRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private EditText nameEditText, addressEditText, phoneEditText, latitudeEditText, longitudeEditText;
    private Button saveButton;
    private GoogleMap mMap;
    private Marker marker;
    private double latitude, longitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_restaurant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameEditText = findViewById(R.id.editTextName);
        addressEditText = findViewById(R.id.editTextAddress);
        phoneEditText = findViewById(R.id.editTextPhone);
        latitudeEditText = findViewById(R.id.editTextLatitude);
        longitudeEditText = findViewById(R.id.editTextLongitude);
        saveButton = findViewById(R.id.buttonSave);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeMap();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        saveButton.setOnClickListener(v -> saveRestaurant());
    }

    private void saveRestaurant() {
        String name = nameEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        double latitude = Double.parseDouble(latitudeEditText.getText().toString());
        double longitude = Double.parseDouble(longitudeEditText.getText().toString());


        FirebaseUtils.saveRestaurant(name, address, phone, latitude, longitude, new FirebaseUtils.RestaurantCreationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddRestaurantActivity.this, "Restaurant added successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddRestaurantActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AddRestaurantActivity.this, "Failed to add restaurant: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            if (marker != null) {
                marker.remove();
            }

            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));
        });

        LatLng defaultLocation = new LatLng(42.00511676388657, 21.408351300442042);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

}