package se.mogumogu.presencedetector;

import retrofit2.Retrofit;

public class RetrofitManager {

    private PresenceDetectionService service;

    public RetrofitManager(String serverUrl){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl + "/sys/api/")
                .addConverterFactory(new StringConverterFactory())
                .build();

        service = retrofit.create(PresenceDetectionService.class);
    }

    public PresenceDetectionService getPresenceDetectionService() {

        return service;
    }
}
