package mogumogu.se.presencedetection.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("api_key")
    private String apiKey = "28742sk238sdkAdhfue243jdfhvnsa1923347";

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    public User(String firstName, String lastName){

        this.firstName = firstName;
        this.lastName = lastName;
    }
}
