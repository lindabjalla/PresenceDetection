package se.mogumogu.presencedetector.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;

import java.util.List;
import java.util.Set;

import se.mogumogu.presencedetector.PresenceDetectorApplication;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.BasicDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.DeviceViewHolder> {

    private Context context;
    private List<Beacon> beacons;
    private FragmentManager manager;

    public BeaconAdapter(final Context context, final List<Beacon> beacons, final FragmentManager manager) {

        this.context = context;
        this.beacons = beacons;
        this.manager = manager;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_active_beacon, parent, false);

        return new DeviceViewHolder(view, context, beacons, manager);
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {

        final Beacon beacon = beacons.get(position);

        holder.proximityUuidView.setText(beacon.getId1().toString());
        holder.majorView.setText(beacon.getId2().toString());
        holder.minorView.setText(beacon.getId3().toString());
        holder.rssiView.setText(String.valueOf(beacon.getRssi()));

        if (position % 2 == 0) {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVintageBlue));

        } else {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLilyWhite));
        }
    }

    @Override
    public int getItemCount() {

        return beacons.size();
    }

    public static final class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView proximityUuidView;
        public final TextView majorView;
        public final TextView minorView;
        public final TextView rssiView;

        private Context context;
        private List<Beacon> beacons;
        private FragmentManager manager;
        private Beacon beacon;
        private Gson gson;
        private Set<SubscribedBeacon> subscribedBeacons;

        public DeviceViewHolder(final View view, final Context context, final List<Beacon> beacons, final FragmentManager manager) {

            super(view);
            this.proximityUuidView = (TextView) view.findViewById(R.id.proximity_uuid);
            this.majorView = (TextView) view.findViewById(R.id.text_major);
            this.minorView = (TextView) view.findViewById(R.id.text_minor);
            this.rssiView = (TextView) view.findViewById(R.id.active_beacon_rssi);

            view.setOnClickListener(this);
            this.context = context;
            this.beacons = beacons;
            this.manager = manager;
            gson = new Gson();
        }

        @Override
        public void onClick(final View view) {

            final int position = getAdapterPosition();
            beacon = beacons.get(position);

            final SharedPreferences preferences =
                    context.getSharedPreferences(PresenceDetectorApplication.PRESENCE_DETECTOR_PREFERENCES, Context.MODE_PRIVATE);

            final String subscribedBeaconSetJson = preferences.getString(PresenceDetectorApplication.SUBSCRIBED_BEACONS, null);

            subscribedBeacons = PresenceDetectorApplication.initializeSubscribedBeacons(subscribedBeaconSetJson);

            if (isSubscribed(beacon, subscribedBeacons)) {

                Toast.makeText(context, "This Beacon is previously subscribed", Toast.LENGTH_LONG).show();

            } else {

                final String beaconJson = gson.toJson(beacon);
                preferences.edit().putString(PresenceDetectorApplication.BEACON_KEY, beaconJson).apply();

                showSubscriptionDialog();
            }
        }

        private void showSubscriptionDialog() {

            final DialogFragment dialogFragment = BasicDialogFragment.newInstance(
                    R.layout.dialog_fragment_subscription, R.string.dialog_fragment_subscription_title, R.string.subscribe);

            dialogFragment.show(manager, "SubscriptionDialogFragment");
        }

        private boolean isSubscribed(final Beacon beacon, final Set<SubscribedBeacon> subscribedBeacons) {

            for (final SubscribedBeacon subscribedBeacon : subscribedBeacons) {

                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                    return true;
                }
            }

            return false;
        }
    }
}
