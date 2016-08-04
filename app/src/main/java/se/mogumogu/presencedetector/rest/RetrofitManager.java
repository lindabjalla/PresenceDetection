package se.mogumogu.presencedetector.rest;

import retrofit2.Retrofit;

public final class RetrofitManager {

    private PresenceDetectionService service;

    public RetrofitManager(final String serverUrl){

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl + "/sys/api/")
                .addConverterFactory(new StringConverterFactory())
                .build();

        service = retrofit.create(PresenceDetectionService.class);
    }

    public PresenceDetectionService getPresenceDetectionService() {

        return service;
    }
}
