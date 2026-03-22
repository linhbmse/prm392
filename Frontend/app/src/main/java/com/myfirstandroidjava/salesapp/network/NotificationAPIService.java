package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.NotificationBadge;
import com.myfirstandroidjava.salesapp.models.NotificationListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationAPIService {
    @GET("Notification")
    Call<NotificationListResponse> getNotifications(
            @Query("skip") int skip,
            @Query("take") int take
    );

    @GET("Notification/badge")
    Call<NotificationBadge> getBadge();

    @PUT("Notification/{id}/read")
    Call<GenericResponse> markRead(@Path("id") int id);

    @PUT("Notification/read-all")
    Call<GenericResponse> markAllRead();
}
