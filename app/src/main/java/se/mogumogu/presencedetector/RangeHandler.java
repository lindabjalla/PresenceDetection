package se.mogumogu.presencedetector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import se.mogumogu.presencedetector.activity.SubscribedBeaconsActivity;
import se.mogumogu.presencedetector.adapter.SubscribedBeaconAdapter;
import se.mogumogu.presencedetector.model.BeaconInRange;
import se.mogumogu.presencedetector.model.BeaconOutOfRange;
import se.mogumogu.presencedetector.model.SubscribedBeacon;
import se.mogumogu.presencedetector.rest.RetrofitManager;

public final class RangeHandler implements RangeNotifier {

    private static final String NOTIFY_IN_RANGE = "notifyInRange";
    private static final String NOTIFY_OUT_OF_RANGE = "notifyOutOfRange";

    private Context context;
    private Activity activity;
    private SharedPreferences appDataPreferences;
    private SharedPreferences settingsPreferences;
    private Gson gson;
    private Set<SubscribedBeacon> subscribedBeacons;
    private List<SubscribedBeacon> subscribedBeaconList;
    private List<Beacon> beaconsToNotifyInRange;
    private Set<Beacon> beaconsToNotifyOutOfRange;
    private List<Beacon> inRangeUnNotifiedBeacons;
    private String userId;
    private int rssiThreshold;
    private RetrofitManager retrofitManager;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SubscribedBeaconAdapter subscribedBeaconAdapter;

    public RangeHandler(final Context context) {

        this.context = context;
        appDataPreferences = context.getSharedPreferences(PresenceDetectorApplication.PRESENCE_DETECTOR_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();

        final String subscribedBeaconsJson = appDataPreferences.getString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, null);
        subscribedBeacons = PresenceDetectorApplication.initializeSubscribedBeacons(subscribedBeaconsJson);
        subscribedBeaconList = new ArrayList<>();
        subscribedBeaconList.addAll(subscribedBeacons);
    }

