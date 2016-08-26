package se.mogumogu.presencedetector.rest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PresenceDetectorService {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("register_user")
    Call<String> registerUser(@Body String input);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("subscribe_beacon")
    Call<String> subscribeBeacon(@Body String input);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("beacon_nearby")
    Call<String> setInRangeNotification(@Body String input);

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("beacon_outofrange")
    Call<String> setOutOfRangeNotification(@Body String input);
}
