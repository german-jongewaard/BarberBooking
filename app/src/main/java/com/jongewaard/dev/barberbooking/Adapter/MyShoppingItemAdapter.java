package com.jongewaard.dev.barberbooking.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jongewaard.dev.barberbooking.Common.Common;
import com.jongewaard.dev.barberbooking.Database.CartDatabase;
import com.jongewaard.dev.barberbooking.Database.CartItem;
import com.jongewaard.dev.barberbooking.Database.DatabaseUtils;
import com.jongewaard.dev.barberbooking.Interface.IRecyclerItemSelectedListener;
import com.jongewaard.dev.barberbooking.Model.ShoppingItem;
import com.jongewaard.dev.barberbooking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {

    Context context;
    List<ShoppingItem> mShoppingItemList;
    CartDatabase cartDatabase;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList) {
        this.context = context;
        mShoppingItemList = shoppingItemList;
        cartDatabase = CartDatabase.getInstance(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_shopping_item_name, txt_shopping_item_price, txt_add_to_cart;
        ImageView img_shopping_item;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;


        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img_shopping_item = (ImageView)itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = (TextView)itemView.findViewById(R.id.txt_name_shopping_item);
            txt_shopping_item_price = (TextView)itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_cart = (TextView)itemView.findViewById(R.id.txt_add_to_cart);

            txt_add_to_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onitemSelectedListener(view, getAdapterPosition());

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_shopping_item, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        Picasso.get()
                .load(mShoppingItemList
                        .get(i)
                        .getImage())
                .into(myViewHolder
                        .img_shopping_item);

        myViewHolder.txt_shopping_item_name.setText(Common.formatShoppingItemName(mShoppingItemList.get(i).getName()));
        myViewHolder.txt_shopping_item_price.setText(new StringBuilder("$").append(mShoppingItemList.get(i).getPrice()));


        //Add to Cart
        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onitemSelectedListener(View view, int pos) {
                //Create cart Item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(mShoppingItemList.get(pos).getId());
                cartItem.setProductName(mShoppingItemList.get(pos).getName());
                cartItem.setProductImage(mShoppingItemList.get(pos).getImage());
                cartItem.setProductQuantity(1);
                cartItem.setProductPrice(mShoppingItemList.get(pos).getPrice());
                cartItem.setUserPhone(Common.currentUser.getPhoneNumber());

                //Insert to db
                DatabaseUtils.insertToCart(cartDatabase, cartItem);
                Toast.makeText(context, "Added to Cart !", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return mShoppingItemList.size();
    }
    



}
