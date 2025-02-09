package com.example.booking;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView nameTextView, addressTextView, phoneTextView, distanceTextView, ratingTextView;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private String restaurantId;
    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameTextView = findViewById(R.id.restaurantName);
        addressTextView = findViewById(R.id.address);
        phoneTextView = findViewById(R.id.phone);
        distanceTextView = findViewById(R.id.restaurantDistance);
        ratingTextView = findViewById(R.id.restaurantRating);

        String restaurantName = getIntent().getStringExtra("name");

        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);

        if (restaurantName != null) {
            FirebaseUtils.fetchRestaurantDetails(restaurantName, new FirebaseUtils.OnRestaurantDetailsListener() {
                @Override
                public void onSuccess(Restaurant restaurant) {
                    restaurantId = restaurant.getRestaurantId();
                    nameTextView.setText(restaurant.getName());
                    addressTextView.setText(restaurant.getAddress());
                    phoneTextView.setText(restaurant.getPhoneNumber());
                    String distance = "Latitude: " + restaurant.getLatitude() + " Longitude: " + restaurant.getLongitude();
                    ratingTextView.setText("Rating: " + String.valueOf(restaurant.getRevSum() / restaurant.getRevCnt()));
                    distanceTextView.setText(distance);
                    latitude = restaurant.getLatitude();
                    longitude = restaurant.getLongitude();
                    loadReviews(restaurantId);
                    if (mMap != null) {
                        updateMapLocation();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(RestaurantDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation();
    }

    private void updateMapLocation() {
        if (latitude != 0.0 && longitude != 0.0) {
            LatLng restaurantLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(restaurantLocation).title("Restaurant Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLocation, 15));
        }
    }


    private void loadReviews(String restaurantId) {
        if(restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, "Res id is null", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUtils.fetchReviewsByRestaurantId(restaurantId, new FirebaseUtils.OnReviewsFetchedListener() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (reviews.isEmpty()) {
                    ratingTextView.setText("No ratings yet");
                    return;
                }
                int oldSize = reviewList.size();
                reviewList.addAll(reviews);
                reviewAdapter.notifyItemRangeInserted(oldSize, reviews.size());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RestaurantDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}