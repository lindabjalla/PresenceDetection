package se.mogumogu.presencedetector.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.RangeHandler;

public class SubscribedBeaconsActivity extends AppCompatActivity implements BeaconConsumer {

    public static boolean isActive;

    private Context context;
    private BeaconManager beaconManager;
    private Region allBeaconsRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_beacons);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_beacons_toolbar);
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        myToolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.colorDimGray));
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        context = this;
        allBeaconsRegion = new Region("allBeacons", null, null, null);
        SharedPreferences preferences = getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
        RangeHandler rangeHandler = new RangeHandler(preferences, this, this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setRangeNotifier(rangeHandler);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.menu_item_my_beacons:
                intent = new Intent(this, SubscribedBeaconsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_settings:
                intent = new Intent(this, SettingsActivity.class);
                context.startActivity(intent);
                return true;

            case R.id.menu_item_help:
                intent = new Intent(this, HelpActivity.class);
                context.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onStart() {

        super.onStart();
        isActive = true;
    }

    @Override
    protected void onPause() {

        Log.d("onPause", "onPause");
        super.onPause();
        beaconManager.unbind(this);
        Log.d("beaconManager", beaconManager.getRangingNotifier().toString());
    }

    @Override
    protected void onStop() {

        super.onStop();
        isActive = false;
    }
}
