package com.jongewaard.dev.barberbooking.Interface;


import com.jongewaard.dev.barberbooking.Model.Banner;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);

    void onLookbookLoadFailed(String message);
}
