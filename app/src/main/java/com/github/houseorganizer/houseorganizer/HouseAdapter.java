package com.github.houseorganizer.houseorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseHolder> {

    Context cx;
    String houseNames[];
    int houseImages[];

    public HouseAdapter(Context cx, String houseNames[], int houseImages[]) {
        this.cx = cx;
        this.houseNames = houseNames;
        this.houseImages = houseImages;
    }

    @NonNull
    @Override
    public HouseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cx);
        View view = inflater.inflate(R.layout.house_row, parent, false);
        return new HouseHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseHolder holder, int position) {
        holder.houseName.setText(houseNames[position]);
        holder.houseImage.setImageResource(houseImages[position]);
    }

    @Override
    public int getItemCount() {
        return houseNames.length;
    }

    public class HouseHolder extends RecyclerView.ViewHolder {

        TextView houseName;
        ImageView houseImage;

        public HouseHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.houseName);
            houseImage = itemView.findViewById(R.id.houseImage);
        }
    }
}
