package com.github.houseorganizer.houseorganizer.util;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Represents a generic ViewHolder with 2 Views,
 * one on the left, the other on the right.
 *
 * @param <S> the type parameter of the left View
 * @param <T> the type parameter of the right View
 *
 * @see RecyclerView.ViewHolder
 */
public final class BiViewHolder<S extends View, T extends View> extends RecyclerView.ViewHolder {
    /**
     * The left View of this ViewHolder
     */
    public final S leftView;

    /**
     * The right View of this ViewHolder
     */
    public final T rightView;

    /**
     * Creates a BiViewHolder from the given parent View,
     * and the resource IDs for the left and right Views.
     *
     * @param itemView: the parent View in which the left and right views can be found
     * @param leftResId: the id of the left View
     * @param rightResId: the id of the right View
     */
    public BiViewHolder(@NonNull View itemView, @IdRes int leftResId, @IdRes int rightResId) {
        super(itemView);

        this.leftView  = itemView.findViewById(leftResId);
        this.rightView = itemView.findViewById(rightResId);
    }
}
