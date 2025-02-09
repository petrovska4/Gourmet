package com.example.booking;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
            Restaurant restaurant = new Restaurant(userId, name, address, phone, latitude, longitude, 0.0, 0.0);

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
                            callback.onSuccess(new ArrayList<>());
                            return;
                        }

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String restaurantId = document.getString("restaurantId");

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
                                    callback.onSuccess(restaurantList);
                                })
                                .addOnFailureListener(e -> {
                                    callback.onFailure(e.getMessage());
                                });

                    })
                    .addOnFailureListener(e -> {
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

                        String restaurantId = document.getString("restaurantId");
                        String address = document.getString("address");
                        String phone = document.getString("phone");
                        String userId = document.getString("userId");
                        double longitude = document.getDouble("longitude") != null ? document.getDouble("longitude") : 0.0;
                        double latitude = document.getDouble("latitude") != null ? document.getDouble("latitude") : 0.0;
                        double revSum = document.getDouble("revSum") != null ? document.getDouble("revSum") : 0.0;
                        double revCnt = document.getDouble("revCnt") != null ? document.getDouble("revCnt") : 0.0;

                        listener.onSuccess(new Restaurant(restaurantId, userId, name, address, phone, latitude, longitude, revSum, revCnt));
                    } else {
                        listener.onFailure("Restaurant not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Error fetching data: " + e.getMessage()));
    }

    public static void saveRestaurantAsFavourite(Context context, String restaurantId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("favourites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(context, "Restaurant is already in favorites!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> favouriteData = new HashMap<>();
                    favouriteData.put("userId", userId);
                    favouriteData.put("restaurantId", restaurantId);

                    db.collection("favourites")
                            .add(favouriteData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(context, "Added to favorites!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error saving favorite: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error checking favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public static void saveReview(Review review, Runnable onSuccess) {
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving review", e);
                });

        db.collection("restaurants")
                .whereEqualTo("restaurantId", review.getRestaurantId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot restaurantDoc = queryDocumentSnapshots.getDocuments().get(0);
                        db.collection("restaurants")
                                .document(restaurantDoc.getId())
                                .update(
                                        "revCnt", FieldValue.increment(1),
                                        "revSum", FieldValue.increment(review.getRating())
                                )
                                .addOnFailureListener(e -> Log.e("Firebase", "Error updating restaurant", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error fetching restaurant", e));
    }

    public static void fetchReviewsByRestaurantId(String restaurantId, OnReviewsFetchedListener listener) {
        CollectionReference reviewsRef = db.collection("reviews");

        reviewsRef.whereEqualTo("restaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Review review = snapshot.toObject(Review.class);
                        reviews.add(review);
                    }
                    listener.onSuccess(reviews);
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e.getMessage());
                });
    }

    public static void checkIfReviewExists(String restaurantId, OnReviewCheckListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("reviews")
                .whereEqualTo("restaurantId", restaurantId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean exists = !queryDocumentSnapshots.isEmpty();
                    listener.onCheckCompleted(exists);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnReviewCheckListener {
        void onCheckCompleted(boolean exists);
        void onFailure(String error);
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

    public interface OnReviewsFetchedListener {
        void onSuccess(List<Review> reviews);
        void onFailure(String errorMessage);
    }
}
