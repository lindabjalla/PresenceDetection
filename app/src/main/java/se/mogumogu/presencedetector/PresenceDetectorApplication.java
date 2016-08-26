package se.mogumogu.presencedetector;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class PresenceDetectorApplication extends Application implements BootstrapNotifier {

    private static final String TAG = PresenceDetectorApplication.class.getSimpleName();

    public static final String PRESENCE_DETECTOR_PREFERENCES = "se.mogumogu.presencedetector.PRESENCE_DETECTION_PREFERENCES";
    public static final String USER_IS_REGISTERED = "se.mogumogu.presencedetector.USER_IS_REGISTERED";
    public static final String USER_ID = "se.mogumogu.presencedetector.USER_ID";
    public static final String PREFERENCE_SERVER_URL_KEY = "preference_server_url_key";
    public static final String DEFAULT_SERVER_URL = "http://beacons.zenzor.io";
    public static final String SUBSCRIBED_BEACONS = "se.mogumogu.presencedetection.SUBSCRIBED_BEACONS";
    public static final String TIMESTAMP = "se.mogumogu.presencedetection.TIMESTAMP";
    public static final String BEACON_KEY = "se.mogumogu.presencedetection.BEACON_KEY";
    public static final String SUBSCRIBED_BEACON = "se.mogumogu.presencedetector.SUBSCRIBED_BEACON";
    public static final String PREFERENCE_RSSI_THRESHOLD_KEY = "preference_rssi_threshold_key";
    public static final String PREFERENCE_REACTIVATION_INTERVAL_KEY = "preference_reactivation_interval_key";
    public static final int NOTIFICATION_ID = 1;

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "App started up");

        final BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundBetweenScanPeriod(15000L);
        beaconManager.setBackgroundScanPeriod(1100L);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        final RegionBootstrap regionBootstrap = new RegionBootstrap(this, new Region("allBeacons", null, null, null));
    }

    @Override
    public void didEnterRegion(final Region region) {

        Log.d(TAG, "Got a didEnterRegion call");

        final RangeHandler rangeHandler = new RangeHandler(this);
        beaconManager.setRangeNotifier(rangeHandler);

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't start ranging");
        }
    }

    @Override
    public void didExitRegion(final Region region) {

        Log.d(TAG, "Got a didExitRegion call");

        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(final int i, final Region region) {
    }

    public static Set<SubscribedBeacon> initializeSubscribedBeacons(final String subscribedBeaconsJson) {

        Set<SubscribedBeacon> subscribedBeacons;
        final Gson gson = new Gson();

        if (subscribedBeaconsJson == null) {

            subscribedBeacons = new HashSet<>();

        } else {

            final Type typeSubscribedBeacon = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
            subscribedBeacons = gson.fromJson(subscribedBeaconsJson, typeSubscribedBeacon);
        }

        return subscribedBeacons;
    }
}
