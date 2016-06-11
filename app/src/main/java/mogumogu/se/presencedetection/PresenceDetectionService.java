package mogumogu.se.presencedetection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PresenceDetectionService {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("register_user")
    Call<String> registerUser(@Body String input);
}
