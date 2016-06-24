package se.mogumogu.presencedetection.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import se.mogumogu.presencedetection.R;
import se.mogumogu.presencedetection.dialogfragment.RegistrationDialogFragment;
import se.mogumogu.presencedetection.RetrofitManager;
import se.mogumogu.presencedetection.model.User;
import se.mogumogu.presencedetection.model.UserId;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends FragmentActivity implements RegistrationDialogFragment.RegistrationDialogListener {

    public static final String PRESENCE_DETECTION_PREFERENCES = "se.mogumogu.presencedetection.PRESENCE_DETECTION_PREFERENCES";
    public static final String USER_IS_REGISTERED = "se.mogumogu.presencedetection.USER_IS_REGISTERED";
    public static final String USER_ID = "se.mogumogu.presencedetection.USER_ID";

    private SharedPreferences preferences;
    private DialogFragment dialogFragment;
    private Intent intent;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, ScanActivity.class);
        preferences = getSharedPreferences(PRESENCE_DETECTION_PREFERENCES, MODE_PRIVATE);
        gson = new Gson();

        //      bara för test
//        preferences.edit().putBoolean(USER_IS_REGISTERED, false).apply();

        String userId = preferences.getString(USER_ID, null);

        if (userId != null) {

            Log.d("userId", userId);
        }

        boolean userIsRegistered = preferences.getBoolean(USER_IS_REGISTERED, false);

        if (!userIsRegistered) {

            showRegistrationDialog();
        }

        Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(intent);
            }
        });
    }

    public void showRegistrationDialog() {

        dialogFragment = new RegistrationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "RegistrationDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, View view) {

        EditText firstNameEditText = (EditText) view.findViewById(R.id.first_name);
        EditText lastNameEditText = (EditText) view.findViewById(R.id.last_name);

        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();

        if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {

            Toast.makeText(this, "First name and last name can not be empty.", Toast.LENGTH_LONG).show();
            showRegistrationDialog();

        } else {

            final User user = new User(firstName, lastName);
            String userJson = gson.toJson(user);

            RetrofitManager retrofitManager = new RetrofitManager();
            Log.d("inputString", "input=" + userJson);
            Call<String> result = retrofitManager.getPresenceDetectionService().registerUser("input=" + userJson);

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    Log.d("response", response.body());

                    String responseBody = response.body();

                    if (responseBody.contains("\"response_value\":\"200\"")) {

                        UserId userIdObject = gson.fromJson(responseBody, UserId.class);
                        final String userId = userIdObject.getUserId();
                        Log.d("userId from server", userId);

                        preferences.edit().putString(USER_ID, userId).apply();
                        preferences.edit().putBoolean(USER_IS_REGISTERED, true).apply();
                        dialogFragment.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Log.d("onFailure", "failed");
                    t.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        dialogFragment.getDialog().cancel();
        showRegistrationDialog();
    }
}
