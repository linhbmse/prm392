package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.ProductDetailResponse;
import com.myfirstandroidjava.salesapp.models.ProductListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductAPIService {
    @GET("Product")
    Call<ProductListResponse> getProducts(
            @Query("search") String search,
            @Query("categoryId") Integer categoryId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("sort") String sort,
            @Query("skip") int skip,
            @Query("take") int take
    );

    @GET("Product/{id}")
    Call<ProductDetailResponse> getProductDetail(@Path("id") int id);
}
