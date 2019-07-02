package com.jongewaard.dev.barberbooking.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jongewaard.dev.barberbooking.Fragments.BookingStep1Fragment;
import com.jongewaard.dev.barberbooking.Fragments.BookingStep2Fragment;
import com.jongewaard.dev.barberbooking.Fragments.BookingStep3Fragment;
import com.jongewaard.dev.barberbooking.Fragments.BookingStep4Fragment;

public class MyViewPagerAdapter extends FragmentPagerAdapter {

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return BookingStep1Fragment.getInstance();
            case 1:
                return BookingStep2Fragment.getInstance();
            case 2:
                return BookingStep3Fragment.getInstance();
            case 3:
                return BookingStep4Fragment.getInstance();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
