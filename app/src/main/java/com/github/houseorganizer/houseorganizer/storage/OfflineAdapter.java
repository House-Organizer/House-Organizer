package com.github.houseorganizer.houseorganizer.storage;


import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.List;

public final class OfflineAdapter extends RecyclerView.Adapter<OfflineAdapter.ItemHolder> {
    private final List<? extends OfflineItem> itemList;
    private final Context context;

    public OfflineAdapter(List<? extends OfflineItem> items, Context context) {
        this.itemList = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.offline_item_row, parent, false);

        return new ItemHolder(view);
    }

    @Override // Very simple information about the item
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        OfflineItem item = itemList.get(position);
        holder.itemButton.setText(item.title());
        holder.itemButton.setBackgroundTintList(getColor(item));

        holder.itemButton.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle(item.title())
                        .setMessage(item.info())
                        .show());
    }

    private ColorStateList getColor(OfflineItem item) {
        int gradientStart = resolveColor(com.google.android.material.R.attr.colorPrimary);
        int gradientEnd = resolveColor(com.google.android.material.R.attr.colorSecondary);

        int color = ColorUtils.blendARGB(gradientStart, gradientEnd, 1-item.colorRatio());

        return ColorStateList.valueOf(color);
    }

    private @ColorInt int resolveColor(@AttrRes int colorAttrId) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(colorAttrId, tv, true);

        return ContextCompat.getColor(context, tv.resourceId);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        protected Button itemButton;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            itemButton = itemView.findViewById(R.id.offline_item_button);
        }
    }
}
