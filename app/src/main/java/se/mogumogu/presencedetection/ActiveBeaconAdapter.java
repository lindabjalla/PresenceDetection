package se.mogumogu.presencedetection;

import android.content.Context;
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

public class ActiveBeaconAdapter extends RecyclerView.Adapter<ActiveBeaconAdapter.DeviceViewHolder> {

    private Context context;
    private List<Beacon> beacons;

    public ActiveBeaconAdapter(final Context context, final Set<Beacon> beacons) {

        this.context = context;
        this.beacons = new ArrayList<>();
        this.beacons.addAll(beacons);
        Collections.sort(this.beacons, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon beacon1, Beacon beacon2) {
                return Integer.compare(beacon1.getRssi(), beacon2.getRssi());
            }
        });
        Collections.reverse(this.beacons);

        for (Beacon b : this.beacons) {
            Log.d("rssi", "---------" + b.getId1().toString() + ": " + String.valueOf(b.getRssi()));
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_active_beacons, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {

        holder.proximityUuidView.setText(beacons.get(position).getId1().toString());
        holder.majorView.setText(beacons.get(position).getId2().toString());
        holder.minorView.setText(beacons.get(position).getId3().toString());

        if (position % 2 == 0) {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEggshell));

        } else {

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVintageBlue));
        }
    }

    @Override
    public int getItemCount() {

        return beacons.size();
    }

    public static final class DeviceViewHolder extends RecyclerView.ViewHolder {

        public final TextView proximityUuidView;
        public final TextView majorView;
        public final TextView minorView;

        public DeviceViewHolder(View view) {

            super(view);
            this.proximityUuidView = (TextView) view.findViewById(R.id.proximity_uuid);
            this.majorView = (TextView) view.findViewById(R.id.text_major);
            this.minorView = (TextView) view.findViewById(R.id.text_minor);
        }
    }
}
