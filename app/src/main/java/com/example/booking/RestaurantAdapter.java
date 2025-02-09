package com.example.booking;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private List<Restaurant> restaurantList;
    private double userLat, userLng;
    private Context context;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList, double userLat, double userLng) {
        this.restaurantList = restaurantList;
        this.userLat = userLat;
        this.userLng = userLng;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.restaurantName.setText(restaurant.getName());

        double distance = getDistance(userLat, userLng, restaurant.getLatitude(), restaurant.getLongitude());
        holder.restaurantDistance.setText(String.format("Distance: %.2f km", distance));

        float rating = (float) ((float) restaurant.getRevSum() / restaurant.getRevCnt());

        holder.restaurantRating.setText("Rating: " + (rating > 0 ? rating : "No ratings yet"));

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailsActivity.class);
            intent.putExtra("name", restaurant.getName());
            context.startActivity(intent);
        });

        holder.btnFavourite.setOnClickListener(v -> {
            FirebaseUtils.saveRestaurantAsFavourite(restaurant.getRestaurantId());
        });

    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName, restaurantDistance, restaurantRating;
        Button btnDetails;
        ImageButton btnFavourite;

        public ViewHolder(View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantDistance = itemView.findViewById(R.id.restaurantDistance);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnFavourite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        Location locationA = new Location("A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        Location locationB = new Location("B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        return locationA.distanceTo(locationB) / 1000;

    }
}
