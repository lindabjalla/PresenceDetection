package mogumogu.se.presencedetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends FragmentActivity implements RegistrationDialogFragment.RegistrationDialogListener {

    public static final String PRESENCE_DETECTION_PREFERENCES = "se.mogumogu.presencedetection.PRESENCE_DETECTION_PREFERENCES";
    public static final String USER_IS_REGISTERED = "se.mogumogu.presencedetection.USER_IS_REGISTERED";
    public static final String FIRST_NAME = "se.mogumogu.presencedetection.FIRST_NAME";
    public static final String LAST_NAME = "se.mogumogu.presencedetection.LAST_NAME";

    private SharedPreferences preferences;
    private DialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//      bara för test
        preferences.edit().putBoolean(USER_IS_REGISTERED, true).apply();

        preferences = getSharedPreferences(PRESENCE_DETECTION_PREFERENCES, MODE_PRIVATE);
        boolean userIsRegistered = preferences.getBoolean(USER_IS_REGISTERED, false);

        if (userIsRegistered) {

            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);

        } else {

            showRegistrationDialog();
        }

        setContentView(R.layout.activity_registration);
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

            Toast.makeText(this, "First name and last name can not be empty", Toast.LENGTH_SHORT).show();

        } else {

            preferences.edit().putBoolean(USER_IS_REGISTERED, true).apply();

            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(FIRST_NAME, firstName);
            intent.putExtra(LAST_NAME, lastName);
            startActivity(intent);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        dialogFragment.getDialog().cancel();
    }
}
