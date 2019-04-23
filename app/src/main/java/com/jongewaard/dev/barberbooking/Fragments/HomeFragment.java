package com.jongewaard.dev.barberbooking.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jongewaard.dev.barberbooking.Adapter.HomeSliderAdapter;
import com.jongewaard.dev.barberbooking.Adapter.LookbookAdapter;
import com.jongewaard.dev.barberbooking.BookingActivity;
import com.jongewaard.dev.barberbooking.Common.Common;
import com.jongewaard.dev.barberbooking.Database.CartDatabase;
import com.jongewaard.dev.barberbooking.Database.DatabaseUtils;
import com.jongewaard.dev.barberbooking.Interface.IBannerLoadListener;
import com.jongewaard.dev.barberbooking.Interface.IBookingInfoLoadListener;
import com.jongewaard.dev.barberbooking.Interface.IBookingInformationChangeListener;
import com.jongewaard.dev.barberbooking.Interface.ICountItemInCartListener;
import com.jongewaard.dev.barberbooking.Interface.ILookbookLoadListener;
import com.jongewaard.dev.barberbooking.Model.Banner;
import com.jongewaard.dev.barberbooking.Model.BookingInformation;
import com.jongewaard.dev.barberbooking.R;
import com.jongewaard.dev.barberbooking.Service.PicassoImageLoadingService;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements ILookbookLoadListener, IBannerLoadListener, IBookingInfoLoadListener, IBookingInformationChangeListener, ICountItemInCartListener {

    private Unbinder mUnbinder;

    CartDatabase cartDatabase;

    AlertDialog dialog;

    @BindView(R.id.notification_badge)
    NotificationBadge notificationBadge;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    //Interface
    IBannerLoadListener mIBannerLoadListener;
    ILookbookLoadListener mILookbookLoadListener;
    IBookingInfoLoadListener mIBookingInfoLoadListener;
    IBookingInformationChangeListener mIBookingInformationChangeListener;

    //FireStore
    CollectionReference bannerRef, lookbookRef;

    //Datos del CardView(gone) en el fragment_home
    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        //Database
        cartDatabase = CartDatabase.getInstance(getContext());

        //Init
        Slider.init(new PicassoImageLoadingService());
        mIBannerLoadListener = this;
        mILookbookLoadListener = this;
        mIBookingInfoLoadListener = this;
        mIBookingInformationChangeListener = this;

        //Check if it's logged?
        //Si esta logueado con la cuenta de facebook
        if(AccountKit.getCurrentAccessToken() != null){
            setUserInformation();
            loadBanner();
            loadLookbook();
            loadUserBooking();
            countCartItem();
        }
        return view;
    }

    private void countCartItem() {
        DatabaseUtils.countItemCart(cartDatabase, this);


    }

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking()
    {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking()
    {
        changueBookingFromUser();
    }

    private void changueBookingFromUser() {
        //Show dialog confirm
        android.support.v7.app.AlertDialog.Builder confirmDialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Hey!")
                .setMessage("Do you really want to change booking information?\nBecause we will delete your old booking information\nJust confirm")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteBookingFromBarber(true); //True because we call from button change
                    }
                });
        confirmDialog.show();


    }

    private void deleteBookingFromBarber(final boolean isChange) {
        /*To delete booking, first we need delete from Barber collections
         * After that, we will delete from User booking collections
         * And final, delete event
         * */

        //We need Load common.currentBooking because we need some data from BookingInformation
        if(Common.currentBooking != null)
        {
            dialog.show();

            //Get booking information in barber object
            DocumentReference barberBookinginfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCityBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barber")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());

            //when we hve document, just delete it
            barberBookinginfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //After delete on Barber done
                    //We will start delete from User
                    deleteBookingFromUser(isChange);
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Current Booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(final boolean isChange) {

        //First, we need get information from user object
        if(!TextUtils.isEmpty(Common.currentBookingId))
        {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);

            //delete
            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //After delete from "User", just delete from Calendar
                    //first, we need get save Uri of event we just add
                    Paper.init(getActivity());
                    Uri eventUri = Uri.parse(Paper.book().read(Common.EVENT_URI_CACHE).toString());
                    getActivity().getContentResolver().delete(eventUri, null, null);

                    Toast.makeText(getActivity(), "Success delete booking !", Toast.LENGTH_SHORT).show();

                    //Refresh
                    loadUserBooking();

                    //Check if isChange -> call from change button, we will fired interface
                    if(isChange)
                        mIBookingInformationChangeListener.onBookingInformationChange();

                    dialog.dismiss();
                }
            });
        }
        else
        {
            dialog.dismiss();

            Toast.makeText(getContext(), "Booking information ID must not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.card_view_booking)
    void booking(){
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //Get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        //Select booking information from firebase with done=false and timestamp greater today
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1) //Only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful())
                        {
                            if(!task.getResult().isEmpty())
                            {
                                for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult())
                                {
                                    BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                    mIBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                                    break; //Exit loop as soon as
                                }
                            }
                            else
                                mIBookingInfoLoadListener.onBookingInfoLoadEmpty();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mIBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());
            }
        });
    }

    private void loadLookbook() {
        lookbookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> lookbook = new ArrayList<>();

                        if(task.isSuccessful()){

                            for(QueryDocumentSnapshot bannerSnapShot: task.getResult()){

                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                lookbook.add(banner);
                            }
                            mILookbookLoadListener.onLookbookLoadSuccess(lookbook);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mILookbookLoadListener.onLookbookLoadFailed(e.getMessage());
            }
        });
    }

    private void loadBanner() {
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();

                        if(task.isSuccessful()){

                            for(QueryDocumentSnapshot bannerSnapShot: task.getResult()){

                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            mIBannerLoadListener.onBannerLoadSuccess(banners);
                         }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mIBannerLoadListener.onBannerLoadFailed(e.getMessage());
            }
        });
    }

    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(), banners));
    }

    @Override
    public void onLookbookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();

        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBookingInfoLoadFailed(String messagge) {
        Toast.makeText(getContext(), messagge, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBookingInformationChange() {
        //here we will just start activity Booking
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    @Override
    public void onCartItemCountSuccess(int count) {
        notificationBadge.setText(String.valueOf(count));

    }
}
