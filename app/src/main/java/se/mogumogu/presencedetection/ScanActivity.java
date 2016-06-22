package se.mogumogu.presencedetection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.N)
public class ScanActivity extends Activity implements BeaconConsumer {

    protected static final String TAG = ScanActivity.class.getSimpleName();
    private BeaconManager beaconManager;
    private Context context = this;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ActiveBeaconAdapter adapter;
    private Set<Beacon> activeBeacons;
    private final Region ALL_BEACONS_REGION = new Region("allBeacons", null, null, null);

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

                activeBeacons = new LinkedHashSet<>();

                recyclerView = (RecyclerView) findViewById(R.id.recycler_view_scan);
                layoutManager = new LinearLayoutManager(context);

                if (beacons.size() > 0) {

                    for (Beacon beacon : beacons) {

                        if (beacon.getId1() != null && beacon.getBluetoothName().equals("closebeacon.com")) {

                            activeBeacons.add(beacon);
                            Log.d("activeBeacon", beacon.toString() + " is about " + beacon.getDistance() + " meters away." + "serviceUUID " + beacon.getServiceUuid());
                        }
                    }
                }

                adapter = new ActiveBeaconAdapter(context, activeBeacons);

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
}
