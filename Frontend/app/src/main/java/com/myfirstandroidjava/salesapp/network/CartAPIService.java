package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.AddToCartRequest;
import com.myfirstandroidjava.salesapp.models.CartListResponse;
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.UpdateCartItemRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartAPIService {
    @GET("Cart")
    Call<CartListResponse> getCart();

    @POST("Cart/items")
    Call<CartListResponse> addToCart(@Body AddToCartRequest addToCartRequest);

    @PUT("Cart/items/{id}")
    Call<GenericResponse> updateCartItem(@Path("id") int id, @Body UpdateCartItemRequest request);

    @DELETE("Cart/items/{id}")
    Call<GenericResponse> removeCartItem(@Path("id") int id);

    @DELETE("Cart/clear")
    Call<GenericResponse> clearCart();
}
