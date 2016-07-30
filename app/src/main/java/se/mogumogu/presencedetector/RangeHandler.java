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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.mogumogu.presencedetector.activity.RegistrationActivity;
import se.mogumogu.presencedetector.activity.ScanActivity;
import se.mogumogu.presencedetector.activity.SubscribedBeaconsActivity;
import se.mogumogu.presencedetector.adapter.SubscribedBeaconAdapter;
import se.mogumogu.presencedetector.model.BeaconInRange;
import se.mogumogu.presencedetector.model.BeaconOutOfRange;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public class RangeHandler implements RangeNotifier {

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
    private List<Beacon> beaconsToNotifyInRange;
    private String userId;
    private String timestamp;
    private RetrofitManager retrofitManager;
    private Set<Beacon> beaconsToNotifyOutOfRange;
    private Activity activity;
    private FragmentManager manager;
    private int rssiThreshold;

    public RangeHandler(Context context) {

        this.context = context;
        appDataPreferences = context.getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
        responseSuccess = "\"response_value\":\"200\"";

        subscribedBeaconsJson = appDataPreferences.getString(ScanActivity.SUBSCRIBED_BEACONS, null);

        if (subscribedBeaconsJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            Type typeSubscribedBeacon = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconsJson, typeSubscribedBeacon);
        }

        String defaultRssiThreshold = context.getString(R.string.rssi_threshold_default);
        String rssiThresholdString = settingsPreferences.getString(NumberPickerPreference.PREFERENCE_RSSI_THRESHOLD_KEY, defaultRssiThreshold);
        rssiThreshold = Integer.parseInt(rssiThresholdString);
    }

    public RangeHandler(Context context, Activity activity, FragmentManager manager) {

        this(context);
        this.activity = activity;
        this.manager = manager;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beaconsInRegion, Region region) {

        Log.d("RangeHandler", "didRange");
        Log.d("beaconsInRegion", beaconsInRegion.toString());
        userId = appDataPreferences.getString(RegistrationActivity.USER_ID, null);
        timestamp = appDataPreferences.getString(ScanActivity.TIMESTAMP, null);

        String serverUrl = settingsPreferences.getString(RegistrationActivity.PREFERENCE_SERVER_URL_KEY, RegistrationActivity.DEFAULT_SERVER_URL);
        retrofitManager = new RetrofitManager(serverUrl);

        List<Beacon> allBeaconsInRange = findAllBeaconsInRange();
        List<Beacon> beaconsInRegionInRange = findBeaconsInRegionInRange(beaconsInRegion, allBeaconsInRange);
        List<Beacon> beaconsWithLowRssiLevel = findBeaconsWithLowRssiLevel(beaconsInRegionInRange);
        beaconsToNotifyOutOfRange = new HashSet<>();

        Log.d("beaconsInRegion", beaconsInRegion.toString());
        Log.d("beaconsInRange", beaconsInRegionInRange.toString());
        Log.d("lowRssiBeacons", beaconsWithLowRssiLevel.toString());

        if (SubscribedBeaconsActivity.isActive) {

            if (beaconsInRegion.size() > 0) {

                for (Beacon beacon : beaconsInRegion) {

                    if (isCloseBeacon(beacon) && beaconIsSubscribed(beacon, subscribedBeacons)) {

                        refreshRangeStatusToSubscribedBeacons(beacon, subscribedBeacons);
                    }
                }
            }

            final RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_my_beacons);
            final RecyclerView.LayoutManager layoutManager;
            final SubscribedBeaconAdapter subscribedBeaconAdapter;

            layoutManager = new LinearLayoutManager(context);
            subscribedBeaconAdapter = new SubscribedBeaconAdapter(context, subscribedBeacons, manager);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(subscribedBeaconAdapter);
                }
            });
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
        if (!beaconsToNotifyOutOfRange.isEmpty()) {

            System.out.println("Kom in 1");

            notifyOutOfRange();

        } else if (!beaconsToNotifyInRange.isEmpty()) {

            notifyInRange();
        }
    }

    private List<Beacon> findAllBeaconsInRange() {

        List<Beacon> allBeaconsInRange = new ArrayList<>();

        for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.isInRangeNotified() && !subscribedBeacon.isOutOfRangeNotified()) {

                allBeaconsInRange.add(subscribedBeacon.getBeacon());
            }
        }
        return allBeaconsInRange;
    }

    private List<Beacon> findBeaconsInRegionInRange(Collection<Beacon> beaconsInRegion, List<Beacon> allBeaconsInRange) {

        List<Beacon> beaconsInRegionInRange = new ArrayList<>();

        for (Beacon beaconInRange : allBeaconsInRange) {

            for (Beacon beaconInRegion : beaconsInRegion) {

                if (beaconInRange.getIdentifiers().containsAll(beaconInRegion.getIdentifiers())) {

                    beaconsInRegionInRange.add(beaconInRegion);
                }
            }
        }
        return beaconsInRegionInRange;
    }

    private List<Beacon> findBeaconsWithLowRssiLevel(Collection<Beacon> beacons) {

        List<Beacon> beaconsWithLowRssi = new ArrayList<>();

        for (Beacon beacon : beacons) {

            if (beacon.getRssi() <= -70) {

                beaconsWithLowRssi.add(beacon);
            }
        }
        return beaconsWithLowRssi;
    }

    private List<Beacon> findInRangeUnNotifiedBeacons(Collection<Beacon> beaconsInRegion) {

        List<Beacon> inRangeUnNotifiedBeacons = new ArrayList<>();

        for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.isOutOfRangeNotified()) {

                String defaultIntervalValue = context.getString(R.string.default_interval_value);
                String reactivationIntervalString =
                        settingsPreferences.getString(PREFERENCE_REACTIVATION_INTERVAL_KEY, defaultIntervalValue);
                long reactivationInterval = Long.parseLong(reactivationIntervalString);

                Log.d("interval", reactivationIntervalString);

                //TODO: Ask how much the minimum interval must be

                long currentTime = System.currentTimeMillis();
                long outOfRangeTime = Long.parseLong(subscribedBeacon.getTimeStamp());

                Log.d("currentTime", String.valueOf(currentTime));
                Log.d("outOfRangeTime", String.valueOf(outOfRangeTime));
                Log.d("difference", String.valueOf(currentTime - outOfRangeTime));

                if ((currentTime - outOfRangeTime) >= reactivationInterval) {

                    subscribedBeacon.setInRangeNotified(false);
                    subscribedBeacon.setOutOfRangeNotified(false);
                }
            }

            for (Beacon beaconInRegion : beaconsInRegion) {
                Log.d("beaconRssi", String.valueOf(beaconInRegion.getRssi()));
                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beaconInRegion.getIdentifiers())
                        && !subscribedBeacon.isInRangeNotified()
                        && (beaconInRegion.getRssi() >= rssiThreshold && beaconInRegion.getRssi() <= -20)) {

                    Log.d("rssiThreshold", String.valueOf(rssiThreshold));

                    inRangeUnNotifiedBeacons.add(beaconInRegion);
                }
            }
        }
        return inRangeUnNotifiedBeacons;
    }

    private SubscribedBeacon findSubscribedBeacon(Beacon beacon) {

        for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                return subscribedBeacon;
            }
        }
        return null;
    }

    private void sendNotification(String title, String text, int resourceId) {

        Log.d("sendNotification", "came in");

        NotificationCompat.Builder notificationBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(resourceId)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{0, 100, 200, 300})
                        .setLights(0xFF0000FF, 100, 3000)
                        .setPriority(Notification.PRIORITY_DEFAULT);

        Intent intent = new Intent(context, BeaconDetailsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(BeaconDetailsActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent
                = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        Log.d("sendNotification", "notification sent");
    }

    private void notifyInRange() {

        for (final Beacon beaconToNotifyInRange : beaconsToNotifyInRange) {

            Log.d("beaconRssi", String.valueOf(beaconToNotifyInRange.getRssi()));

            BeaconInRange beaconInRange = new BeaconInRange(userId,
                    beaconToNotifyInRange.getId1().toString(),
                    beaconToNotifyInRange.getId2().toString(),
                    beaconToNotifyInRange.getId3().toString(),
                    String.valueOf(beaconToNotifyInRange.getRssi()),
                    timestamp);

            String beaconInRangeJson = gson.toJson(beaconInRange);

            final Call<String> result = retrofitManager.getPresenceDetectionService()
                    .setInRangeNotification("input=" + beaconInRangeJson);

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    Log.d("responseInRange", response.body());

                    String responseBody = response.body();

                    if (responseBody.contains(responseSuccess)) {

                        SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyInRange);

                        if (subscribedBeacon != null) {

                            subscribedBeacon.setInRangeNotified(true);
                            Log.d("inRangeNotified", String.valueOf(subscribedBeacon.isInRangeNotified()));
                            beaconAliasName = subscribedBeacon.getAliasName();
                        }

                        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
                        appDataPreferences.edit().putString(ScanActivity.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

                        sendNotification("Beacon in range", "Beacon " + beaconAliasName + " is in range.", R.drawable.icon_bluetooth_in_range);

                    } else {

                        Log.d("responseInRangeElse", responseBody);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    t.printStackTrace();
                }
            });
        }
    }

    public void notifyOutOfRange() {

        for (final Beacon beaconToNotifyOutOfRange : beaconsToNotifyOutOfRange) {

            final String timeStamp = String.valueOf(System.currentTimeMillis());

            BeaconOutOfRange beaconOutOfRange = new BeaconOutOfRange(userId,
                    beaconToNotifyOutOfRange.getId1().toString(),
                    timeStamp);

            String beaconOutOfRangeJson = gson.toJson(beaconOutOfRange);

            final Call<String> result = retrofitManager.getPresenceDetectionService().setOutOfRangeNotification("input=" + beaconOutOfRangeJson);

            result.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    String responseBody = response.body();

                    if (responseBody.contains(responseSuccess)) {

                        Log.d("responseOutOfRange", responseBody);

                        SubscribedBeacon subscribedBeacon = findSubscribedBeacon(beaconToNotifyOutOfRange);

                        if (subscribedBeacon != null) {

                            subscribedBeacon.setOutOfRangeNotified(true);
                            subscribedBeacon.setTimeStamp(timeStamp);
                            Log.d("outOfRangeNotified", String.valueOf(subscribedBeacon.isOutOfRangeNotified()));
                            beaconAliasName = subscribedBeacon.getAliasName();
                        }

                        subscribedBeaconsJson = gson.toJson(subscribedBeacons);
                        appDataPreferences.edit().putString(ScanActivity.SUBSCRIBED_BEACONS, subscribedBeaconsJson).apply();

                        sendNotification("Beacon out of range", "Beacon " + beaconAliasName + " is out of range.", R.drawable.icon_bluetooth_out_of_range);

                    } else {

                        Log.d("responseOutOfRange", responseBody);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    t.printStackTrace();
                }
            });
        }
    }

    public boolean isCloseBeacon(Beacon beacon) {

        return beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com");
    }

    public boolean beaconIsSubscribed(Beacon beacon, Set<SubscribedBeacon> subscribedBeacons) {

        if (subscribedBeacons != null) {

            for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                    return true;
                }
            }
        }
        return false;
    }

    public void refreshRangeStatusToSubscribedBeacons(Beacon beacon, Set<SubscribedBeacon> subscribedBeacons) {

        Log.d("rssi", String.valueOf(beacon.getRssi()));
        for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if (beacon.getIdentifiers().containsAll(subscribedBeacon.getBeacon().getIdentifiers()) && (beacon.getRssi() >= rssiThreshold && beacon.getRssi() <= -20)) {

                subscribedBeacon.setInRange(true);

            } else {

                subscribedBeacon.setInRange(false);
            }
        }
    }
}
