package se.mogumogu.presencedetector.model;

import com.google.gson.annotations.SerializedName;

public abstract class AbstractEntity {

    @SerializedName("api_key")
    protected String apiKey = "28742sk238sdkAdhfue243jdfhvnsa1923347";

    @SerializedName("id_user")
    protected String userId;

    @SerializedName("beacon_uuid")
    protected String beaconUuid;
}
