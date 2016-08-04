package se.mogumogu.presencedetector.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.RangeHandler;
import se.mogumogu.presencedetector.rest.RetrofitManager;
import se.mogumogu.presencedetector.adapter.BeaconAdapter;
import se.mogumogu.presencedetector.fragment.SubscriptionDialogFragment;
import se.mogumogu.presencedetector.model.BeaconSubscription;
import se.mogumogu.presencedetector.model.SubscribedBeacon;
import se.mogumogu.presencedetector.model.Timestamp;

@TargetApi(Build.VERSION_CODES.N)
public final class ScanActivity extends ToolbarProvider
        implements BeaconConsumer, SubscriptionDialogFragment.SubscriptionDialogListener, RangeNotifier {

    private static final String TAG = ScanActivity.class.getSimpleName();

    public static final String SUBSCRIBED_BEACONS = "se.mogumogu.presencedetection.SUBSCRIBED_BEACONS";
    public static final String TIMESTAMP = "se.mogumogu.presencedetection.TIMESTAMP";

    private BeaconManager beaconManager;
    private Context context = this;
    private BeaconAdapter adapter;
    private List<Beacon> closeBeacons;
    private Region allBeaconsRegion;
    private Gson gson;
    private SharedPreferences appDataPreferences;
    private SharedPreferences settingsPreferences;
    private String subscribedBeaconsJson;
    private Set<SubscribedBeacon> subscribedBeacons;
    private Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final Toolbar myToolbar = (Toolbar) findViewById(R.id.beacon_details_toolbar);
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        closeBeacons = new ArrayList<>();

        allBeaconsRegion = new Region("allBeacons", null, null, null);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scan);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new BeaconAdapter(context, closeBeacons, getSupportFragmentManager());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setRangeNotifier(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        gson = new Gson();
        appDataPreferences = getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        subscribedBeaconsJson = appDataPreferences.getString(SUBSCRIBED_BEACONS, null);

        if (subscribedBeaconsJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            Type typeSubscribedBeacon = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconsJson, typeSubscribedBeacon);
        }

        intent = new Intent(context, RegistrationActivity.class);
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
    public void onDialogPositiveClick(final DialogFragment dialog, final View view) {

        final String beaconJson = appDataPreferences.getString(BeaconAdapter.BEACON_KEY, null);
        final Beacon beacon = gson.fromJson(beaconJson, Beacon.class);

        if (beaconIsSubscribed(beacon)) {

            Toast.makeText(context, "This beacon is previously subscribed", Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

        subscribeBeacon(beacon, view, dialog);
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

        dialog.dismiss();
        startActivity(intent);
    }

    @Override
    protected void onPause() {

        Log.d("onPause", "onPause");
        super.onPause();
        final RangeHandler rangeHandler = new RangeHandler(context);
        beaconManager.unbind(this);
        beaconManager.setRangeNotifier(rangeHandler);

        try {
            beaconManager.startRangingBeaconsInRegion(allBeaconsRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d("beaconManager", beaconManager.getRangingNotifier().toString());
    }

    @Override
    protected void onStop() {

        Log.d("onStop", "onStop");
        super.onStop();
    }

    private boolean beaconIsSubscribed(final Beacon beacon) {

        if (subscribedBeacons != null) {

            for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, final Region region) {

        Log.d("ScanActivity", "rangeNotifier");
        Log.d("all beacons", beacons.toString());

        if (beacons.size() > 0) {

            for (final Beacon beacon : beacons) {

                Log.d("activeBeacon", beacon.toString() + " is about " + beacon.getDistance() + " meters away." + "serviceUUID " + beacon.getServiceUuid());
                Log.d("rssi", String.valueOf(beacon.getRssi()));

                if (beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com")) {

                    if (isAlreadyDetected(beacon, closeBeacons)) {

                        replaceBeaconToTheOneWithNewStatus(beacon, closeBeacons);

                    } else {

                        closeBeacons.add(beacon);
                    }
                    Log.d("closeBeacons", closeBeacons.toString());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }

        try {

            beaconManager.startRangingBeaconsInRegion(allBeaconsRegion);

        } catch (RemoteException e) {

            Log.d(TAG, e.toString());
        }
    }

    private void subscribeBeacon(final Beacon beacon, final View view, final DialogFragment dialog) {

        Log.d("subscribeBeacon", "came in");

        final String serverUrl = settingsPreferences.getString(
                RegistrationActivity.PREFERENCE_SERVER_URL_KEY, RegistrationActivity.DEFAULT_SERVER_URL);
        final RetrofitManager retrofitManager = new RetrofitManager(serverUrl);

        final String userId = appDataPreferences.getString(RegistrationActivity.USER_ID, null);
        final BeaconSubscription beaconSubscription = new BeaconSubscription(userId, beacon.getId1().toString());
        final String beaconSubscriptionJson = gson.toJson(beaconSubscription);

        final Call<String> result
                = retrofitManager.getPresenceDetectionService().subscribeBeacon("input=" + beaconSubscriptionJson);
        result.enqueue(new Callback<String>() {
            @Override
            public void onResponse(final Call<String> call, final Response<String> response) {

                Log.d("response", response.body());

                final String responseBody = response.body();

                if (responseBody.contains("\"response_value\":\"200\"")) {

                    final Timestamp timestampObject = gson.fromJson(responseBody, Timestamp.class);
                    final String timestamp = timestampObject.getTimestamp();
                    Log.d("timestamp from server", timestamp);

                    final EditText aliasNameEditText = (EditText) view.findViewById(R.id.alias_name);
                    final String aliasName = aliasNameEditText.getText().toString();

                    subscribedBeacons.add(new SubscribedBeacon(aliasName, beacon));
                    subscribedBeaconsJson = gson.toJson(subscribedBeacons);
                    appDataPreferences.edit().putString(TIMESTAMP, timestamp).apply();
                    appDataPreferences.edit().putString(SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();
                    dialog.dismiss();

                    Toast.makeText(context, aliasName + " is successfully subscribed.", Toast.LENGTH_LONG).show();
                    startActivity(intent);

                } else {

                    Log.d("response", responseBody);
                }
            }

            @Override
            public void onFailure(final Call<String> call, final Throwable t) {

                t.printStackTrace();
            }
        });
    }

    private void replaceBeaconToTheOneWithNewStatus(final Beacon beacon, final List<Beacon> beacons) {

        for (Beacon aBeacon : beacons) {

            if (aBeacon.getIdentifiers().containsAll(beacon.getIdentifiers())) {

                beacons.set(beacons.indexOf(aBeacon), beacon);
            }
        }
    }

    private boolean isAlreadyDetected(final Beacon beacon, final List<Beacon> beacons) {

        for (Beacon aBeacon : beacons) {

            if (aBeacon.getIdentifiers().containsAll(beacon.getIdentifiers())) {

                return true;
            }
        }
        return false;
    }
}
