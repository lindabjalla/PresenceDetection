package se.mogumogu.presencedetection.Activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.mogumogu.presencedetection.BeaconAdapter;
import se.mogumogu.presencedetection.DialogFragment.SubscriptionDialogFragment;
import se.mogumogu.presencedetection.R;
import se.mogumogu.presencedetection.RetrofitManager;
import se.mogumogu.presencedetection.model.BeaconSubscription;
import se.mogumogu.presencedetection.model.Timestamp;

@TargetApi(Build.VERSION_CODES.N)
public class ScanActivity extends FragmentActivity implements BeaconConsumer, SubscriptionDialogFragment.SubscriptionDialogListener {

    public static final String SUBSCRIBED_BEACONS = "se.mogumogu.presencedetection.SUBSCRIBED_BEACONS";
    public static final String TIMESTAMP = "se.mogumogu.presencedetection.TIMESTAMP";

    protected static final String TAG = ScanActivity.class.getSimpleName();
    private BeaconManager beaconManager;
    private Context context = this;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private BeaconAdapter adapter;
    private Set<Beacon> closeBeacons;
    private final Region ALL_BEACONS_REGION = new Region("allBeacons", null, null, null);
    private Gson gson;
    private SharedPreferences preferences;
    private String subscribedBeaconSetJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scan);
        layoutManager = new LinearLayoutManager(context);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.setRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                Log.d("all beacons", beacons.toString());
                closeBeacons = new HashSet<>();

                recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scan);
                layoutManager = new LinearLayoutManager(context);

                if (beacons.size() > 0) {

                    for (Beacon beacon : beacons) {

                        if (beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com")) {

                            closeBeacons.add(beacon);
                            Log.d("activeBeacon", beacon.toString() + " is about " + beacon.getDistance() + " meters away." + "serviceUUID " + beacon.getServiceUuid());
                        }
                    }
                }

                adapter = new BeaconAdapter(context, closeBeacons, getSupportFragmentManager());

                try {
                    beaconManager.stopRangingBeaconsInRegion(ALL_BEACONS_REGION);

                } catch (RemoteException e) {

                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(ALL_BEACONS_REGION);

        } catch (RemoteException e) {

            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onDialogPositiveClick(final DialogFragment dialog, View view) {

        preferences = context.getSharedPreferences(MainActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);

        subscribedBeaconSetJson = preferences.getString(SUBSCRIBED_BEACONS, null);

        if (subscribedBeaconSetJson != null) {

            Log.d("subscribedBeacons", subscribedBeaconSetJson);
        }

        final Set<Beacon> subscribedBeacons;
        gson = new Gson();

        if (subscribedBeaconSetJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            Type type = new TypeToken<Set<Beacon>>() {
            }.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconSetJson, type);
        }

        String beaconJson = preferences.getString(BeaconAdapter.BEACON_KEY, null);
        final Beacon beacon = gson.fromJson(beaconJson, Beacon.class);

        for (Beacon aBeacon : subscribedBeacons) {

            if (beacon.getId1() != aBeacon.getId1() && beacon.getId2() != aBeacon.getId2() && beacon.getId3() != aBeacon.getId3()) {

                RetrofitManager retrofitManager = new RetrofitManager();
                final String userId = preferences.getString(MainActivity.USER_ID, null);
                BeaconSubscription beaconSubscription = new BeaconSubscription(userId, beacon.getId1().toString());
                String beaconSubscriptionJson = gson.toJson(beaconSubscription);

                Call<String> result = retrofitManager.getPresenceDetectionService().subscribeBeacon("input=" + beaconSubscriptionJson);
                result.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        Log.d("response", response.body());

                        String responseBody = response.body();

                        if (responseBody.contains("\"response_value\":\"200\"")) {

                            Timestamp timestampObject = gson.fromJson(responseBody, Timestamp.class);
                            final String timestamp = timestampObject.getTimestamp();
                            Log.d("timestamp from server", timestamp);

                            subscribedBeacons.add(beacon);
                            subscribedBeaconSetJson = gson.toJson(subscribedBeacons);
                            preferences.edit().putString(TIMESTAMP, timestamp).apply();
                            preferences.edit().putString(SUBSCRIBED_BEACONS, subscribedBeaconSetJson).apply();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                        t.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        dialog.dismiss();
    }
}
