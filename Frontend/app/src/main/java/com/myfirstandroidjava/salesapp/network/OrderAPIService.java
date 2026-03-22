package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.CreateOrderRequest;
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.Order;
import com.myfirstandroidjava.salesapp.models.OrderListResponse;
import com.myfirstandroidjava.salesapp.models.OrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderAPIService {
    @POST("Order")
    Call<OrderResponse> createOrder(@Body CreateOrderRequest request);

    @GET("Order")
    Call<OrderListResponse> getMyOrders(
            @Query("skip") int skip,
            @Query("take") int take
    );

    @GET("Order/{id}")
    Call<Order> getOrderDetail(@Path("id") int id);

    @PUT("Order/{id}/cancel")
    Call<GenericResponse> cancelOrder(@Path("id") int id);

    @PUT("Order/{id}/confirm")
    Call<GenericResponse> confirmOrder(@Path("id") int id);

    @PUT("Order/{id}/complete")
    Call<GenericResponse> completeOrder(@Path("id") int id);
}
