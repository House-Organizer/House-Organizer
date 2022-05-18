package com.github.houseorganizer.houseorganizer.shop;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ItemsHolder> {

    public static class ItemsHolder extends RecyclerView.ViewHolder{
        public ImageButton cancel;
        public CheckBox checkBox;
        public ItemsHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.item_checkbox);
            cancel = itemView.findViewById(R.id.delete_item_button);
        }
    }


    private ShopList shopList;

    public ShopListAdapter(ShopList shopList){
        this.shopList = shopList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShopList(ShopList shopList){
        this.shopList = shopList;
        this.notifyDataSetChanged();
    }

    public FirestoreShopList getFirestoreShopList(){
        if(shopList instanceof FirestoreShopList) return (FirestoreShopList) shopList;
        return null;
    }

    public static Task<ShopListAdapter> initializeFirestoreShopList(DocumentReference currentHouse,
                                                                     FirebaseFirestore db) {
        if (currentHouse == null) return Tasks.forCanceled();
        CollectionReference root = db.collection("shop_lists");
        return root.whereEqualTo("household", currentHouse).get()
                .continueWithTask(r -> {
                    // If empty -> create new house
                    if (r.getResult().getDocuments().size() == 0) {
                        return null;
                        // If not empty then retrieve the existing shopList
                    } else {
                        return FirestoreShopList.retrieveShopList(root, currentHouse).continueWith(t -> {
                            FirestoreShopList shopList = t.getResult();
                            return new ShopListAdapter(shopList);
                        });
                    }
                });
    }

    @NonNull
    @Override
    public ItemsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.shoplist_row, parent, false);
        return new ItemsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsHolder holder, int position) {
        CheckBox box = holder.checkBox;
        ShopItem item = shopList.getItemAt(position);
        String text = item.toString();
        box.setText(text);
        box.setChecked(item.isPickedUp());
        box.setOnClickListener( v -> shopList.toggleItemPickedUp(position));
        holder.cancel.setOnClickListener(v -> {
            shopList.removeItem(item);
            this.notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public void setUpShopListView(AppCompatActivity parent) {
        RecyclerView shopListView = parent.findViewById(R.id.task_list);
        shopListView.setAdapter(this);
        shopListView.setLayoutManager(new LinearLayoutManager(parent));
    }

    public void addItem(AppCompatActivity parent, FirestoreShopList shopList){
        LayoutInflater inflater = LayoutInflater.from(parent);
        final View dialogView = inflater.inflate(R.layout.shop_item_dialog, null);
        new AlertDialog.Builder(parent)
                .setTitle(R.string.add_item_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> retrieveItemFromDialog(shopList, dialogView))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void retrieveItemFromDialog(FirestoreShopList shopList, View dialogView){
        final String name = ((EditText) dialogView.findViewById(R.id.edit_text_name)).getText().toString();
        final String unit = ((EditText) dialogView.findViewById(R.id.edit_text_unit)).getText().toString();
        int quantity = 0;
        try {
            quantity = Integer.parseInt(((EditText) dialogView.findViewById(R.id.edit_text_quantity)).getText().toString());
        }catch (Exception e){
            // Only possible bad input is empty field
        }
        shopList.addItem(new ShopItem(name, quantity, unit));
        this.notifyItemInserted(shopList.size()-1);
        shopList.updateItems();
    }
}
