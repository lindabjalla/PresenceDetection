package se.mogumogu.presencedetector.adapter;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.List;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.fragment.BeaconAliasNameDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public final class SubscribedBeaconAdapter extends RecyclerView.Adapter<SubscribedBeaconAdapter.SubscribedBeaconViewHolder> {

    private Context context;
    private List<SubscribedBeacon> subscribedBeacons;
    private FragmentManager manager;

    public SubscribedBeaconAdapter(
            final Context context, final List<SubscribedBeacon> subscribedBeacons, final FragmentManager manager) {

        this.context = context;
        this.subscribedBeacons = subscribedBeacons;
        this.manager = manager;
    }

    @Override
    public SubscribedBeaconViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_subscribed_beacon, parent, false);

        return new SubscribedBeaconViewHolder(view, subscribedBeacons, manager);
    }

    @Override
    public void onBindViewHolder(final SubscribedBeaconViewHolder holder, final int position) {

        final SubscribedBeacon subscribedBeacon = subscribedBeacons.get(position);
        final Beacon beacon = subscribedBeacon.getBeacon();

        holder.aliasNameView.setText(subscribedBeacon.getAliasName());
        holder.dateOfSubscriptionView.setText(subscribedBeacon.getDateOfSubscription());
        holder.statusView.setText(getBeaconStatus(subscribedBeacon));
        holder.uuidView.setText(beacon.getId1().toString());
        holder.majorView.setText(beacon.getId2().toString());
        holder.minorView.setText(beacon.getId3().toString());

        if (position % 2 == 0) {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLilyWhite));

        } else {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVintageBlue));
        }
    }

    @Override
    public int getItemCount() {

        return subscribedBeacons.size();
    }

    private String getBeaconStatus(final SubscribedBeacon subscribedBeacon) {

        if (subscribedBeacon.isInRange()) {

            return "in range";

        } else {

            return "out of range";
        }
    }

    public static final class SubscribedBeaconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView aliasNameView;
        public final TextView dateOfSubscriptionView;
        public final TextView statusView;
        public final TextView uuidView;
        public final TextView majorView;
        public final TextView minorView;
        private List<SubscribedBeacon> subscribedBeacons;
        private SubscribedBeacon subscribedBeacon;
        private FragmentManager manager;

        public SubscribedBeaconViewHolder(
                final View view, final List<SubscribedBeacon> subscribedBeacons, final FragmentManager manager) {

            super(view);

            aliasNameView = (TextView) view.findViewById(R.id.subscribed_beacon_alias_name);
            dateOfSubscriptionView = (TextView) view.findViewById(R.id.date_of_subscription);
            statusView = (TextView) view.findViewById(R.id.subscribed_beacons_status);
            uuidView = (TextView) view.findViewById(R.id.subscribed_beacons_uuid);
            majorView = (TextView) view.findViewById(R.id.subscribed_beacons_major);
            minorView = (TextView) view.findViewById(R.id.subscribed_beacons_minor);

            view.setOnClickListener(this);
            this.subscribedBeacons = subscribedBeacons;
            this.manager = manager;
        }

        @Override
        public void onClick(final View view) {

            final int position = getAdapterPosition();
            subscribedBeacon = subscribedBeacons.get(position);

            showEditBeaconAliasNameDialog(subscribedBeacon);

        }

        private void showEditBeaconAliasNameDialog(final SubscribedBeacon subscribedBeacon) {

            final DialogFragment dialogFragment = BeaconAliasNameDialogFragment.newInstance(subscribedBeacon);
            dialogFragment.show(manager, "BeaconAliasNameDialogFragment");
        }
    }
}
