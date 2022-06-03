package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents an offline shop item, which has a name, unit,
 * quantity, and picked-up information.
 *
 * @see OfflineItem
 */
public final class OfflineShopItem extends OfflineItem {
    private final String name;
    private final int quantity;
    private final String unit;
    private final boolean isPickedUp;

    /**
     * Creates an OfflineShopItem with the given name, quantity,
     * unit, and picked-up information.
     *
     * @param name: the name of the shop item
     * @param quantity: the quantity of the shop item
     * @param unit: the unit of the shop item
     * @param isPickedUp: whether the shop item has been picked up
     */
    public OfflineShopItem(String name, int quantity, String unit, boolean isPickedUp){
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isPickedUp = isPickedUp;
    }

    /**
     * Returns a serialized version of this OfflineShopItem
     *
     * @return a serialized version of this OfflineShopItem
     *
     * @see Object#toString()
     */
    @NonNull
    @Override
    public String toString() {
        return "OfflineShopItem{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", isPickedUp=" + isPickedUp +
                '}';
    }

    /**
     * Returns the name of this OfflineShopItem
     * @return the name of this OfflineShopItem
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the quantity of this OfflineShopItem
     * @return the quantity of this OfflineShopItem
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the unit of this OfflineShopItem
     * @return the unit of this OfflineShopItem
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the picked-up status of this OfflineShopItem
     * @return the picked-up status of this OfflineShopItem
     */
    public boolean isPickedUp() {
        return isPickedUp;
    }

    /**
     * @see OfflineItem#title()
     */
    @NonNull
    @Override
    public String title() {
        return name;
    }

    /**
     * @see OfflineItem#colorRatio()
     */
    public float colorRatio() {
        return 0.6f;
    }

    /**
     * @see OfflineItem#info()
     */
    @NonNull
    public String info() {
        return String.format(Locale.ROOT, "%s [%d %s][%s]", name, quantity, unit, isPickedUp ? "x" : "\t");
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            OfflineShopItem that = (OfflineShopItem) o;
            return quantity == that.quantity && isPickedUp == that.isPickedUp && Objects.equals(name, that.name) && Objects.equals(unit, that.unit);
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unit, isPickedUp);
    }
}
