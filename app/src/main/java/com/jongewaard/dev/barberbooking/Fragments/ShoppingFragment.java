package com.jongewaard.dev.barberbooking.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jongewaard.dev.barberbooking.Adapter.MyShoppingItemAdapter;
import com.jongewaard.dev.barberbooking.Common.SpacesItemDecoration;
import com.jongewaard.dev.barberbooking.Interface.IShoppingDataLoadListener;
import com.jongewaard.dev.barberbooking.Model.ShoppingItem;
import com.jongewaard.dev.barberbooking.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 *
 * A simple {@link Fragment} subclass.
 */
public class ShoppingFragment extends Fragment implements IShoppingDataLoadListener {

    CollectionReference shoppingItemRef;

    IShoppingDataLoadListener mIShoppingDataLoadListener;

    Unbinder mUnbinder;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;
    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @OnClick(R.id.chip_wax)
    void wasChipClick(){
        setSelecteedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @OnClick(R.id.chip_spray)
    void sprayChipClick(){
        setSelecteedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_body_care)
    Chip chip_body_care;
    @OnClick(R.id.chip_body_care)
    void bodyCareChipClick(){
        setSelecteedChip(chip_body_care);
        loadShoppingItem("BodyCare");
    }

    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @OnClick(R.id.chip_hair_care)
    void hairCareChipClick(){
        setSelecteedChip(chip_hair_care);
        loadShoppingItem("HairCare");
    }

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("Items");

        //Get Data
        shoppingItemRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIShoppingDataLoadListener.onShoppingDataLoadfailed(e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<ShoppingItem> shoppingItems = new ArrayList<>();
                    for(DocumentSnapshot itemSnapShot:task.getResult())
                    {
                        ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                        shoppingItem.setId(itemSnapShot.getId()); //Remember add it if you don't want to get null reference
                        shoppingItems.add(shoppingItem);
                    }
                    mIShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                }
            }
        });
    }

    private void setSelecteedChip(Chip chip) {
        //Set Color
        for(int i = 0; i < chipGroup.getChildCount(); i++ )
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);
            if(chipItem.getId() != chip.getId()) //If not selected
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else //If selected
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }


    public ShoppingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);

        mUnbinder = ButterKnife.bind(this, itemView);
        mIShoppingDataLoadListener = this;

        //Default load
        loadShoppingItem("Wax");

        initView();

        return itemView;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList);
        recycler_items.setAdapter(adapter);

    }

    @Override
    public void onShoppingDataLoadfailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();

    }
}
