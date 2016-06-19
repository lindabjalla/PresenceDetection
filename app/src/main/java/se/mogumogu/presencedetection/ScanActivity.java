package se.mogumogu.presencedetection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.M)
public class ScanActivity extends Activity implements BeaconConsumer {

    protected static final String TAG = ScanActivity.class.getSimpleName();
    private BeaconManager beaconManager;
    private Context context = this;
//    private RecyclerView recyclerView;
//    private RecyclerView.LayoutManager layoutManager;
//    private ActiveBeaconAdapter adapter;
    private Set<Beacon> activeBeacons;
    private final Region ALL_BEACONS_REGION = new Region("allBeacons", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
    }

    @Override
    public void onBeaconServiceConnect() {

    }
}
