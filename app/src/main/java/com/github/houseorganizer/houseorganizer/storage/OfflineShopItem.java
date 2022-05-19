package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.github.houseorganizer.houseorganizer.R;

import java.util.Locale;
import java.util.Objects;

public final class OfflineShopItem extends OfflineItem {
    private final String name;
    private final int quantity;
    private final String unit;
    private final boolean isPickedUp;

    public OfflineShopItem(String name, int quantity, String unit, boolean isPickedUp){
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isPickedUp = isPickedUp;
    }

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

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isPickedUp() {
        return isPickedUp;
    }

    @NonNull
    @Override
    public String title() {
        return name;
    }

    public @AttrRes
    int color() {
        return com.google.android.material.R.attr.colorSecondaryVariant;
    }

    @NonNull
    public String info() {
        return String.format(Locale.ROOT, "%s [%d %s][%s]", name, quantity, unit, isPickedUp ? "x" : "\t");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            OfflineShopItem that = (OfflineShopItem) o;
            return quantity == that.quantity && isPickedUp == that.isPickedUp && Objects.equals(name, that.name) && Objects.equals(unit, that.unit);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unit, isPickedUp);
    }
}
