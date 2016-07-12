package se.mogumogu.presencedetector.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import se.mogumogu.presencedetector.BeaconDetailsActivity;
import se.mogumogu.presencedetector.R;
import se.mogumogu.presencedetector.model.SubscribedBeacon;

public class SubscribedBeaconAdapter extends RecyclerView.Adapter<SubscribedBeaconAdapter.SubscribedBeaconViewHolder>{

    private Context context;
    private List<SubscribedBeacon> subscribedBeacons;

    public SubscribedBeaconAdapter(Context context, Set<SubscribedBeacon> subscribedBeaconsSet){

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
    }

    @Override
    public SubscribedBeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_subscribed_beacon, parent, false);
        return new SubscribedBeaconViewHolder(view, context, subscribedBeacons);
    }

    @Override
    public void onBindViewHolder(SubscribedBeaconViewHolder holder, int position) {

        holder.aliasNameView.setText(subscribedBeacons.get(position).getAliasName());
        holder.statusView.setText(getBeaconStatus(position));

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

    private String getBeaconStatus(int position){

        SubscribedBeacon subscribedBeacon = subscribedBeacons.get(position);

        if(subscribedBeacon.isInRange()){

            return "in range";

        }else{

            return "out of range";
        }
    }

    public static final class SubscribedBeaconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public static final String SUBSCRIBED_BEACON = "se.mogumogu.presencedetector.SUBSCRIBED_BEACON";

        public final TextView aliasNameView;
        public final TextView statusView;
        private Context context;
        private List<SubscribedBeacon> subscribedBeacons;
        private SubscribedBeacon subscribedBeacon;

        public SubscribedBeaconViewHolder(View view, Context context, List<SubscribedBeacon> subscribedBeacons) {

            super(view);
            aliasNameView = (TextView) view.findViewById(R.id.my_beacons_alias_name);
            statusView = (TextView) view.findViewById(R.id.my_beacons_status);
            view.setOnClickListener(this);
            this.context = context;
            this.subscribedBeacons = subscribedBeacons;
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            subscribedBeacon = subscribedBeacons.get(position);

            Intent intent = new Intent(context, BeaconDetailsActivity.class);
            intent.putExtra(SUBSCRIBED_BEACON, subscribedBeacon);
            context.startActivity(intent);
        }
    }
}
