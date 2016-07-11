package se.mogumogu.presencedetector;

import retrofit2.Retrofit;

public class RetrofitManager {

    public static final String BASE_URL = "http://beacons.zenzor.io/sys/api/";

    private PresenceDetectionService service;

    public RetrofitManager(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(new StringConverterFactory())
                .build();

        service = retrofit.create(PresenceDetectionService.class);
    }

    public PresenceDetectionService getPresenceDetectionService() {

        return service;
    }
}
