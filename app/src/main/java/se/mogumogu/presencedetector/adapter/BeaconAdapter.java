package se.mogumogu.presencedetector.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.activity.RegistrationActivity;
import se.mogumogu.presencedetector.activity.ScanActivity;
import se.mogumogu.presencedetector.fragment.BasicDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.DeviceViewHolder> {

    public static final String BEACON_KEY = "se.mogumogu.presencedetection.BEACON_KEY";

    private Context context;
    private List<Beacon> beacons;
    private FragmentManager manager;

    public BeaconAdapter(final Context context, final List<Beacon> beacons, final FragmentManager manager) {

        this.context = context;
        this.beacons = beacons;

        Collections.sort(beacons, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon beacon1, Beacon beacon2) {

                return Integer.compare(beacon1.getRssi(), beacon2.getRssi());
            }
        });
        Collections.reverse(beacons);

        this.manager = manager;
        for (Beacon b : beacons) {
            Log.d("rssi", "---------" + b.getId1().toString() + ": " + String.valueOf(b.getRssi()));
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_active_beacon, parent, false);
        return new DeviceViewHolder(view, context, beacons, manager);
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {

        holder.beaconNumberView.setText(context.getString(R.string.beacon, (position + 1)));
        holder.proximityUuidView.setText(beacons.get(position).getId1().toString());
        holder.majorView.setText(beacons.get(position).getId2().toString());
        holder.minorView.setText(beacons.get(position).getId3().toString());

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

        public final TextView beaconNumberView;
        public final TextView proximityUuidView;
        public final TextView majorView;
        public final TextView minorView;
        private Context context;
        private List<Beacon> beacons;
        private FragmentManager manager;
        private Beacon beacon;
        private Gson gson;
        private Set<SubscribedBeacon> subscribedBeacons;


        public DeviceViewHolder(final View view, final Context context, final List<Beacon> beacons, final FragmentManager manager) {

            super(view);
            this.beaconNumberView = (TextView) view.findViewById(R.id.beacon_number);
            this.proximityUuidView = (TextView) view.findViewById(R.id.proximity_uuid);
            this.majorView = (TextView) view.findViewById(R.id.text_major);
            this.minorView = (TextView) view.findViewById(R.id.text_minor);

            view.setOnClickListener(this);
            this.context = context;
            this.beacons = beacons;
            this.manager = manager;
            gson = new Gson();
        }

        @Override
        public void onClick(final View view) {

            int position = getAdapterPosition();
            beacon = beacons.get(position);

            final SharedPreferences preferences =
                    context.getSharedPreferences(RegistrationActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);

            final String subscribedBeaconSetJson = preferences.getString(ScanActivity.SUBSCRIBED_BEACONS, null);

            if (subscribedBeaconSetJson == null) {

                subscribedBeacons = new HashSet<>();

            } else {

                final Type type = new TypeToken<Set<SubscribedBeacon>>() {}.getType();
                subscribedBeacons = gson.fromJson(subscribedBeaconSetJson, type);
            }

            if (beaconIsSubscribed(beacon, subscribedBeacons)) {

                Toast.makeText(context, "This Beacon is previously subscribed", Toast.LENGTH_LONG).show();

            } else {

                final String beaconJson = gson.toJson(beacon);
                preferences.edit().putString(BEACON_KEY, beaconJson).apply();

                showSubscriptionDialog();
            }
        }

        private void showSubscriptionDialog() {

            DialogFragment dialogFragment = BasicDialogFragment.newInstance(
                    R.layout.dialog_fragment_subscription, R.string.dialog_fragment_subscription_title, R.string.subscribe);
            dialogFragment.show(manager, "SubscriptionDialogFragment");
        }

        private boolean beaconIsSubscribed(final Beacon beacon, final Set<SubscribedBeacon> subscribedBeacons) {

            for (SubscribedBeacon subscribedBeacon : subscribedBeacons) {

                if (subscribedBeacon.getBeacon().getIdentifiers().containsAll(beacon.getIdentifiers())) {

                    return true;
                }
            }
            return false;
        }
    }
}
