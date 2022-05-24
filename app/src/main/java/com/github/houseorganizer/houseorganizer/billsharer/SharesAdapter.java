package com.github.houseorganizer.houseorganizer.billsharer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharesAdapter extends RecyclerView.Adapter<SharesAdapter.ShareHolder> {

    private final HashMap<String, Double> shares;
    private final List<String> user_emails;

    public SharesAdapter(HashMap<String, Double> shares) {
        this.shares = shares;
        user_emails = new ArrayList<>();
        shares.forEach((k, v) -> user_emails.add(k));
    }

    @NonNull
    @Override
    public ShareHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.share_row, parent, false);
        return new ShareHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareHolder holder, int position) {
        String user = user_emails.get(position);
        holder.user.setText(user);
        holder.amount.setText(setShareAmount(holder));
    }

    private String setShareAmount(ShareHolder holder) {
        if (holder.modified) {
            return holder.amount.getText().toString();
        } else {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return user_emails.size();
    }

    public static class ShareHolder extends RecyclerView.ViewHolder {
        public TextView user;
        public EditText amount;
        public boolean modified;

        public ShareHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
