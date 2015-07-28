package com.insiteo.sampleapp.beacon;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.insiteo.sampleapp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by MMO on 25/02/2015.
 */
public class BeaconRegionAdapter extends RecyclerView.Adapter<BeaconRegionAdapter.ViewHolder> {

    private static final String TAG = "RegionAdapter";

    private List<StateBeaconRegion> mData;

    private int enteredColor, exitColor;
    private Drawable drawable;


    public BeaconRegionAdapter(Context context) {
        mData = new ArrayList<StateBeaconRegion>();
        enteredColor = context.getResources().getColor(R.color.insiteo_beacon_notification_entered);
        exitColor = context.getResources().getColor(R.color.insiteo_beacon_notification_exit);

        drawable = context.getResources().getDrawable(R.drawable.ic_insiteo_beacon);
    }

    public void setDataCollection(Collection<StateBeaconRegion> regions) {
        mData = new ArrayList<>(regions.size());
        mData.addAll(regions);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.beacon_region_item, viewGroup, false);
        return new ViewHolder(v);
    }



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        StateBeaconRegion region = mData.get(position);

        viewHolder.getLabelTextView().setText(region.getLabel());
        viewHolder.getMessageTextView().setText(region.getMessage() != null ? region.getMessage() : "");

        StringBuilder identifier = new StringBuilder();
        identifier.append(" uuid: ").append(region.getUuid());
        if(region.getMajor() != null) identifier.append("\n major: ").append(region.getMajor());
        if(region.getMinor() != null) identifier.append("\n minor: ").append(region.getMinor());

        viewHolder.getIdentifierTextView().setText(identifier.toString());

        viewHolder.getForceNotification().setChecked(region.shouldForceNotification());

        viewHolder.getIcon().setColorFilter(region.getState() == StateBeaconRegion.State.IN ? enteredColor : exitColor, PorterDuff.Mode.SRC_ATOP);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getExternalId();
    }

    public StateBeaconRegion getItem(int position) {
        return mData.get(position);
    }

    public int add(StateBeaconRegion region) {
        int position = mData.indexOf(region);

        if(position != -1) {
            mData.remove(position);
            mData.add(position, region);
        } else {
            mData.add(position = mData.size(), region);
        }
        notifyDataSetChanged();
        return position;
    }

    //**********************************************************************************************
    //
    // *********************************************************************************************

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView labelTextView;
        private final TextView messageTextView;
        private final TextView identifierTextView;
        private final ImageView icon;
        private final CheckBox forceNotification;

        private final CardView view;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            view = (CardView) v;
            icon = (ImageView) v.findViewById(R.id.region_icon);
            labelTextView = (TextView) v.findViewById(R.id.region_label);
            messageTextView = (TextView) v.findViewById(R.id.region_message);
            identifierTextView = (TextView) v.findViewById(R.id.region_identifier);
            forceNotification = (CheckBox) v.findViewById(R.id.region_force_notification);
        }

        public CardView getView() {
            return view;
        }

        public ImageView getIcon(){
            return icon;
        }

        public TextView getLabelTextView() {
            return labelTextView;
        }
        public TextView getMessageTextView() {
            return messageTextView;
        }
        public TextView getIdentifierTextView() {
            return identifierTextView;
        }
        public CheckBox getForceNotification() { return forceNotification; }
    }





}
