package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents an offline debt, with a title and debt information.
 */
public class OfflineDebt extends OfflineItem {
    private final String title, info;

    /**
     * Builds an OfflineDebt with the given title and information.
     *
     * @param title the title of this OfflineDebt, generally in the format "x owes y z CHF"
     * @param info the info of this OfflineDebt
     */
    public OfflineDebt(String title, String info) {
        this.title = title;
        this.info = info;
    }

    /**
     * Returns a serialized version of this OfflineDebt
     *
     * @return a serialized version of this OfflineDebt
     *
     * @see Object#toString()
     */
    @Override
    @NonNull
    public String toString() {
        return "OfflineDebt{" +
                "title='" + title + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    /**
     * Returns the title of this offline debt
     * @return the title of this offline debt
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the info of this offline debt
     * @return the info of this offline debt
     */
    public String getInfo() {
        return info;
    }

    /**
     * @see OfflineItem#title()
     */
    @NonNull
    public String title() {
        return title;
    }

    /**
     * @see OfflineItem#info()
     */
    @NonNull
    public String info() {
        return info;
    }

    /**
     * @see OfflineItem#colorRatio()
     */
    public float colorRatio() {
        return 0f;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OfflineDebt that = (OfflineDebt) o;
        return Objects.equals(title, that.title) && Objects.equals(info, that.info);

    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, info);
    }
}
