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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import se.mogumogu.presencedetector.PresenceDetectorApplication;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.RangeHandler;
import se.mogumogu.presencedetector.adapter.BeaconAdapter;
import se.mogumogu.presencedetector.fragment.BasicDialogFragment;
import se.mogumogu.presencedetector.model.BeaconSubscription;
import se.mogumogu.presencedetector.model.SubscribedBeacon;
import se.mogumogu.presencedetector.rest.RetrofitManager;

@TargetApi(Build.VERSION_CODES.N)
public final class ScanActivity extends ToolbarProvider
        implements BeaconConsumer, BasicDialogFragment.BasicDialogListener, RangeNotifier {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private BeaconManager beaconManager;
    private Context context;
    private BeaconAdapter adapter;
    private List<Beacon> closeBeacons;
    private Region allBeaconsRegion;
    private Gson gson;
    private SharedPreferences appDataPreferences;
    private SharedPreferences settingsPreferences;
    private Set<SubscribedBeacon> subscribedBeacons;
    private Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.scan_toolbar);
        setToolbar(toolbar, false);

        context = this;
        closeBeacons = new ArrayList<>();

        initializeRecyclerView();
        initializeBeaconManager();

        gson = new Gson();
        appDataPreferences = getSharedPreferences(PresenceDetectorApplication.PRESENCE_DETECTOR_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String subscribedBeaconsJson = appDataPreferences.getString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, null);
        subscribedBeacons = PresenceDetectorApplication.initializeSubscribedBeacons(subscribedBeaconsJson);

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

        final String beaconJson = appDataPreferences.getString(PresenceDetectorApplication.BEACON_KEY, null);
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

        Log.d(TAG, "onPause");
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

            for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

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

            makeCloseBeaconList(beacons);
            sortBeaconsByRssi();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    adapter.notifyDataSetChanged();
                }
            });
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
                PresenceDetectorApplication.PREFERENCE_SERVER_URL_KEY, PresenceDetectorApplication.DEFAULT_SERVER_URL);
        final RetrofitManager retrofitManager = new RetrofitManager(serverUrl, context);

        final String userId = appDataPreferences.getString(PresenceDetectorApplication.USER_ID, null);
        final BeaconSubscription beaconSubscription = new BeaconSubscription(userId, beacon.getId1().toString());
        final String beaconSubscriptionJson = gson.toJson(beaconSubscription);

        final Call<String> result =
                retrofitManager.getPresenceDetectionService().subscribeBeacon("input=" + beaconSubscriptionJson);

        retrofitManager.setBeacon(beacon);
        retrofitManager.setView(view);
        retrofitManager.setDialogFragment(dialog);
        retrofitManager.handleResponse(result, TAG);
    }

    private void replaceBeaconToTheOneWithNewStatus(final Beacon beacon, final List<Beacon> beacons) {

        for (final Beacon aBeacon : beacons) {

            if (aBeacon.getIdentifiers().containsAll(beacon.getIdentifiers())) {

                beacons.set(beacons.indexOf(aBeacon), beacon);
            }
        }
    }

    private boolean isAlreadyDetected(final Beacon beacon, final List<Beacon> beacons) {

        for (final Beacon aBeacon : beacons) {

            if (RangeHandler.isSameBeacon(beacon, aBeacon)) {

                return true;
            }
        }

        return false;
    }

    private void makeCloseBeaconList(final Collection<Beacon> beacons) {

        for (final Beacon beacon : beacons) {

            Log.d("activeBeacon", beacon.toString() + " is about " + beacon.getDistance() + " meters away." + "serviceUUID " + beacon.getServiceUuid());
            Log.d("rssi", String.valueOf(beacon.getRssi()));

            if (RangeHandler.isCloseBeacon(beacon)) {

                if (isAlreadyDetected(beacon, closeBeacons)) {

                    replaceBeaconToTheOneWithNewStatus(beacon, closeBeacons);

                } else {

                    closeBeacons.add(beacon);
                }

                Log.d("closeBeacons", closeBeacons.toString());
            }
        }
    }

    private void sortBeaconsByRssi() {

        Collections.sort(closeBeacons, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon beacon1, Beacon beacon2) {

                return Integer.compare(beacon1.getRssi(), beacon2.getRssi());
            }
        });

        Collections.reverse(closeBeacons);
    }

    private void initializeRecyclerView() {

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scan);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new BeaconAdapter(context, closeBeacons, getSupportFragmentManager());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initializeBeaconManager() {

        allBeaconsRegion = new Region("allBeacons", null, null, null);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setRangeNotifier(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }
}
