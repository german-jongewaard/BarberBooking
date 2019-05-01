package com.jongewaard.dev.barberbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jongewaard.dev.barberbooking.Common.Common;
import com.jongewaard.dev.barberbooking.Interface.IRecyclerItemSelectedListener;
import com.jongewaard.dev.barberbooking.Model.Salon;
import com.jongewaard.dev.barberbooking.R;

import java.util.ArrayList;
import java.util.List;

public class MySalonAdpter extends RecyclerView.Adapter<MySalonAdpter.MyViewHolder> {

    Context mContext;
    List<Salon> mSalonList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;


    public MySalonAdpter(Context context, List<Salon> salonList) {
        mContext = context;
        mSalonList = salonList;
        cardViewList = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_salon_name, txt_salon_address;
        CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_salon_address = (TextView)itemView.findViewById(R.id.txt_salon_address);
            txt_salon_name = (TextView)itemView.findViewById(R.id.txt_salon_name);
            card_salon = (CardView)itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onitemSelectedListener(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MySalonAdpter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_salon, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MySalonAdpter.MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(mSalonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(mSalonList.get(i).getAddress());

        if(!cardViewList.contains(myViewHolder.card_salon))
            cardViewList.add(myViewHolder.card_salon);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onitemSelectedListener(View view, int pos) {
                //Set with background for all card not be selected
                for (CardView cardView:cardViewList)
                    cardView.setCardBackgroundColor(mContext.getResources()
                            .getColor(android.R.color.white));

                //Set selected BG for only selected item
                myViewHolder.card_salon.setCardBackgroundColor(mContext.getResources()
                .getColor(android.R.color.holo_orange_dark));

                //Send Broadcast to tell Booking Activity enable Button next
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_SALOON_STORE, mSalonList.get(pos));
                intent.putExtra(Common.KEY_STEP, 1 );
                localBroadcastManager.sendBroadcast(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSalonList.size();
    }

}
