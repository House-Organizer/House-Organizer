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

/**
 * A general-purpose RecyclerView Adapter able to represent
 * OfflineItems such as OfflineTask, OfflineEvent etc.
 *
 * Each OfflineItem is represented by a colourful button.
 *
 * The colour of the button is obtained by mixing the primary
 * and secondary colours of the current theme, according to
 * the color ratio of the OfflineItem
 *
 * @see OfflineItem
 * @see RecyclerView.Adapter
 */
public final class OfflineAdapter extends RecyclerView.Adapter<OfflineAdapter.ItemHolder> {
    private final List<? extends OfflineItem> itemList;
    private final Context context;

    /**
     * Builds an OfflineAdapter for the given OfflineItem list
     * and application context.
     * @param items the OfflineItem list to be adapted
     * @param context the application context
     */
    public OfflineAdapter(List<? extends OfflineItem> items, Context context) {
        this.itemList = items;
        this.context = context;
    }

    /**
     * Returns a ViewHolder adapted for OfflineItems
     *
     * @param parent The ViewGroup into which the new View will be
     *               added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return a ViewHolder adapted for OfflineItems
     *
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     */
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.offline_item_row, parent, false);

        return new ItemHolder(view);
    }

    /**
     * Adds listeners such that users can interact with
     * a button for the current item. When the button is
     * clicked, an AlertDialog pops up with further information.
     *
     * @param holder: the ViewHolder used for the current OfflineItem
     * @param position: the position of the current OfflineItem UI being bound
     *
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
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

    /**
     * Mixes the primary and secondary colors of the theme
     * according to the color ratio of the given item.
     */
    private ColorStateList getColor(OfflineItem item) {
        int gradientStart = resolveColor(com.google.android.material.R.attr.colorPrimary);
        int gradientEnd = resolveColor(com.google.android.material.R.attr.colorSecondary);

        int color = ColorUtils.blendARGB(gradientStart, gradientEnd, 1-item.colorRatio());

        return ColorStateList.valueOf(color);
    }

    /**
     * Resolves the given color attribute in the current theme.
     *
     * (The equivalent of typing "?attr/{colorAttrId}" in an .xml file
     */
    private @ColorInt int resolveColor(@AttrRes int colorAttrId) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(colorAttrId, tv, true);

        return ContextCompat.getColor(context, tv.resourceId);
    }

    /**
     * Returns how many offline items are contained in this adapter
     *
     * @return how many offline items are contained in this adapter
     *
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Represents a general-purpose ViewHolder for OfflineItems.
     *
     * @see RecyclerView.ViewHolder
     */
    public static final class ItemHolder extends RecyclerView.ViewHolder {
        protected Button itemButton;

        /**
         * Builds a ViewHolder for an offline button starting from
         * the given parent view
         *
         * @param itemView the parent view for the offline button
         */
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            itemButton = itemView.findViewById(R.id.offline_item_button);
        }
    }
}
