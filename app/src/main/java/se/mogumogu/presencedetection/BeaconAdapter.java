package se.mogumogu.presencedetection;

import android.content.Context;
import android.content.Intent;
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

import se.mogumogu.presencedetection.Activity.SubscriptionActivity;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.DeviceViewHolder> {

    public static final String BEACON_KEY = "se.mogumogu.presencedetection.BEACON_KEY";
    private Context context;
    private List<Beacon> beaconList;

    public BeaconAdapter(final Context context, final Set<Beacon> beaconSet) {

        this.context = context;
        beaconList = new ArrayList<>();
        beaconList.addAll(beaconSet);
        Collections.sort(beaconList, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon beacon1, Beacon beacon2) {
                return Integer.compare(beacon1.getRssi(), beacon2.getRssi());
            }
        });
        Collections.reverse(beaconList);

        for (Beacon b : beaconList) {
            Log.d("rssi", "---------" + b.getId1().toString() + ": " + String.valueOf(b.getRssi()));
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_active_beacons, parent, false);
        return new DeviceViewHolder(view, context, beaconList);
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {

        holder.proximityUuidView.setText(beaconList.get(position).getId1().toString());
        holder.majorView.setText(beaconList.get(position).getId2().toString());
        holder.minorView.setText(beaconList.get(position).getId3().toString());

        if (position % 2 == 0) {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEggshell));

        } else {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVintageBlue));
        }
    }

    @Override
    public int getItemCount() {

        return beaconList.size();
    }

    public static final class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final TextView proximityUuidView;
        public final TextView majorView;
        public final TextView minorView;
        private Context context;
        private List<Beacon> beacons;

        public DeviceViewHolder(View view, Context context, List<Beacon> beacons) {

            super(view);
            this.proximityUuidView = (TextView) view.findViewById(R.id.proximity_uuid);
            this.majorView = (TextView) view.findViewById(R.id.text_major);
            this.minorView = (TextView) view.findViewById(R.id.text_minor);

            view.setOnClickListener(this);
            this.context = context;
            this.beacons = beacons;
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            Beacon beacon = beacons.get(position);

            Intent intent = new Intent(context, SubscriptionActivity.class);
            intent.putExtra(BEACON_KEY, beacon);
            context.startActivity(intent);
        }
    }
}
