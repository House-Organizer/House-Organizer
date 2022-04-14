package com.github.houseorganizer.houseorganizer.shop;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.task.TaskListAdapter;

public class ShopListView {

    public ShopListView(){}

    public static void setUpShopListView(AppCompatActivity parent, ShopListAdapter shopListAdapter) {
        RecyclerView shopListView = parent.findViewById(R.id.task_list);
        shopListView.setAdapter(shopListAdapter);
        shopListView.setLayoutManager(new LinearLayoutManager(parent));
    }

    public static void addItem(AppCompatActivity parent, FirestoreShopList shopList, ShopListAdapter adapter){
        LayoutInflater inflater = LayoutInflater.from(parent);
        final View dialogView = inflater.inflate(R.layout.shop_item_dialog, null);
        new AlertDialog.Builder(parent)
                .setTitle(R.string.add_item_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> retrieveItemFromDialog(shopList, dialogView, adapter))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private static void retrieveItemFromDialog(FirestoreShopList shopList, View dialogView, ShopListAdapter adapter){
        final String name = ((EditText) dialogView.findViewById(R.id.editTextName)).getText().toString();
        final String unit = ((EditText) dialogView.findViewById(R.id.editTextUnit)).getText().toString();
        int quantity = 0;
        try {
            quantity = Integer.parseInt(((EditText) dialogView.findViewById(R.id.editTextQuantity)).getText().toString());
        }catch (Exception e){
            // Only possible bad input is empty field
        }
        shopList.addItem(new ShopItem(name, quantity, unit));
        shopList.updateItems();
        adapter.notifyItemInserted(shopList.size()-1);
    }
}
