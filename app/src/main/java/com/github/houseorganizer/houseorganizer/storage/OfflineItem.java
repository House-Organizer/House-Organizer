package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

/**
 * Represents a generic offline item, with
 * a title, info, and color ratio.
 *
 * @see OfflineEvent
 * @see OfflineTask
 * @see OfflineShopItem
 * @see OfflineDebt
 */
public abstract class OfflineItem {

    /**
     * Returns the title of this offline item
     * @return the title of this offline item
     */
    @NonNull
    abstract public String title();

    /**
     * Returns the information of this offline item
     * @return the information of this offline item
     */
    @NonNull
    abstract public String info();

    /**
     * Returns the color ratio that should be used
     * when mixing 2 colors to represent the background
     * color of a button in a RecyclerView for OfflineItems
     *
     * @return the color ratio of this offline item
     */
    abstract public float colorRatio();
}
