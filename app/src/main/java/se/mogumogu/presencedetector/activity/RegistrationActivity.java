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
import se.mogumogu.presencedetector.PresenceDetectorApplication;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.BasicDialogFragment;
import se.mogumogu.presencedetector.model.User;
import se.mogumogu.presencedetector.rest.RetrofitManager;

public final class RegistrationActivity extends ToolbarProvider implements BasicDialogFragment.BasicDialogListener {

    private static final String TAG = RegistrationActivity.class.getSimpleName();

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

        final SharedPreferences appDataPreferences = getSharedPreferences(PresenceDetectorApplication.PRESENCE_DETECTOR_PREFERENCES, MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gson = new Gson();
        context = this;

        final String userId = appDataPreferences.getString(PresenceDetectorApplication.USER_ID, null);
        Log.d("userId", userId != null ? userId : "null");

        final boolean userIsRegistered = appDataPreferences.getBoolean(PresenceDetectorApplication.USER_IS_REGISTERED, false);

        if (!userIsRegistered) {

            showRegistrationDialog();
        }

        initializeScanButton();
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

            final User user = new User(firstName, lastName);
            final String userJson = gson.toJson(user);

            final String serverUrl = settingsPreferences.getString(
                    PresenceDetectorApplication.PREFERENCE_SERVER_URL_KEY, PresenceDetectorApplication.DEFAULT_SERVER_URL);
            final RetrofitManager retrofitManager = new RetrofitManager(serverUrl, context);

            Log.d("serverUrl", serverUrl);
            Log.d("inputString", "input=" + userJson);

            final Call<String> result = retrofitManager.getPresenceDetectionService().registerUser("input=" + userJson);
            retrofitManager.handleResponse(result, TAG);
        }
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

        dialogFragment.getDialog().cancel();
        showRegistrationDialog();
    }

    private void initializeScanButton() {

        final Button scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                intent = new Intent(context, ScanActivity.class);
                startActivity(intent);
            }
        });
    }
}
