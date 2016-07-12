package se.mogumogu.presencedetector.adapter;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.dialogfragment.EditBeaconAliasNameDialogFragment;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public class SubscribedBeaconAdapter extends RecyclerView.Adapter<SubscribedBeaconAdapter.SubscribedBeaconViewHolder> {

    private Context context;
    private List<SubscribedBeacon> subscribedBeacons;
    private FragmentManager manager;

    public SubscribedBeaconAdapter(Context context, Set<SubscribedBeacon> subscribedBeaconsSet, FragmentManager manager) {

        this.context = context;
        subscribedBeacons = new ArrayList<>();
        subscribedBeacons.addAll(subscribedBeaconsSet);

        Log.d("unsorted", subscribedBeacons.toString());

        Collections.sort(subscribedBeacons, new Comparator<SubscribedBeacon>() {
            @Override
            public int compare(SubscribedBeacon beacon1, SubscribedBeacon beacon2) {

                return Boolean.compare(beacon1.isInRange(), beacon2.isInRange());
            }
        });
        Collections.reverse(subscribedBeacons);

        Log.d("sorted by boolean", subscribedBeacons.toString());

        this.manager = manager;
    }

    @Override
    public SubscribedBeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_subscribed_beacon, parent, false);
        return new SubscribedBeaconViewHolder(view, subscribedBeacons, manager);
    }

    @Override
    public void onBindViewHolder(SubscribedBeaconViewHolder holder, int position) {

        SubscribedBeacon subscribedBeacon = subscribedBeacons.get(position);
        Beacon beacon = subscribedBeacon.getBeacon();

        holder.aliasNameView.setText(subscribedBeacon.getAliasName());
        holder.statusView.setText(getBeaconStatus(subscribedBeacon));
        holder.uuidView.setText(beacon.getId1().toString());
        holder.majorView.setText(beacon.getId2().toString());
        holder.minorView.setText(beacon.getId3().toString());

        if (position % 2 == 0) {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEggshell));

        } else {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVintageBlue));
        }
    }

    @Override
    public int getItemCount() {

        return subscribedBeacons.size();
    }

    private String getBeaconStatus(SubscribedBeacon subscribedBeacon) {

        if (subscribedBeacon.isInRange()) {

            return "in range";

        } else {

            return "out of range";
        }
    }

    public static final class SubscribedBeaconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView aliasNameView;
        public final TextView statusView;
        public final TextView uuidView;
        public final TextView majorView;
        public final TextView minorView;
        private List<SubscribedBeacon> subscribedBeacons;
        private SubscribedBeacon subscribedBeacon;
        private FragmentManager manager;

        public SubscribedBeaconViewHolder(View view, List<SubscribedBeacon> subscribedBeacons, FragmentManager manager) {

            super(view);

            aliasNameView = (TextView) view.findViewById(R.id.subscribed_beacons_alias_name);
            statusView = (TextView) view.findViewById(R.id.subscribed_beacons_status);
            uuidView = (TextView) view.findViewById(R.id.subscribed_beacons_uuid);
            majorView = (TextView) view.findViewById(R.id.subscribed_beacons_major);
            minorView = (TextView) view.findViewById(R.id.subscribed_beacons_minor);

            view.setOnClickListener(this);
            this.subscribedBeacons = subscribedBeacons;
            this.manager = manager;
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            subscribedBeacon = subscribedBeacons.get(position);

            showEditBeaconAliasNameDialog(subscribedBeacon);

        }

        private void showEditBeaconAliasNameDialog(SubscribedBeacon subscribedBeacon) {

            DialogFragment dialogFragment = EditBeaconAliasNameDialogFragment.newInstance(subscribedBeacon);
            dialogFragment.show(manager, "EditBeaconAliasNameDialogFragment");
        }
    }
}
