package com.github.houseorganizer.houseorganizer.util;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BiViewHolder<S extends View, T extends View> extends RecyclerView.ViewHolder {
    public final S leftView;
    public final T rightView;

    public BiViewHolder(@NonNull View itemView, @IdRes int leftResId, @IdRes int rightResId) {
        super(itemView);

        this.leftView  = itemView.findViewById(leftResId);
        this.rightView = itemView.findViewById(rightResId);
    }
}
