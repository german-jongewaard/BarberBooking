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
import com.jongewaard.dev.barberbooking.Model.TimeSlot;
import com.jongewaard.dev.barberbooking.R;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;

    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {

        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());
        if(timeSlotList.size() == 0) //If ll position is availble, just show list
        {
            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            myViewHolder.txt_time_slot_description.setText("Available");
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
            .getColor(android.R.color.black));
            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        else //If have position is full (booked)
        {
            for(TimeSlot slotValue:timeSlotList)
            {
                //Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if(slot == i) //If slot == position
                {
                    //We will set tag for alltime slot is full
                    //So base on tag, we can set all remain card background without change full time slot
                    myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    myViewHolder.txt_time_slot_description.setText("Full");
                    myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                            .getColor(android.R.color.white));
                    myViewHolder.txt_time_slot.setCompoundDrawablePadding(context.getResources().getColor(android.R.color.white));
                }
            }
        }

        //Add only card to list  (20 crd because we have 20 time slot)
        //No dd card already in cardViewlist
        if(!cardViewList.contains(myViewHolder.card_time_slot))
            cardViewList.add(myViewHolder.card_time_slot);

        //Check if card time slot is available
      if(!timeSlotList.contains(i)){
          myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
              @Override
              public void onitemSelectedListener(View view, int pos) {
                  //Loop all card in card List
                  for(CardView cardView:cardViewList)
                  {
                      if(cardView.getTag() == null) //Only available card time slot be change
                          cardView.setCardBackgroundColor(context.getResources()
                                  .getColor(android.R.color.white));
                  }
                  //Our selected card will be change color
                  myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                          .getColor(android.R.color.holo_orange_dark));

                  //After that, send broadcast to enable NEXT
                  Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                  intent.putExtra(Common.KEY_TIME_SLOT, i); //Put index of time slot we have selected)
                  intent.putExtra(Common.KEY_STEP, 3); //Go to step 3
                  localBroadcastManager.sendBroadcast(intent);
              }
          });
      }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;


        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView)itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onitemSelectedListener(view, getAdapterPosition());

        }
    }
}
