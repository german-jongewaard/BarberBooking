package com.jongewaard.dev.barberbooking.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jongewaard.dev.barberbooking.Model.Banner;
import com.jongewaard.dev.barberbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LookbookAdapter extends RecyclerView.Adapter<LookbookAdapter.MyViewHolder> {

    Context context;
    List<Banner> lookbook;

    public LookbookAdapter(Context context, List<Banner> lookbook) {
        this.context = context;
        this.lookbook = lookbook;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (ImageView)itemView.findViewById(R.id.image_look_book);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemview = LayoutInflater.from(context)
                .inflate(R.layout.layout_look_book, viewGroup, false);

        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Picasso.get().load(lookbook.get(i).getImage()).into(myViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return lookbook.size();
    }


}
