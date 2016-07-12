package se.mogumogu.presencedetector;

import android.util.Log;

import org.altbeacon.beacon.Beacon;

import java.util.Set;

import se.mogumogu.presencedetector.model.SubscribedBeacon;

public class BeaconUtil {

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

    public void refreshRangeStatusToSubscribedBeacons(Beacon beacon, Set<SubscribedBeacon> subscribedBeacons){

        Log.d("BeaconUtil rssi", String.valueOf(beacon.getRssi()));
        for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

            if(beacon.getIdentifiers().containsAll(subscribedBeacon.getBeacon().getIdentifiers()) && (beacon.getRssi() >= -45 && beacon.getRssi() <= -20)){

                subscribedBeacon.setInRange(true);

            }else{

                subscribedBeacon.setInRange(false);
            }
        }
    }
}
