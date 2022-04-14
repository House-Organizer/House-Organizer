package com.github.houseorganizer.houseorganizer.shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ItemsHolder> {

    public class ItemsHolder extends RecyclerView.ViewHolder{
        public CheckBox checkBox;
        public ImageButton cancelButton;
        public ItemsHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.item_checkbox);
            cancelButton = itemView.findViewById(R.id.delete_item_button);
        }
    }


    private final ShopList shopList;

    public ShopListAdapter(ShopList shopList){
        this.shopList = shopList;
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
        box.setOnClickListener( v -> {
                    boolean pickedUp = item.isPickedUp();
                    pickedUp = !pickedUp;
                    item.setPickedUp(pickedUp);
                    box.setChecked(pickedUp);
                });
        holder.cancelButton.setOnClickListener(v -> {
            shopList.removeItem(item);
            this.notifyItemRemoved(position);
        });
    }



    @Override
    public int getItemCount() {
        return shopList.size();
    }
}
