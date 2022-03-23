package com.github.houseorganizer.houseorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HouseAdapter extends RecyclerView.Adapter<BiViewHolder<ImageView, TextView>> {

    final Context cx;
    final String[] houseNames;
    final int[] houseImages;

    public HouseAdapter(Context cx, String[] houseNames, int[] houseImages) {
        this.cx = cx;
        this.houseNames = houseNames;
        this.houseImages = houseImages;
    }

    @NonNull
    @Override
    public BiViewHolder<ImageView, TextView> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cx);
        View view = inflater.inflate(R.layout.house_row, parent, false);
        return new BiViewHolder<>(view, R.id.houseImage, R.id.houseName);
    }

    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<ImageView, TextView> holder, int position) {
        holder.rightView.setText(houseNames[position]);
        holder.leftView.setImageResource(houseImages[position]);
    }

    @Override
    public int getItemCount() {
        return houseNames.length;
    }

    /*
    public static class HouseHolder extends RecyclerView.ViewHolder {
        final TextView houseName;
        final ImageView houseImage;
        public HouseHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.houseName);
            houseImage = itemView.findViewById(R.id.houseImage);
        }
    } */
}