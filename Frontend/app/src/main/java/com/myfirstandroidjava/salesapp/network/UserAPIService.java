package com.myfirstandroidjava.salesapp.network;

import com.myfirstandroidjava.salesapp.models.ChangePasswordRequest;
import com.myfirstandroidjava.salesapp.models.GenericResponse;
import com.myfirstandroidjava.salesapp.models.UpdateProfileRequest;
import com.myfirstandroidjava.salesapp.models.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserAPIService {
    @GET("User/me")
    Call<UserProfile> getProfile();

    @PUT("User/me")
    Call<UserProfile> updateProfile(@Body UpdateProfileRequest request);

    @PUT("User/me/password")
    Call<GenericResponse> changePassword(@Body ChangePasswordRequest request);
}
