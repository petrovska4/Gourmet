package com.example.booking;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, addressTextView, phoneTextView, distanceTextView, ratingTextView;


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

        if (restaurantName != null) {
            FirebaseUtils.fetchRestaurantDetails(restaurantName, new FirebaseUtils.OnRestaurantDetailsListener() {
                @Override
                public void onSuccess(Restaurant restaurant) {
                    nameTextView.setText(restaurant.getName());
                    addressTextView.setText(restaurant.getAddress());
                    phoneTextView.setText(restaurant.getPhoneNumber());
                    String distance = "Latitude: " + restaurant.getLatitude() + " Longitude: " + restaurant.getLongitude();
                    distanceTextView.setText(distance);
//                    ratingTextView.setText(String.format("%.1f ★", restaurant.getRating()));
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(RestaurantDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}