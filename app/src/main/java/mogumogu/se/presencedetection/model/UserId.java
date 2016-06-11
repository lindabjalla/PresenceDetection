package mogumogu.se.presencedetection.model;

import com.google.gson.annotations.SerializedName;

public class UserId {

    @SerializedName("id_user")
    private String userId;

    public String getUserId() {

        return userId;
    }
}
