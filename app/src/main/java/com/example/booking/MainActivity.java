package com.example.booking;

import static com.example.booking.FirebaseUtils.fetchUserIsRestaurateurStatus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    private FirebaseAuth mAuth;
    private ImageButton menuIcon;
    private boolean isRestaurateur = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        fetchUserIsRestaurateurStatus(new FirebaseUtils.FirebaseUserCallback() {
            @Override
            public void onSuccess(Boolean status) {
                isRestaurateur = status;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        menuIcon = findViewById(R.id.menu_icon);

        menuIcon.setOnClickListener(v -> showMenu());

        updateIds();
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuIcon);

        Menu menu = popupMenu.getMenu();

        if(isRestaurateur) {
            menu.add(Menu.NONE, 1, 1, "Add Restaurant");
            menu.add(Menu.NONE, 2, 2, "My restaurants");
        }
        menu.add(Menu.NONE, 4, 4, "Logout");
        menu.add(Menu.NONE, 3, 3, "Favourite restaurants");


        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    openAddRestaurantScreen();
                    return true;
                case 2:
                    openMyRestaurantsScreen();
                    return true;
                case 3:
                    openFavouriteRestaurantsScreen();
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void openAddRestaurantScreen() {
        Intent intent = new Intent(MainActivity.this, AddRestaurantActivity.class);
        startActivity(intent);
    }

    private void openMyRestaurantsScreen() {
        Intent intent = new Intent(MainActivity.this, MyRestaurantsActivity.class);
        startActivity(intent);
    }

    private void openFavouriteRestaurantsScreen() {
        Intent intent = new Intent(MainActivity.this, FavouriteRestaurantsActivity.class);
        startActivity(intent);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
                fetchRestaurants(userLat, userLng);
            } else {
                Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRestaurants(double userLat, double userLng) {
        FirebaseUtils.getAllRestaurants(new FirebaseUtils.RestaurantsCallback() {
            @Override
            public void onSuccess(List<Restaurant> restaurants) {
                restaurantList = restaurants;
                sortRestaurantsByDistance(userLat, userLng);
                adapter = new RestaurantAdapter(MainActivity.this, restaurantList, userLat, userLng);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortRestaurantsByDistance(double userLat, double userLng) {
        Collections.sort(restaurantList, new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant r1, Restaurant r2) {
                double dist1 = calculateDistance(userLat, userLng, r1.getLatitude(), r1.getLongitude());
                double dist2 = calculateDistance(userLat, userLng, r2.getLatitude(), r2.getLongitude());
                return Double.compare(dist1, dist2);
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateIds() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String documentId = document.getId();
                db.collection("restaurants").document(documentId)
                        .update("restaurantId", documentId)
                        .addOnSuccessListener(aVoid -> {
                            System.out.println("Updated restaurantId for: " + documentId);
                        })
                        .addOnFailureListener(e -> {
                            System.err.println("Failed to update restaurantId: " + e.getMessage());
                        });
            }
        }).addOnFailureListener(e -> {
            System.err.println("Error retrieving documents: " + e.getMessage());
        });
    }

}