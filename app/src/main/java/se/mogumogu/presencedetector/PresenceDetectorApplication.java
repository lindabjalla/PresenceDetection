package se.mogumogu.presencedetector;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class PresenceDetectorApplication extends Application implements BootstrapNotifier {

    private static final String TAG = PresenceDetectorApplication.class.getSimpleName();

    private BeaconManager beaconManager;
    private Context context = this;

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "App started up");

        BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundBetweenScanPeriod(15000L);
        beaconManager.setBackgroundScanPeriod(1100L);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        RegionBootstrap regionBootstrap = new RegionBootstrap(this, new Region("allBeacons", null, null, null));
    }

    @Override
    public void didEnterRegion(Region region) {

        Log.d(TAG, "Got a didEnterRegion call");

        RangeHandler rangeHandler = new RangeHandler(context);
        beaconManager.setRangeNotifier(rangeHandler);

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't start ranging");
        }
    }

    @Override
    public void didExitRegion(Region region) {

        Log.d(TAG, "Got a didExitRegion call");

        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
    }
}
