package se.mogumogu.presencedetector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.text.DateFormat;
import java.util.Date;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.BeaconAliasNameDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class BeaconDetailsActivity extends ToolbarProvider {

    private String aliasName;
    private long inRangeTimeInMilliSeconds;
    private long outOfRangeTimeInMilliSeconds;
    private String proximityUuid;
    private String major;
    private String minor;
    private String rssi;
    private String inRangeDateTime;
    private String outOfRangeDateTime;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_details);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.beacon_details_toolbar);
        setToolbar(toolbar, false);

        getSubscribedBeacon();

        setBeaconDetailsInView();
    }

    private void setBeaconDetailsInView(){

        final TextView aliasNameTextView = (TextView) findViewById(R.id.beacon_details_alias_name);
        aliasNameTextView.setText(aliasName);

        final TextView lastTimeInRangeTextView = (TextView) findViewById(R.id.beacon_details_last_time_in_range);
        lastTimeInRangeTextView.setText(inRangeDateTime);

        final TextView lastTimeOutOfRangeTextView = (TextView) findViewById(R.id.beacon_details_last_time_out_of_range);
        lastTimeOutOfRangeTextView.setText(outOfRangeDateTime);

        final TextView proximityUuidTextView = (TextView) findViewById(R.id.beacon_details_uuid);
        proximityUuidTextView.setText(proximityUuid);

        final TextView majorTextView = (TextView) findViewById(R.id.beacon_details_major);
        majorTextView.setText(major);

        final TextView minorTextView = (TextView) findViewById(R.id.beacon_details_minor);
        minorTextView.setText(minor);

        final TextView rssiTextView = (TextView) findViewById(R.id.beacon_details_rssi);
        rssiTextView.setText(rssi);
    }

    private void getSubscribedBeacon(){

        final Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        final SubscribedBeacon subscribedBeacon = bundle.getParcelable(BeaconAliasNameDialogFragment.SUBSCRIBED_BEACON);

        if (subscribedBeacon != null) {

            aliasName = subscribedBeacon.getAliasName();
            inRangeTimeInMilliSeconds = subscribedBeacon.getInRangeTime();
            outOfRangeTimeInMilliSeconds = subscribedBeacon.getOutOfRangeTime();
            final Beacon beacon = subscribedBeacon.getBeacon();
            proximityUuid = beacon.getId1().toString();
            major = beacon.getId2().toString();
            minor = beacon.getId3().toString();
            rssi = String.valueOf(beacon.getRssi());
        }

        final DateFormat dateFormat = DateFormat.getDateTimeInstance();
        inRangeDateTime = dateFormat.format(new Date(inRangeTimeInMilliSeconds));

        if (outOfRangeTimeInMilliSeconds == 0) {

            outOfRangeDateTime = getResources().getString(R.string.no_data);

        } else {

            outOfRangeDateTime = dateFormat.format(new Date(outOfRangeTimeInMilliSeconds));
        }
    }
}
