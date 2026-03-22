package com.myfirstandroidjava.salesapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.StoreLocationResponse;
import com.myfirstandroidjava.salesapp.network.RetrofitClient;
import com.myfirstandroidjava.salesapp.network.StoreAPIService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreLocationFragment extends Fragment {

    private TextView textStoreAddress;
    private Button btnShowOnMap, btnGetDirections;
    private ProgressBar progressBar;
    private CardView locationCard;
    private StoreAPIService storeAPIService;

    private double latitude;
    private double longitude;
    private String address;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textStoreAddress = view.findViewById(R.id.textStoreAddress);
        btnShowOnMap = view.findViewById(R.id.btnShowOnMap);
        btnGetDirections = view.findViewById(R.id.btnGetDirections);
        progressBar = view.findViewById(R.id.progressBar);
        locationCard = view.findViewById(R.id.locationCard);

        storeAPIService = RetrofitClient.getClientPublic(getContext()).create(StoreAPIService.class);

        fetchStoreLocation();

        btnShowOnMap.setOnClickListener(v -> showOnMap());
        btnGetDirections.setOnClickListener(v -> getDirections());
    }

    private void fetchStoreLocation() {
        progressBar.setVisibility(View.VISIBLE);

        storeAPIService.getStoreLocations().enqueue(new Callback<List<StoreLocationResponse>>() {
            @Override
            public void onResponse(Call<List<StoreLocationResponse>> call, Response<List<StoreLocationResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Use the first location from the list
                    StoreLocationResponse location = response.body().get(0);

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    address = location.getAddress();

                    if (address != null && !address.isEmpty() && latitude != 0.0 && longitude != 0.0) {
                        textStoreAddress.setText(address);
                        locationCard.setVisibility(View.VISIBLE);
                        btnShowOnMap.setVisibility(View.VISIBLE);
                        btnGetDirections.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(), "Invalid location data from API", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No store locations found", Toast.LENGTH_SHORT).show();
                    Log.e("STORE_LOCATION", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<StoreLocationResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("STORE_LOCATION", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading store location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOnMap() {
        if (getActivity() == null || address == null || latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(getContext(), "Incomplete location data", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getContext(), "Map app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDirections() {
        if (getActivity() == null || address == null || address.isEmpty()) {
            Toast.makeText(getContext(), "Incomplete location data", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Intent genericMapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:" + latitude + "," + longitude + "?q=" + Uri.encode(address)));

            if (genericMapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(genericMapIntent);
            } else {
                Toast.makeText(getContext(), "Map app not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}