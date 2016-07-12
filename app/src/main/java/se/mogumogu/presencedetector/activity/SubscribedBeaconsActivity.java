package se.mogumogu.presencedetector.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.RangeHandler;
import se.mogumogu.presencedetector.dialogfragment.EditBeaconAliasNameDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public class SubscribedBeaconsActivity extends AppCompatActivity implements BeaconConsumer, EditBeaconAliasNameDialogFragment.EditBeaconAliasNameDialogListener {

    private static final String TAG = SubscribedBeaconsActivity.class.getSimpleName();
    public static boolean isActive;

    private Context context;
    private BeaconManager beaconManager;
    private Region allBeaconsRegion;
    private String subscribedBeaconsJson;
    private Set<SubscribedBeacon> subscribedBeacons;
    private Gson gson;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_beacons);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_beacons_toolbar);
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        context = this;
        allBeaconsRegion = new Region("allBeacons", null, null, null);
        preferences = getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
        RangeHandler rangeHandler = new RangeHandler(preferences, this, this, getSupportFragmentManager());

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setRangeNotifier(rangeHandler);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        gson = new Gson();
        subscribedBeaconsJson = preferences.getString(ScanActivity.SUBSCRIBED_BEACONS, null);

        if (subscribedBeaconsJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            Type typeSubscribedBeacon = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconsJson, typeSubscribedBeacon);
        }
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

        switch (item.getItemId()) {

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

    @Override
    public void onBeaconServiceConnect() {

        try {
            beaconManager.startRangingBeaconsInRegion(allBeaconsRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        isActive = true;
    }

    @Override
    protected void onPause() {

        Log.d("onPause", "onPause");
        super.onPause();
        beaconManager.unbind(this);
        Log.d("beaconManager", beaconManager.getRangingNotifier().toString());
    }

    @Override
    protected void onStop() {

        super.onStop();
        isActive = false;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, View view) {

        EditText editText = (EditText) view.findViewById(R.id.alias_name_edit);
        String aliasName = editText.getText().toString();
        final Bundle arguments = dialog.getArguments();
        final SubscribedBeacon subscribedBeacon = (SubscribedBeacon) arguments.getSerializable(EditBeaconAliasNameDialogFragment.SUBSCRIBED_BEACON);

        if (subscribedBeacon != null) {

            subscribedBeacons.remove(subscribedBeacon);
            subscribedBeacons.add(subscribedBeacon.setAliasName(aliasName));
            subscribedBeaconsJson = gson.toJson(subscribedBeacons);
            preferences.edit().putString(ScanActivity.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

        } else {

            Log.d(TAG, "subscribed beacon is null");
        }

        Toast.makeText(context, "The changes were successfully saved.", Toast.LENGTH_LONG).show();
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        dialog.dismiss();
    }
}