    public RangeHandler(final Activity activity, final FragmentManager manager) {

        this(activity);
        this.activity = activity;
        recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_my_beacons);
        layoutManager = new LinearLayoutManager(activity);
        subscribedBeaconAdapter = new SubscribedBeaconAdapter(activity, subscribedBeaconList, manager);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(subscribedBeaconAdapter);
    }

    @Override
    public void didRangeBeaconsInRegion(final Collection<Beacon> beaconsInRegion, final Region region) {

        Log.d("RangeHandler", "didRange");
        Log.d("beaconsInRegion", beaconsInRegion.toString());

        initializeRssiThreshold();

        userId = appDataPreferences.getString(PresenceDetectorApplication.USER_ID, null);

        final String serverUrl = settingsPreferences.getString(
                PresenceDetectorApplication.PREFERENCE_SERVER_URL_KEY, PresenceDetectorApplication.DEFAULT_SERVER_URL);

        retrofitManager = new RetrofitManager(serverUrl, context);

        final List<Beacon> allBeaconsInRange = findAllBeaconsInRange();
        final List<Beacon> newBeaconsInRange = findNewBeaconsInRange(beaconsInRegion, allBeaconsInRange);
        final List<Beacon> beaconsWithLowRssiLevel = findBeaconsWithLowRssiLevel(newBeaconsInRange);
        beaconsToNotifyOutOfRange = new HashSet<>();

        Log.d("beaconsInRegion", beaconsInRegion.toString());
        Log.d("newBeaconsInRange", newBeaconsInRange.toString());
        Log.d("lowRssiBeacons", beaconsWithLowRssiLevel.toString());

        if (beaconsInRegion.size() > 0) {

            for (final Beacon beacon : beaconsInRegion) {

                if (isCloseBeacon(beacon) && beaconIsSubscribed(beacon, subscribedBeacons)) {

                    refreshSubscribedBeaconStatus(beacon, subscribedBeacons);
                }
            }

            if (SubscribedBeaconsActivity.active) {

                notifyDataSetChanged();
            }
        }
        if (!newBeaconsInRange.isEmpty() && beaconsInRegion.isEmpty()) {

            beaconsToNotifyOutOfRange.addAll(newBeaconsInRange);
        }
        if (!beaconsWithLowRssiLevel.isEmpty()) {

            beaconsToNotifyOutOfRange.addAll(beaconsWithLowRssiLevel);
        }

        beaconsToNotifyInRange = new ArrayList<>();
        beaconsToNotifyInRange = findInRangeUnNotifiedBeacons(beaconsInRegion);

        Log.d("beaconsOutOfRange", beaconsToNotifyOutOfRange.toString());
        Log.d("newBeaconsInRange", beaconsToNotifyInRange.toString());
        Log.d("subscribed beacons", subscribedBeacons.toString());
        Log.d("rssiThreshold", String.valueOf(rssiThreshold));

        if (!beaconsToNotifyOutOfRange.isEmpty()) {

            notifyOutOfRange();

        } else if (!beaconsToNotifyInRange.isEmpty()) {

            notifyInRange();
        }
    }

    private List<Beacon> findAllBeaconsInRange() {

        final List<Beacon> allBeaconsInRange = new ArrayList<>();

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.isInRangeNotified() && !subscribedBeacon.isOutOfRangeNotified()) {

                allBeaconsInRange.add(subscribedBeacon.getBeacon());
            }
        }

        return allBeaconsInRange;
    }

    private List<Beacon> findNewBeaconsInRange(final Collection<Beacon> beaconsInRegion, final List<Beacon> allBeaconsInRange) {

        final List<Beacon> newBeaconsInRange = new ArrayList<>();

        for (final Beacon beaconInRange : allBeaconsInRange) {

            for (final Beacon beaconInRegion : beaconsInRegion) {

                if (isSameBeacon(beaconInRange, beaconInRegion)) {

                    newBeaconsInRange.add(beaconInRegion);
                }
            }
        }

        return newBeaconsInRange;
    }

    private List<Beacon> findBeaconsWithLowRssiLevel(final Collection<Beacon> beacons) {

        final List<Beacon> beaconsWithLowRssi = new ArrayList<>();

        for (final Beacon beacon : beacons) {

            if (beacon.getRssi() <= -70) {

                beaconsWithLowRssi.add(beacon);
            }
        }

        return beaconsWithLowRssi;
    }

    private List<Beacon> findInRangeUnNotifiedBeacons(final Collection<Beacon> beaconsInRegion) {

        inRangeUnNotifiedBeacons = new ArrayList<>();

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.isOutOfRangeNotified()) {

                updateNotificationStatus(subscribedBeacon);
            }

            addInRangeUnNotifiedBeaconsToList(beaconsInRegion, subscribedBeacon);
        }

        return inRangeUnNotifiedBeacons;
    }

    private SubscribedBeacon findSubscribedBeacon(final Beacon beacon) {

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (isSameBeacon(beacon, subscribedBeacon.getBeacon())) {

                return subscribedBeacon;
            }
        }

        return null;
    }

    private void notifyInRange() {

        for (final Beacon beaconToNotifyInRange : beaconsToNotifyInRange) {

            Log.d("beaconRssi", String.valueOf(beaconToNotifyInRange.getRssi()));

            final long inRangeTime = System.currentTimeMillis();
            final String inRangeTimeStamp = String.valueOf(inRangeTime / 1000L);

            final BeaconInRange beaconInRange =
                    new BeaconInRange(userId,
                            beaconToNotifyInRange.getId1().toString(),
                            beaconToNotifyInRange.getId2().toString(),
                            beaconToNotifyInRange.getId3().toString(),
                            String.valueOf(beaconToNotifyInRange.getRssi()),
                            inRangeTimeStamp);

            final String beaconInRangeJson = gson.toJson(beaconInRange);

            final Call<String> result =
                    retrofitManager.getPresenceDetectionService().setInRangeNotification("input=" + beaconInRangeJson);

            final SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyInRange);

            retrofitManager.setSubscribedBeacon(subscribedBeacon);
            retrofitManager.setInRangeTime(inRangeTime);
            retrofitManager.handleResponse(result, NOTIFY_IN_RANGE);
        }
    }

    private void notifyOutOfRange() {

        for (final Beacon beaconToNotifyOutOfRange : beaconsToNotifyOutOfRange) {
            Log.d("beaconOutRssi", String.valueOf(beaconToNotifyOutOfRange.getRssi()));

            final long outOfRangeTime = System.currentTimeMillis();
            Log.d("outOfRangeTime", String.valueOf(outOfRangeTime));

            final String outOfRangeTimestamp = String.valueOf(outOfRangeTime / 1000L);
            Log.d("outOfRangeTimestamp", outOfRangeTimestamp);

            final BeaconOutOfRange beaconOutOfRange =
                    new BeaconOutOfRange(userId, beaconToNotifyOutOfRange.getId1().toString(), outOfRangeTimestamp);

            final String beaconOutOfRangeJson = gson.toJson(beaconOutOfRange);
            Log.d("beaconOutOfRangeJson", beaconOutOfRangeJson);

            final Call<String> result = retrofitManager.getPresenceDetectionService().setOutOfRangeNotification("input=" + beaconOutOfRangeJson);

            final SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyOutOfRange);
            retrofitManager.setSubscribedBeacon(subscribedBeacon);
            retrofitManager.setOutOfRangeTime(outOfRangeTime);
            retrofitManager.handleResponse(result, NOTIFY_OUT_OF_RANGE);
        }
    }

    private boolean beaconIsSubscribed(final Beacon beacon, final Set<SubscribedBeacon> subscribedBeacons) {

        if (subscribedBeacons != null) {

            for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                    return true;
                }
            }
        }

        return false;
    }

    private void refreshSubscribedBeaconStatus(final Beacon beacon, final Set<SubscribedBeacon> subscribedBeacons) {

        Log.d("rssi", String.valueOf(beacon.getRssi()));

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (isSameBeacon(beacon, subscribedBeacon.getBeacon())) {

                subscribedBeacon.setBeacon(beacon);
            }

            if (isSameBeacon(beacon, subscribedBeacon.getBeacon()) && isInRange(beacon)) {

                subscribedBeacon.setInRange(true);

            } else {

                subscribedBeacon.setInRange(false);
            }
        }
    }

    private boolean isInRange(final Beacon beacon) {

        return beacon.getRssi() >= rssiThreshold && beacon.getRssi() <= -20;
    }

    private void sortByInRangeStatus() {

        Collections.sort(subscribedBeaconList, new Comparator<SubscribedBeacon>() {
            @Override
            public int compare(SubscribedBeacon beacon1, SubscribedBeacon beacon2) {

                final int comparedRangeStatus = Boolean.compare(beacon2.isInRange(), beacon1.isInRange());

                if (comparedRangeStatus != 0) {

                    return comparedRangeStatus;

                } else {

                    Log.d("beacon1Name", beacon1.getAliasName());
                    Log.d("beacon2Name", beacon2.getAliasName());
                    Log.d("aliasNameCompare", String.valueOf(beacon1.getAliasName().compareTo(beacon2.getAliasName())));

                    return beacon1.getAliasName().toUpperCase().compareTo(beacon2.getAliasName().toUpperCase());
                }
            }
        });

        Log.d("sorted beacons", subscribedBeaconList.toString());
    }

    private void initializeRssiThreshold() {

        final String defaultRssiThreshold = context.getString(R.string.rssi_threshold_default);
        final String rssiThresholdString =
                settingsPreferences.getString(PresenceDetectorApplication.PREFERENCE_RSSI_THRESHOLD_KEY, defaultRssiThreshold);
        rssiThreshold = Integer.parseInt(rssiThresholdString);
    }

    private long initializeReactivationInterval() {

        final String defaultIntervalValue = context.getString(R.string.default_interval_value);
        final String reactivationIntervalString =
                settingsPreferences.getString(PresenceDetectorApplication.PREFERENCE_REACTIVATION_INTERVAL_KEY, defaultIntervalValue);

        return Long.parseLong(reactivationIntervalString);
    }

    private void notifyDataSetChanged(){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                sortByInRangeStatus();
                subscribedBeaconAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean exceedsReactivationInterval(final long currentTime, final long outOfRangeTime, final long reactivationInterval) {

        return (currentTime - outOfRangeTime) > reactivationInterval;
    }

    private void updateNotificationStatus(final SubscribedBeacon subscribedBeacon) {

        final long reactivationInterval = initializeReactivationInterval();
        Log.d("interval", String.valueOf(reactivationInterval));

        final long currentTime = System.currentTimeMillis();
        final long outOfRangeTime = subscribedBeacon.getOutOfRangeTime();

        Log.d("currentTime", String.valueOf(currentTime));
        Log.d("outOfRangeTime", String.valueOf(outOfRangeTime));
        Log.d("difference", String.valueOf(currentTime - outOfRangeTime));

        if (exceedsReactivationInterval(currentTime, outOfRangeTime, reactivationInterval)) {

            subscribedBeacon.setInRangeNotified(false);
            subscribedBeacon.setOutOfRangeNotified(false);
        }
    }

    private void addInRangeUnNotifiedBeaconsToList(
            final Collection<Beacon> beaconsInRegion, final SubscribedBeacon subscribedBeacon){

        for (final Beacon beaconInRegion : beaconsInRegion) {

            Log.d("beaconRssi", String.valueOf(beaconInRegion.getRssi()));

            if (isSameBeacon(beaconInRegion, subscribedBeacon.getBeacon())
                    && !subscribedBeacon.isInRangeNotified()
                    && isInRange(beaconInRegion)) {

                Log.d("rssiThreshold", String.valueOf(rssiThreshold));

                inRangeUnNotifiedBeacons.add(beaconInRegion);
            }
        }
    }

    public static boolean isCloseBeacon(final Beacon beacon) {

        return beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com");
    }

    public static boolean isSameBeacon(final Beacon beacon1, final Beacon beacon2) {

        return beacon1.getIdentifiers().containsAll(beacon2.getIdentifiers());
    }
}
