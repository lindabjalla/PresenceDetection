package se.mogumogu.presencedetector.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.BasicDialogFragment;
import se.mogumogu.presencedetector.model.User;
import se.mogumogu.presencedetector.model.UserId;
import se.mogumogu.presencedetector.rest.RetrofitManager;

public final class RegistrationActivity extends ToolbarProvider implements BasicDialogFragment.BasicDialogListener {

    public static final String PRESENCE_DETECTION_PREFERENCES = "se.mogumogu.presencedetection.PRESENCE_DETECTION_PREFERENCES";
    public static final String USER_IS_REGISTERED = "se.mogumogu.presencedetection.USER_IS_REGISTERED";
    public static final String USER_ID = "se.mogumogu.presencedetection.USER_ID";
    public static final String PREFERENCE_SERVER_URL_KEY = "preference_server_url_key";
    public static final String DEFAULT_SERVER_URL = "http://beacons.zenzor.io";

    private SharedPreferences appDataPreferences;
    private SharedPreferences settingsPreferences;
    private DialogFragment dialogFragment;
    private Gson gson;
    private Context context;
    private Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.registration_toolbar);
        setToolbar(toolbar, true);

        appDataPreferences = getSharedPreferences(PRESENCE_DETECTION_PREFERENCES, MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();
        context = this;

        final String userId = appDataPreferences.getString(USER_ID, null);

        if (userId != null) {

            Log.d("userId", userId);
        }

        final boolean userIsRegistered = appDataPreferences.getBoolean(USER_IS_REGISTERED, false);

        if (!userIsRegistered) {

            showRegistrationDialog();
        }

        final Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                intent = new Intent(context, ScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showRegistrationDialog() {

        dialogFragment = BasicDialogFragment.newInstance(
                R.layout.dialog_fragment_registration, R.string.dialog_fragment_registration_title, R.string.activate);
        dialogFragment.show(getSupportFragmentManager(), "BasicDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(final DialogFragment dialog, final View view) {

        final EditText firstNameEditText = (EditText) view.findViewById(R.id.first_name);
        final EditText lastNameEditText = (EditText) view.findViewById(R.id.last_name);

        final String firstName = firstNameEditText.getText().toString();
        final String lastName = lastNameEditText.getText().toString();

        if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {

            Toast.makeText(this, "First name and last name can not be empty.", Toast.LENGTH_LONG).show();
            showRegistrationDialog();

        } else {

            //TODO: separate retrofit implementation

            final User user = new User(firstName, lastName);
            final String userJson = gson.toJson(user);

            final String serverUrl = settingsPreferences.getString(PREFERENCE_SERVER_URL_KEY, DEFAULT_SERVER_URL);
            final RetrofitManager retrofitManager = new RetrofitManager(serverUrl);

            Log.d("serverUrl", serverUrl);
            Log.d("inputString", "input=" + userJson);

            final Call<String> result = retrofitManager.getPresenceDetectionService().registerUser("input=" + userJson);

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(final Call<String> call, final Response<String> response) {

                    Log.d("response", response.body());

                    final String responseBody = response.body();

                    if (responseBody.contains("\"response_value\":\"200\"")) {

                        final UserId userIdObject = gson.fromJson(responseBody, UserId.class);
                        final String userId = userIdObject.getUserId();
                        Log.d("userId from server", userId);

                        appDataPreferences.edit().putString(USER_ID, userId).apply();
                        appDataPreferences.edit().putBoolean(USER_IS_REGISTERED, true).apply();

                        Toast.makeText(context, "The app is successfully activated.", Toast.LENGTH_LONG).show();

                        intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(final Call<String> call, final Throwable t) {

                    Log.d("onFailure", "failed");
                    t.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

        dialogFragment.getDialog().cancel();
        showRegistrationDialog();
    }
}
