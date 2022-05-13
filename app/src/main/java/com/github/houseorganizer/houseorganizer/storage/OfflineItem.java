package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

/**
 * @see OfflineEvent
 * @see OfflineTask
 * @see OfflineShopItem
 */
public abstract class OfflineItem {
    @NonNull
    abstract public String title();

    @NonNull
    abstract public String info();
}
