package se.mogumogu.presencedetector.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.dialogfragment.RegistrationDialogFragment;
import se.mogumogu.presencedetector.RetrofitManager;
import se.mogumogu.presencedetector.model.User;
import se.mogumogu.presencedetector.model.UserId;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity implements RegistrationDialogFragment.RegistrationDialogListener {

    public static final String PRESENCE_DETECTION_PREFERENCES = "se.mogumogu.presencedetection.PRESENCE_DETECTION_PREFERENCES";
    public static final String USER_IS_REGISTERED = "se.mogumogu.presencedetection.USER_IS_REGISTERED";
    public static final String USER_ID = "se.mogumogu.presencedetection.USER_ID";

    private SharedPreferences preferences;
    private DialogFragment dialogFragment;
    private Gson gson;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.registration_toolbar);
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

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

                Intent intent = new Intent(context, ScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showRegistrationDialog() {

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

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch(item.getItemId()){

            case R.id.menu_item_my_beacons:
                intent = new Intent(this, SubscribedBeaconsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_settings:
                intent = new Intent(this, SettingsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_help:
                intent = new Intent(this, HelpActivity.class);
                context.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
