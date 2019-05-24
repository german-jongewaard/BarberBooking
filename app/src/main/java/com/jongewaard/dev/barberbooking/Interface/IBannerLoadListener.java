package com.jongewaard.dev.barberbooking.Interface;

import com.jongewaard.dev.barberbookingshop.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {

    void onBannerLoadSuccess(List<Banner> banners);

    void onBannerLoadFailed(String message);
}
