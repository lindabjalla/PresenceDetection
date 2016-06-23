package se.mogumogu.presencedetection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import se.mogumogu.presencedetection.Activity.MainActivity;
import se.mogumogu.presencedetection.Activity.SubscriptionActivity;
import se.mogumogu.presencedetection.DialogFragment.RegistrationDialogFragment;
import se.mogumogu.presencedetection.DialogFragment.SubscriptionDialogFragment;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.DeviceViewHolder> {

    public static final String BEACON_KEY = "se.mogumogu.presencedetection.BEACON_KEY";
    private Context context;
    private List<Beacon> beaconList;
    private FragmentManager manager;

    public BeaconAdapter(final Context context, final Set<Beacon> beaconSet, FragmentManager manager) {

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
        this.manager = manager;
        for (Beacon b : beaconList) {
            Log.d("rssi", "---------" + b.getId1().toString() + ": " + String.valueOf(b.getRssi()));
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_active_beacons, parent, false);
        return new DeviceViewHolder(view, context, beaconList, manager);
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
        private DialogFragment dialogFragment;
        private FragmentManager manager;
        private Beacon beacon;
        private Gson gson;

        public DeviceViewHolder(View view, Context context, List<Beacon> beacons, FragmentManager manager) {

            super(view);
            this.proximityUuidView = (TextView) view.findViewById(R.id.proximity_uuid);
            this.majorView = (TextView) view.findViewById(R.id.text_major);
            this.minorView = (TextView) view.findViewById(R.id.text_minor);

            view.setOnClickListener(this);
            this.context = context;
            this.beacons = beacons;
            this.manager = manager;

            dialogFragment = new SubscriptionDialogFragment();
            gson = new Gson();
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            beacon = beacons.get(position);
            String beaconJson = gson.toJson(beacon);
            SharedPreferences preferences =
                    context.getSharedPreferences(MainActivity.PRESENCE_DETECTION_PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().putString(BEACON_KEY, beaconJson).apply();

            showSubscriptionDialog();
        }

        public void showSubscriptionDialog() {

            dialogFragment = new SubscriptionDialogFragment();
            dialogFragment.show(manager, "SubscriptionDialogFragment");
        }
    }
}
