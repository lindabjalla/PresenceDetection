package se.mogumogu.presencedetector;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.mogumogu.presencedetector.activity.BeaconDetailsActivity;
import se.mogumogu.presencedetector.activity.RegistrationActivity;
import se.mogumogu.presencedetector.activity.ScanActivity;
import se.mogumogu.presencedetector.activity.SubscribedBeaconsActivity;
import se.mogumogu.presencedetector.adapter.SubscribedBeaconAdapter;
import se.mogumogu.presencedetector.fragment.BeaconAliasNameDialogFragment;
import se.mogumogu.presencedetector.model.BeaconInRange;
import se.mogumogu.presencedetector.model.BeaconOutOfRange;
import se.mogumogu.presencedetector.model.SubscribedBeacon;
import se.mogumogu.presencedetector.rest.RetrofitManager;

public final class RangeHandler implements RangeNotifier {

    public static final int NOTIFICATION_ID = 1;
    public static final String PREFERENCE_REACTIVATION_INTERVAL_KEY = "preference_reactivation_interval_key";

    private Context context;
    private SharedPreferences appDataPreferences;
    private SharedPreferences settingsPreferences;
    private Gson gson;
    private String responseSuccess;
    private String beaconAliasName;
    private String subscribedBeaconsJson;
    private Set<SubscribedBeacon> subscribedBeacons;
    private List<SubscribedBeacon> subscribedBeaconList;
    private List<Beacon> beaconsToNotifyInRange;
    private String userId;
    private RetrofitManager retrofitManager;
    private Set<Beacon> beaconsToNotifyOutOfRange;
    private int rssiThreshold;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SubscribedBeaconAdapter subscribedBeaconAdapter;
    private Activity activity;

    public RangeHandler(final Context context) {

        this.context = context;
        appDataPreferences = context.getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
        responseSuccess = "\"response_value\":\"200\"";

        subscribedBeaconsJson = appDataPreferences.getString(ScanActivity.SUBSCRIBED_BEACONS, null);

        if (subscribedBeaconsJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            final Type typeSubscribedBeacon = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconsJson, typeSubscribedBeacon);
        }

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

        final String defaultRssiThreshold = context.getString(R.string.rssi_threshold_default);
        final String rssiThresholdString =
                settingsPreferences.getString(NumberPickerPreference.PREFERENCE_RSSI_THRESHOLD_KEY, defaultRssiThreshold);
        rssiThreshold = Integer.parseInt(rssiThresholdString);

        userId = appDataPreferences.getString(RegistrationActivity.USER_ID, null);

        final String serverUrl = settingsPreferences.getString(
                RegistrationActivity.PREFERENCE_SERVER_URL_KEY, RegistrationActivity.DEFAULT_SERVER_URL);

        retrofitManager = new RetrofitManager(serverUrl);

        final List<Beacon> allBeaconsInRange = findAllBeaconsInRange();
        final List<Beacon> beaconsInRegionInRange = findBeaconsInRegionInRange(beaconsInRegion, allBeaconsInRange);
        final List<Beacon> beaconsWithLowRssiLevel = findBeaconsWithLowRssiLevel(beaconsInRegionInRange);
        beaconsToNotifyOutOfRange = new HashSet<>();

        Log.d("beaconsInRegion", beaconsInRegion.toString());
        Log.d("beaconsInRange", beaconsInRegionInRange.toString());
        Log.d("lowRssiBeacons", beaconsWithLowRssiLevel.toString());

        if (beaconsInRegion.size() > 0) {

            for (final Beacon beacon : beaconsInRegion) {

                if (isCloseBeacon(beacon) && beaconIsSubscribed(beacon, subscribedBeacons)) {

                    refreshSubscribedBeaconStatus(beacon, subscribedBeacons);
                }
            }

            if (SubscribedBeaconsActivity.active) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        sortByInRangeStatus();
                        subscribedBeaconAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        if (!beaconsInRegionInRange.isEmpty() && beaconsInRegion.isEmpty()) {

            beaconsToNotifyOutOfRange.addAll(beaconsInRegionInRange);
        }
        if (!beaconsWithLowRssiLevel.isEmpty()) {

            beaconsToNotifyOutOfRange.addAll(beaconsWithLowRssiLevel);
        }

        beaconsToNotifyInRange = new ArrayList<>();
        beaconsToNotifyInRange = findInRangeUnNotifiedBeacons(beaconsInRegion);

        Log.d("beaconsOutOfRange", beaconsToNotifyOutOfRange.toString());
        Log.d("beaconsInRange", beaconsToNotifyInRange.toString());
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

    private List<Beacon> findBeaconsInRegionInRange(final Collection<Beacon> beaconsInRegion, final List<Beacon> allBeaconsInRange) {

        final List<Beacon> beaconsInRegionInRange = new ArrayList<>();

        for (final Beacon beaconInRange : allBeaconsInRange) {

            for (final Beacon beaconInRegion : beaconsInRegion) {

                if (beaconInRange.getIdentifiers().containsAll(beaconInRegion.getIdentifiers())) {

                    beaconsInRegionInRange.add(beaconInRegion);
                }
            }
        }

        return beaconsInRegionInRange;
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

        final List<Beacon> inRangeUnNotifiedBeacons = new ArrayList<>();

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.isOutOfRangeNotified()) {

                final String defaultIntervalValue = context.getString(R.string.default_interval_value);
                final String reactivationIntervalString =
                        settingsPreferences.getString(PREFERENCE_REACTIVATION_INTERVAL_KEY, defaultIntervalValue);
                long reactivationInterval = Long.parseLong(reactivationIntervalString);

                Log.d("interval", reactivationIntervalString);

                long currentTime = System.currentTimeMillis();
                long outOfRangeTime = subscribedBeacon.getOutOfRangeTime();

                Log.d("currentTime", String.valueOf(currentTime));
                Log.d("outOfRangeTime", String.valueOf(outOfRangeTime));
                Log.d("difference", String.valueOf(currentTime - outOfRangeTime));

                if ((currentTime - outOfRangeTime) >= reactivationInterval) {

                    subscribedBeacon.setInRangeNotified(false);
                    subscribedBeacon.setOutOfRangeNotified(false);
                }
            }

            for (final Beacon beaconInRegion : beaconsInRegion) {

                Log.d("beaconRssi", String.valueOf(beaconInRegion.getRssi()));

                if (isSameBeacon(beaconInRegion, subscribedBeacon)
                        && !subscribedBeacon.isInRangeNotified()
                        && isInRange(beaconInRegion)) {

                    Log.d("rssiThreshold", String.valueOf(rssiThreshold));

                    inRangeUnNotifiedBeacons.add(beaconInRegion);
                }
            }
        }

