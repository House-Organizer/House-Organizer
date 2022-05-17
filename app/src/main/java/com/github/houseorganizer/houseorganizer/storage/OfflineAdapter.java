package com.github.houseorganizer.houseorganizer.storage;


import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public final class OfflineAdapter<T extends OfflineItem> extends RecyclerView.Adapter<OfflineAdapter.ItemHolder> {
    private final List<T> itemList;
    private final int viewResId, buttonResId;

    /**
     * @param viewResId: id of the recyclerview row xml
     * @param buttonResId: id of the view of individual elements, not of the whole recyclerview
     */
    public OfflineAdapter(List<T> items, @LayoutRes int viewResId, @IdRes int buttonResId) {
        this.itemList = items;
        this.buttonResId = buttonResId;
        this.viewResId = viewResId;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewResId, parent, false);

        return new ItemHolder(view, buttonResId);
    }

    @Override // Very simple information about the item
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        OfflineItem item = itemList.get(position);
        holder.itemButton.setText(item.title());
        holder.itemButton.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle(item.title())
                        .setMessage(item.info())
                        .show());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        protected Button itemButton;

        public ItemHolder(@NonNull View itemView, @IdRes int buttonResId) {
            super(itemView);
            itemButton = itemView.findViewById(buttonResId);
        }
    }
}
