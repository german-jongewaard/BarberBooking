package com.jongewaard.dev.barberbooking.Interface;

import com.jongewaard.dev.barberbooking.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadfailed(String message);
}