        return inRangeUnNotifiedBeacons;
    }

    private SubscribedBeacon findSubscribedBeacon(final Beacon beacon) {

        for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                return subscribedBeacon;
            }
        }

        return null;
    }

    private void sendNotification(final String title,
                                  final String text,
                                  final int resourceId,
                                  final SubscribedBeacon subscribedBeacon) {

        Log.d("sendNotification", "came in");

        final NotificationCompat.Builder notificationBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(resourceId)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{0, 100, 200, 300})
                        .setLights(0xFF0000FF, 100, 3000)
                        .setPriority(Notification.PRIORITY_DEFAULT);

        final Intent intent = new Intent(context, BeaconDetailsActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(BeaconAliasNameDialogFragment.SUBSCRIBED_BEACON, subscribedBeacon);
        intent.putExtras(bundle);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(BeaconDetailsActivity.class);
        stackBuilder.addNextIntent(intent);

        final PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        Log.d("sendNotification", "notification sent");
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

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(final Call<String> call, final Response<String> response) {

                    Log.d("responseInRange", response.body());

                    final String responseBody = response.body();

                    if (responseBody.contains(responseSuccess)) {

                        final SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyInRange);

                        if (subscribedBeacon != null) {

                            subscribedBeacon.setInRangeTime(inRangeTime);
                            subscribedBeacon.setInRangeNotified(true);

                            Log.d("inRangeNotified", String.valueOf(subscribedBeacon.isInRangeNotified()));

                            beaconAliasName = subscribedBeacon.getAliasName();
                        }

                        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
                        appDataPreferences.edit().putString(ScanActivity.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

                        sendNotification("Beacon in range", "Beacon " + beaconAliasName + " came in range.",
                                R.drawable.icon_bluetooth_in_range, subscribedBeacon);

                    } else {

                        Log.d("responseInRangeElse", responseBody);
                    }
                }

                @Override
                public void onFailure(final Call<String> call, final Throwable t) {

                    t.printStackTrace();
                }
            });
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

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(final Call<String> call, final Response<String> response) {

                    final String responseBody = response.body();

                    if (responseBody.contains(responseSuccess)) {

                        Log.d("responseOutOfRange", responseBody);

                        final SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyOutOfRange);

                        if (subscribedBeacon != null) {

                            Log.d("sBeaconOutRssi", String.valueOf(subscribedBeacon.getBeacon().getRssi()));

                            subscribedBeacon.setOutOfRangeNotified(true);
                            subscribedBeacon.setOutOfRangeTime(outOfRangeTime);

                            Log.d("outOfRangeNotified", String.valueOf(subscribedBeacon.isOutOfRangeNotified()));

                            beaconAliasName = subscribedBeacon.getAliasName();
                        }

                        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
                        appDataPreferences.edit().putString(ScanActivity.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

                        sendNotification("Beacon out of range", "Beacon " + beaconAliasName + " went out of range.",
                                R.drawable.icon_bluetooth_out_of_range, subscribedBeacon);

                    } else {

                        Log.d("responseOutOfRange", responseBody);
                    }
                }

                @Override
                public void onFailure(final Call<String> call, final Throwable t) {

                    t.printStackTrace();
                }
            });
        }
    }

    private boolean isCloseBeacon(final Beacon beacon) {

        return beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com");
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

            if (isSameBeacon(beacon, subscribedBeacon)) {

                subscribedBeacon.setBeacon(beacon);
            }

            if (isSameBeacon(beacon, subscribedBeacon) && isInRange(beacon)) {

                subscribedBeacon.setInRange(true);

            } else {

                subscribedBeacon.setInRange(false);
            }
        }
    }

    private boolean isSameBeacon(final Beacon beacon, final SubscribedBeacon subscribedBeacon) {

        return beacon.getIdentifiers().containsAll(subscribedBeacon.getBeacon().getIdentifiers());
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
}
