package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.Objects;

public class OfflineDebt extends OfflineItem {
    private final String title, info;

    public OfflineDebt(String title, String info) {
        this.title = title;
        this.info = info;
    }

    @Override
    @NonNull
    public String toString() {
        return "OfflineDebt{" +
                "title='" + title + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    @NonNull
    @Override
    public String title() {
        return title;
    }

    @NonNull
    @Override
    public String info() {
        return info;
    }

    public float colorRatio() {
        return 0f;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OfflineDebt that = (OfflineDebt) o;
        return Objects.equals(title, that.title) && Objects.equals(info, that.info);

    }

    @Override
    public int hashCode() {
        return Objects.hash(title, info);
    }
}