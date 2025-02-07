package com.example.booking;


import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtils {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void createUser(String userId, String name, String email, String phone, boolean isRestaurateur, final UserCreationCallback callback) {
        User user = new User(name, email, phone, isRestaurateur);

        DocumentReference userRef = db.collection("users").document(userId);
        userRef.set(user)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    callback.onSuccess(false);
                });
    }

    public static void fetchUserIsRestaurateurStatus(final FirebaseUserCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean status = documentSnapshot.getBoolean("restaurateur");
                    if (status != null) {
                        callback.onSuccess(status);
                    } else {
                        callback.onFailure("field missing");
                    }
                } else {
                    callback.onFailure("User data not found");
                }
            }).addOnFailureListener(e -> {
                callback.onFailure("Error fetching user data: " + e.getMessage());
            });
        } else {
            callback.onFailure("User not logged in");
        }
    }

    public static void saveRestaurant(String name, String address, String phone, double latitude, double longitude, final RestaurantCreationCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            Restaurant restaurant = new Restaurant(userId, name, address, phone, latitude, longitude);

            db.collection("restaurants")
                    .add(restaurant)
                    .addOnSuccessListener(documentReference -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }

    public static void getAllRestaurants(final RestaurantsCallback callback) {

        db.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Restaurant> restaurantList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Restaurant restaurant = document.toObject(Restaurant.class);
                        restaurantList.add(restaurant);
                    }
                    callback.onSuccess(restaurantList);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void getAllRestaurantsByUserId(final RestaurantsCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection("restaurants")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Restaurant> restaurantList = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Restaurant restaurant = document.toObject(Restaurant.class);
                            restaurantList.add(restaurant);
                        }
                        callback.onSuccess(restaurantList);
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e.getMessage());
                    });
        }

    }

    public static void getAllFavouriteRestaurants(final RestaurantsCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection("favourites")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Restaurant> restaurantList = new ArrayList<>();
                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("Favourites", "No favourites found for user: " + userId);
                            callback.onSuccess(new ArrayList<>()); // Return empty list
                            return;
                        }

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String restaurantId = document.getString("restaurantId");
                            Log.d("Favourites", "Found restaurant ID: " + restaurantId);

                            if (restaurantId != null) {
                                Task<QuerySnapshot> task = db.collection("restaurants")
                                        .whereEqualTo("restaurantId", restaurantId)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            for (DocumentSnapshot doc : queryDocumentSnapshots1) {
                                                Restaurant restaurant = doc.toObject(Restaurant.class);
                                                Log.d("Favourites", "Retrieved restaurant: " + restaurant.getName());
                                                restaurantList.add(restaurant);
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Favourites", "Error fetching restaurant: " + e.getMessage()));
                                tasks.add(task);
                            }
                        }

                        Tasks.whenAllComplete(tasks)
                                .addOnSuccessListener(taskResults -> {
                                    Log.d("Favourites", "Returning restaurant list: " + restaurantList.size());
                                    callback.onSuccess(restaurantList);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Favourites", "Error: " + e.getMessage());
                                    callback.onFailure(e.getMessage());
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("Favourites", "Error: " + e.getMessage());
                        callback.onFailure(e.getMessage());
                    });
        } else {
            callback.onFailure("User not authenticated");
        }
    }


    public static void fetchRestaurantDetails(String name, OnRestaurantDetailsListener listener) {
        db.collection("restaurants")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        String address = document.getString("address");
                        String phone = document.getString("phone");
                        String userId = document.getString("userId");
                        double longitude = document.getDouble("longitude") != null ? document.getDouble("longitude") : 0.0;
                        double latitude = document.getDouble("latitude") != null ? document.getDouble("latitude") : 0.0;
                        double rating = document.getDouble("rating") != null ? document.getDouble("rating") : 0.0;

                        listener.onSuccess(new Restaurant(userId, name, address, phone, latitude, longitude));
                    } else {
                        listener.onFailure("Restaurant not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Error fetching data: " + e.getMessage()));
    }

    public static void saveRestaurantAsFavourite(String restaurantId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            return;
        }

        Map<String, Object> favouriteData = new HashMap<>();
        favouriteData.put("userId", userId);
        favouriteData.put("restaurantId", restaurantId);

        db.collection("favourites")
                .add(favouriteData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Favourite saved with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving favourite: " + e.getMessage());
                });
    }

    public static void saveReview(Review review, Runnable onSuccess) {
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    onSuccess.run();  // Calls the success callback
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving review", e);
                });
    }

    public interface OnRestaurantDetailsListener {
        void onSuccess(Restaurant restaurant);
        void onFailure(String errorMessage);
    }


    public interface UserCreationCallback {
        void onSuccess(boolean success);
    }
    public interface RestaurantCreationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface RestaurantsCallback {
        void onSuccess(List<Restaurant> restaurants);
        void onFailure(String errorMessage);
    }

    public interface FirebaseUserCallback {
        void onSuccess(Boolean status);
        void onFailure(String error);
    }
}
