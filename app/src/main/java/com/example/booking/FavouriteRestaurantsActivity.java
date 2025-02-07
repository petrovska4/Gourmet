package com.example.booking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavouriteRestaurantsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavouritesAdapter adapter;
    Button btnShowPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourite_restaurants);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFavouriteRestaurants();
    }

    private void fetchFavouriteRestaurants() {
        Log.d("Fr", "fetch called");
        FirebaseUtils.getAllFavouriteRestaurants(new FirebaseUtils.RestaurantsCallback () {
            @Override
            public void onSuccess(List<Restaurant> restaurantList) {
                Log.d("FavouriteRestaurants", "onSuccess() called with size: " + restaurantList.size());
                if (restaurantList.isEmpty()) {
                    Toast.makeText(FavouriteRestaurantsActivity.this, "No favorite restaurants found", Toast.LENGTH_SHORT).show();
                } else {
                    adapter = new FavouritesAdapter(FavouriteRestaurantsActivity.this,restaurantList, 0, 0);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(FavouriteRestaurantsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}