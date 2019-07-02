package com.jongewaard.dev.barberbooking.Adapter;

import com.jongewaard.dev.barberbooking.Model.Banner;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class HomeSliderAdapter extends SliderAdapter {

    List<Banner> mBannerList;

    public HomeSliderAdapter(List<Banner> bannerList) {
        mBannerList = bannerList;
    }

    @Override
    public int getItemCount() {
        return mBannerList.size();
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {

        imageSlideViewHolder.bindImageSlide(mBannerList.get(position).getImage());

    }
}
